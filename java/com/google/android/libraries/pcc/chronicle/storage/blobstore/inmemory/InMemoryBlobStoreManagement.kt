/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.pcc.chronicle.storage.blobstore.inmemory

import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStoreManagement
import com.google.android.libraries.pcc.chronicle.storage.blobstore.ManagementInfo
import java.time.Instant

/** Implementation of [BlobStoreManagement] for in memory storage using the [InMemoryStorage]. */
class InMemoryBlobStoreManagement(private val inMemoryStorage: InMemoryStorage) :
  BlobStoreManagement {
  override suspend fun clearAll(): Int {
    return inMemoryStorage.store.value.values.sumOf { it.purgeEntitiesWhere { true } }
  }

  override suspend fun deleteEntitiesCreatedBetween(
    startTimeMillis: Long,
    endTimeMillis: Long,
  ): Int {
    return inMemoryStorage.store.value.values.sumOf { cache ->
      cache.purgeEntitiesWhere { entity ->
        entity.metadata.created.toInstant().isAfter(Instant.ofEpochMilli(startTimeMillis)) &&
          entity.metadata.created.toInstant().isBefore(Instant.ofEpochMilli(endTimeMillis))
      }
    }
  }

  override suspend fun trim(managementInfos: Set<ManagementInfo>) {}

  override suspend fun deletePackage(packageName: String): Int {
    return inMemoryStorage.store.value.values.sumOf { cache ->
      cache.purgeEntitiesWhere { entity ->
        packageName in entity.metadata.associatedPackageNamesList
      }
    }
  }

  override suspend fun reconcilePackages(allowedPackages: Set<String>): Int {
    return inMemoryStorage.store.value.values.sumOf { cache ->
      cache.purgeEntitiesWhere { entity ->
        entity.metadata.associatedPackageNamesList.any { pkg -> pkg !in allowedPackages }
      }
    }
  }

  override suspend fun deleteExpiredEntities(
    currentTimeMillis: Long,
    managementInfos: Set<ManagementInfo>,
  ) {
    inMemoryStorage.store.value.values.map { cache ->
      cache.purgeEntitiesWhere { entity ->
        entity.isExpired(cache.managementInfo.ttlMillis, currentTimeMillis)
      }
    }
  }

  private suspend fun CacheWrapper<*>.purgeEntitiesWhere(
    predicate: (WrappedEntity<*>) -> Boolean
  ): Int {
    return cache
      .snapshot()
      .asSequence()
      .filter { (_, entity) -> predicate(entity) }
      .map { (key, _) -> cache.remove(key) }
      .count()
  }

  private fun WrappedEntity<*>.isExpired(ttlMillis: Long, currentTimeMillis: Long): Boolean =
    metadata.created
      .toInstant()
      .plusMillis(ttlMillis)
      .isBefore(Instant.ofEpochMilli(currentTimeMillis))
}
