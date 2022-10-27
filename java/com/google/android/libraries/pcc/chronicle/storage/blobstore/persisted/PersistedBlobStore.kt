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

package com.google.android.libraries.pcc.chronicle.storage.blobstore.persisted

import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStore
import com.google.android.libraries.pcc.chronicle.storage.blobstore.PersistedManagementInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobDao
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobEntity
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobEntityWithPackages
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.protobuf.MessageLite
import java.time.Instant

/**
 * Implementation of [BlobStore] that reads/writes from persisted BlobStore db.
 *
 * @param T the type of the data that will be stored. Must be a proto.
 * @param managementInfo the [PersistedManagementInfo] for your entity.
 */
class PersistedBlobStore<T : MessageLite>(
  private val dao: BlobDao,
  private val managementInfo: PersistedManagementInfo<T>,
  private val timeSource: TimeSource
) : BlobStore<T> {

  override suspend fun putEntity(wrappedEntity: WrappedEntity<T>) {
    dao.insertOrUpdateBlobWithPackages(
      wrappedEntityToPersistedEntity(wrappedEntity, timeSource.now().toEpochMilli()),
      wrappedEntity.metadata.associatedPackageNamesList,
      timeSource.now().toEpochMilli() - managementInfo.ttlMillis
    )
  }

  override suspend fun putEntities(wrappedEntities: Collection<WrappedEntity<T>>) {
    val map =
      wrappedEntities.associate {
        wrappedEntityToPersistedEntity(it, timeSource.now().toEpochMilli()) to
          it.metadata.associatedPackageNamesList
      }
    dao.insertOrUpdateBlobsWithPackages(
      map,
      managementInfo.dtdName,
      managementInfo.quotaInfo,
      timeSource.now().toEpochMilli() - managementInfo.ttlMillis
    )
  }

  override suspend fun getEntityByKey(key: String): WrappedEntity<T>? {
    val persisted =
      dao.blobEntityWithPackagesByKeyAndDtdName(
        key,
        managementInfo.dtdName,
        timeSource.now().toEpochMilli() - managementInfo.ttlMillis
      )
        ?: return null
    return persistedEntityToWrappedEntity(persisted)
  }

  override suspend fun getAllEntities(): List<WrappedEntity<T>> {
    return dao
      .blobEntitiesWithPackagesByDtdName(
        managementInfo.dtdName,
        timeSource.now().toEpochMilli() - managementInfo.ttlMillis
      )
      .map { persistedEntityToWrappedEntity(it) }
  }

  override suspend fun removeEntityByKey(key: String) {
    dao.removeBlobEntityByKeyAndDtdName(key, managementInfo.dtdName)
  }

  override suspend fun removeAll() {
    dao.removeBlobEntitiesByDtdName(managementInfo.dtdName)
  }

  private fun persistedEntityToWrappedEntity(persisted: BlobEntityWithPackages): WrappedEntity<T> {
    return WrappedEntity(
      EntityMetadata(
        id = persisted.blobEntity.key,
        associatedPackageNames = persisted.packages.map { it.packageName },
        created = Instant.ofEpochMilli(persisted.blobEntity.createdTimestampMillis),
        updated = Instant.ofEpochMilli(persisted.blobEntity.updateTimestampMillis)
      ),
      managementInfo.deserializer(persisted.blobEntity.blob)
    )
  }

  private fun wrappedEntityToPersistedEntity(
    wrapped: WrappedEntity<T>,
    timestampMillis: Long
  ): BlobEntity {
    return BlobEntity(
      key = wrapped.metadata.id,
      // TODO(b/239590875): update locusId to be pulled from metadata
      locusId = "",
      createdTimestampMillis = timestampMillis,
      updateTimestampMillis = timestampMillis,
      dtdName = managementInfo.dtdName,
      blob = wrapped.entity.toByteArray()
    )
  }
}
