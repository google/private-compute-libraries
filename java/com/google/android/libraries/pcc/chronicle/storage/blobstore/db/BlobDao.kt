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

package com.google.android.libraries.pcc.chronicle.storage.blobstore.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.google.android.libraries.pcc.chronicle.storage.blobstore.QuotaInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TrimOrder

/** Data access object for blob data in [BlobDatabase]. */
@Dao
interface BlobDao {
  /**
   * Inserts a [BlobEntity]. Uses ignore strategy if unique constraint error on id or key/dtdName
   * pair is encountered. Returns the id of the inserted/replaced blob, or -1 if the insert was
   * ignored. This is a helper method and should only be called from
   * [insertOrUpdateBlobWithPackages] so Blob and Package table stay in sync.
   */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertBlobIfAbsent(entity: BlobEntity): Long

  /**
   * Inserts a [PackageEntity]. Uses abort strategy if unique constraint error on blobId/packageName
   * pair is encountered, which rolls back the transaction and throws an exception. Returns the row
   * id of the inserted package if insert is successful. This is a helper method and should only be
   * called from [insertOrUpdateBlobWithPackages]so Blob and Package table stay in sync.
   */
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertPackageIfAbsent(blobPackage: PackageEntity): Long

  /**
   * Inserts a [BlobEntity] and its associated [PackageEntities][PackageEntity] or updates an
   * existing [BlobEntity] (does not update associated packages).
   */
  @Transaction
  suspend fun insertOrUpdateBlobWithPackages(
    entity: BlobEntity,
    packages: List<String>,
    threshold: Long,
  ) {
    var id = insertBlobIfAbsent(entity)
    // An id of -1 here means that the insert was ignored due to uniqueness constraints.
    if (id == -1L) {
      val currentEntity =
        blobEntityWithPackagesByKeyAndDtdNameNoTtlCheck(entity.key, entity.dtdName) ?: return
      if (currentEntity.isExpired(threshold)) {
        removeBlobEntityById(currentEntity.blobEntity.id)
        id = insertBlobIfAbsent(entity)
      } else {
        update(
          UpdateBlobEntity(currentEntity.blobEntity.id, entity.updateTimestampMillis, entity.blob)
        )
        return
      }
    }
    // The following lines are only reached if the entity was inserted, either immediately on the
    // first try or after deleting it if it was expired.
    packages.forEach { insertPackageIfAbsent(PackageEntity(id, it)) }
  }

  /**
   * Inserts a collection of [BlobEntities][BlobEntity] and their associated [PackageEntities]
   * [PackageEntity]. If any of the [BlobEntities][BlobEntity] already exist, it is updated
   * (associated packages not updated).
   */
  @Transaction
  suspend fun insertOrUpdateBlobsWithPackages(
    blobsWithPackagesMap: Map<BlobEntity, List<String>>,
    dtdName: String,
    quotaInfo: QuotaInfo,
    threshold: Long,
  ) {
    require(blobsWithPackagesMap.size <= quotaInfo.maxRowCount) {
      "Number of entities to insert exceeds quota limit."
    }

    blobsWithPackagesMap.entries.forEach {
      insertOrUpdateBlobWithPackages(it.key, it.value, threshold)
    }

    val rowCount = countBlobsByDtdName(dtdName)
    if (rowCount <= quotaInfo.maxRowCount) {
      return
    }
    val numRowsToDelete = rowCount - quotaInfo.minRowsAfterTrim
    if (quotaInfo.trimOrder == TrimOrder.NEWEST) {
      removeNewestBlobEntitiesByDtdName(dtdName, numRowsToDelete)
    } else {
      removeOldestBlobEntitiesByDtdName(dtdName, numRowsToDelete)
    }
  }

  /**
   * Performs a partial update of a [BlobEntity], only updating the updateTimestamp and serialized
   * blob.
   */
  @Update(entity = BlobEntity::class) suspend fun update(updateBlobEntity: UpdateBlobEntity): Int

  /** Queries the DB for a [BlobEntity] and associated [PackageEntity]s by key/dtdName pair. */
  @Transaction
  @Query(
    """
    SELECT * FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.KEY} = :key
      AND ${BlobEntity.DTD_NAME} = :dtdName
      AND ${BlobEntity.CREATED_TIMESTAMP_MILLIS} >= :threshold
    """
  )
  suspend fun blobEntityWithPackagesByKeyAndDtdName(
    key: String,
    dtdName: String,
    threshold: Long,
  ): BlobEntityWithPackages?

  /**
   * Queries the DB for a [BlobEntity] and associated [PackageEntity]s by key/dtdName pair without
   * checking if the entity is expired. This is a helper function for
   * [insertOrUpdateBlobWithPackages] and shouldn't be called directly by any other function.
   */
  @Transaction
  @Query(
    """
    SELECT * FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.KEY} = :key
      AND ${BlobEntity.DTD_NAME} = :dtdName
    """
  )
  suspend fun blobEntityWithPackagesByKeyAndDtdNameNoTtlCheck(
    key: String,
    dtdName: String,
  ): BlobEntityWithPackages?

  /**
   * Queries the DB for [BlobEntities][BlobEntity] and their associated [PackageEntities]
   * [PackageEntity] by dtdName.
   */
  @Transaction
  @Query(
    """
      SELECT * FROM ${BlobEntity.TABLE_NAME} 
      WHERE ${BlobEntity.DTD_NAME} = :dtdName
        AND ${BlobEntity.CREATED_TIMESTAMP_MILLIS} >= :threshold
    """
  )
  suspend fun blobEntitiesWithPackagesByDtdName(
    dtdName: String,
    threshold: Long,
  ): List<BlobEntityWithPackages>

  /**
   * Queries the DB for [BlobEntities][BlobEntity] and their associated [PackageEntities]
   * [PackageEntity] by locusId/dtdName pair.
   */
  @Transaction
  @Query(
    """
    SELECT * FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.LOCUS_ID} = :locusId
      AND ${BlobEntity.DTD_NAME} = :dtdName
      AND ${BlobEntity.CREATED_TIMESTAMP_MILLIS} >= :threshold
    """
  )
  suspend fun blobEntitiesWithPackagesByLocusIdAndDtdName(
    locusId: String,
    dtdName: String,
    threshold: Long,
  ): List<BlobEntityWithPackages>

  /**
   * Queries the DB for [BlobEntities][BlobEntity] and their associated [PackageEntities]
   * [PackageEntity] by packageName/dtdName pair.
   */
  @Transaction
  @Query(
    """
    SELECT * FROM ${PackageEntity.TABLE_NAME}
    JOIN Blobs ON ${PackageEntity.TABLE_NAME}.${PackageEntity.BLOB_ID} = ${BlobEntity.TABLE_NAME}.${BlobEntity.ID}
    WHERE ${PackageEntity.TABLE_NAME}.${PackageEntity.PACKAGE_NAME} = :packageName
      AND ${BlobEntity.TABLE_NAME}.${BlobEntity.DTD_NAME} = :dtdName
      AND ${BlobEntity.CREATED_TIMESTAMP_MILLIS} >= :threshold
    """
  )
  suspend fun blobEntitiesWithPackagesByPackageNameAndDtdName(
    packageName: String,
    dtdName: String,
    threshold: Long,
  ): Map<PackageEntity, List<BlobEntityWithPackages>>

  /** Queries the DB for [PackageEntities][PackageEntity] by packageName. */
  @Query(
    "SELECT * FROM ${PackageEntity.TABLE_NAME} WHERE ${PackageEntity.PACKAGE_NAME} = :packageName"
  )
  suspend fun packagesByPackageName(packageName: String): List<PackageEntity>

  /** Queries the DB for a [PackageEntities][PackageEntity] by blobId. */
  @Query("SELECT * FROM ${PackageEntity.TABLE_NAME} WHERE ${PackageEntity.BLOB_ID} = :blobId")
  suspend fun packagesByBlobId(blobId: Long): List<PackageEntity>

  /**
   * Deletes a [BlobEntity] with specified id. Any [PackageEntities][PackageEntity] with blobId
   * equal to the entity's id will also be deleted.
   */
  @Query("DELETE FROM ${BlobEntity.TABLE_NAME} WHERE ${BlobEntity.ID} = :id")
  suspend fun removeBlobEntityById(id: Long): Int

  /**
   * Deletes a [BlobEntity] with specified key/dtdName pair. Any [PackageEntities][PackageEntity]
   * with blobId equal to the entity's id will also be deleted.
   */
  @Query(
    """
    DELETE FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.KEY} = :key
      AND ${BlobEntity.DTD_NAME} = :dtdName
    """
  )
  suspend fun removeBlobEntityByKeyAndDtdName(key: String, dtdName: String)

  /**
   * Deletes [PackageEntities][PackageEntity] with specified blobId. This is a helper method and
   * should not be called directly. It is used when [BlobEntities][BlobEntity] are inserted/replaced
   * to ensure its associated [PackageEntities][PackageEntity] stay up to date. In all other
   * circumstances, to delete [PackageEntities][PackageEntity], any of the functions that delete a
   * [BlobEntity] should be called instead as they will also delete any associated [PackageEntities]
   * [PackageEntity].
   */
  @Query("DELETE FROM ${PackageEntity.TABLE_NAME} WHERE ${PackageEntity.BLOB_ID} = :blobId")
  suspend fun removePackagesByBlobId(blobId: Long)

  /**
   * Deletes [BlobEntities][BlobEntity] with specified packageName. Any [PackageEntities]
   * [PackageEntity] with blobIds equal to the deleted entities' ids will also be deleted.
   */
  @Transaction
  suspend fun removeBlobAndPackageEntitiesByPackageName(packageName: String): Int {
    val packageEntities = packagesByPackageName(packageName)
    return packageEntities.map { it.blobId }.toSet().sumOf { removeBlobEntityById(it) }
  }

  /** Deletes [BlobEntities][BlobEntity] with specified dtdName. */
  @Query("DELETE FROM ${BlobEntity.TABLE_NAME} WHERE ${BlobEntity.DTD_NAME} = :dtdName")
  suspend fun removeBlobEntitiesByDtdName(dtdName: String)

  /**
   * Deletes all [BlobEntities][BlobEntity]. This method should only be called by BlobStore
   * Management.
   */
  @Query("DELETE FROM ${BlobEntity.TABLE_NAME}") suspend fun removeAllBlobEntities(): Int

  /**
   * Deletes expired [BlobEntities][BlobEntity] with specified dtdName based on the provided ttl
   * threshold time. This method should only be called by BlobStore Management.
   */
  @Query(
    """
    DELETE FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.DTD_NAME} = :dtdName
      AND ${BlobEntity.CREATED_TIMESTAMP_MILLIS} < :threshold
    """
  )
  suspend fun removeExpiredBlobEntitiesByDtdName(dtdName: String, threshold: Long): Int

  /**
   * Deletes [BlobEntities][BlobEntity] with specified dtdName between the time period between the
   * provided start and end times. Returns the number of rows deleted. This method should only be
   * called by BlobStore Management.
   */
  @Query(
    """
    DELETE FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.CREATED_TIMESTAMP_MILLIS} >= :startTimeMillis
      AND ${BlobEntity.CREATED_TIMESTAMP_MILLIS} < :endTimeMillis
    """
  )
  suspend fun removeBlobEntitiesCreatedBetween(startTimeMillis: Long, endTimeMillis: Long): Int

  /**
   * Deletes the specified number of [BlobEntities][BlobEntity] with provided dtdName, deleting
   * oldest ones first. This method should only be called by BlobStore Management.
   */
  @Query(
    """
    DELETE FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.ID} IN (
      SELECT ${BlobEntity.ID} FROM ${BlobEntity.TABLE_NAME}
      WHERE ${BlobEntity.DTD_NAME} = :dtdName
      ORDER BY ${BlobEntity.ID} ASC LIMIT :numRowsToDelete
    )
    """
  )
  suspend fun removeOldestBlobEntitiesByDtdName(dtdName: String, numRowsToDelete: Int)

  /**
   * Deletes the specified number of [BlobEntities][BlobEntity] with provided dtdName, deleting
   * newest ones first. This method should only be called by BlobStore Management.
   */
  @Query(
    """
    DELETE FROM ${BlobEntity.TABLE_NAME}
    WHERE ${BlobEntity.ID} IN (
      SELECT ${BlobEntity.ID} FROM ${BlobEntity.TABLE_NAME}
      WHERE ${BlobEntity.DTD_NAME} = :dtdName
      ORDER BY ${BlobEntity.ID} DESC LIMIT :numRowsToDelete
    )
    """
  )
  suspend fun removeNewestBlobEntitiesByDtdName(dtdName: String, numRowsToDelete: Int)

  /** Returns the number of [BlobEntities][BlobEntity] with specified dtdName stored in the DB. */
  @Query("SELECT COUNT(1) FROM ${BlobEntity.TABLE_NAME} WHERE ${BlobEntity.DTD_NAME} = :dtdName")
  suspend fun countBlobsByDtdName(dtdName: String): Int

  /**
   * Returns list of distinct package names in package table. This is a helper function and should
   * not be called directly.
   */
  @Query("SELECT * FROM ${PackageEntity.TABLE_NAME}") suspend fun allPackages(): List<PackageEntity>

  /**
   * Deletes all entities and packages associated with those entities that are not in the provided
   * allowed packages list. Returns the number of rows deleted.
   */
  @Transaction
  suspend fun deleteNotAllowedPackages(allowedPackages: Set<String>): Int {
    val packagesToRemove = allPackages().map { it.packageName }.distinct() - allowedPackages
    return packagesToRemove.sumOf { removeBlobAndPackageEntitiesByPackageName(it) }
  }

  private fun BlobEntityWithPackages.isExpired(threshold: Long): Boolean =
    blobEntity.createdTimestampMillis < threshold
}
