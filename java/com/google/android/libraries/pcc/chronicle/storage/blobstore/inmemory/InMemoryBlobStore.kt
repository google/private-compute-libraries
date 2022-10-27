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

import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStore
import com.google.android.libraries.pcc.chronicle.util.TimeSource

/**
 * Implementation of [BlobStore] that reads from/writes to memory.
 *
 * @param T the type of the data that will be stored. Must be a proto.
 * @param store the [CacheWrapper] that contains the cache and management information for the cache.
 */
class InMemoryBlobStore<T>(private val store: CacheWrapper<T>, private val timeSource: TimeSource) :
  BlobStore<T> {
  override suspend fun putEntity(wrappedEntity: WrappedEntity<T>) {
    val currentEntity = store.cache.get(wrappedEntity.metadata.id)
    if (currentEntity != null) {
      if (currentEntity.isExpired()) {
        store.cache.remove(currentEntity.metadata.id)
      } else {
        val updatedEntity =
          WrappedEntity(
            metadata =
              EntityMetadata(
                id = currentEntity.metadata.id,
                associatedPackageNames = currentEntity.metadata.associatedPackageNamesList,
                created = currentEntity.metadata.created.toInstant(),
                updated = timeSource.now()
              ),
            entity = wrappedEntity.entity
          )
        store.cache.put(updatedEntity.metadata.id, updatedEntity)
        return
      }
    }
    val timestamp = timeSource.now()
    store.cache.put(
      wrappedEntity.metadata.id,
      wrappedEntity.copy(
        metadata =
          EntityMetadata(
            id = wrappedEntity.metadata.id,
            associatedPackageNames = wrappedEntity.metadata.associatedPackageNamesList,
            created = timestamp,
            updated = timestamp
          )
      )
    )
  }

  override suspend fun putEntities(wrappedEntities: Collection<WrappedEntity<T>>) {
    wrappedEntities.forEach { putEntity(it) }
  }

  override suspend fun getEntityByKey(key: String): WrappedEntity<T>? {
    return store.cache.get(key)?.takeIf { !it.isExpired() }
  }

  override suspend fun getAllEntities(): List<WrappedEntity<T>> {
    return store.cache.snapshot().values.filterNot { it.isExpired() }
  }

  override suspend fun removeEntityByKey(key: String) {
    store.cache.remove(key)
  }

  override suspend fun removeAll() {
    store.cache.evictAll()
  }

  private fun WrappedEntity<*>.isExpired(): Boolean =
    metadata.created
      .toInstant()
      .plusMillis(store.managementInfo.ttlMillis)
      .isBefore(timeSource.now())
}
