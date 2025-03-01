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

import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStoreManagement
import com.google.android.libraries.pcc.chronicle.storage.blobstore.ManagementInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.PersistedManagementInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TrimOrder
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobDao

/** Implementation of [BlobStoreManagement] for persisted storage using the [BlobDao]. */
class PersistedBlobStoreManagement(private val dao: BlobDao) : BlobStoreManagement {
  override suspend fun clearAll(): Int {
    return dao.removeAllBlobEntities()
  }

  override suspend fun deleteExpiredEntities(
    currentTimeMillis: Long,
    managementInfos: Set<ManagementInfo>,
  ) {
    managementInfos.forEach { info ->
      if (info is PersistedManagementInfo<*>) {
        dao.removeExpiredBlobEntitiesByDtdName(info.dtdName, currentTimeMillis - info.ttlMillis)
      }
    }
  }

  override suspend fun deleteEntitiesCreatedBetween(
    startTimeMillis: Long,
    endTimeMillis: Long,
  ): Int {
    return dao.removeBlobEntitiesCreatedBetween(startTimeMillis, endTimeMillis)
  }

  override suspend fun trim(managementInfos: Set<ManagementInfo>) {
    managementInfos.forEach { info ->
      if (info is PersistedManagementInfo<*>) {
        val rowCount = dao.countBlobsByDtdName(info.dtdName)
        if (rowCount <= info.quotaInfo.maxRowCount) {
          return@forEach
        }
        val numRowsToDelete = rowCount - info.quotaInfo.minRowsAfterTrim
        if (info.quotaInfo.trimOrder == TrimOrder.NEWEST) {
          dao.removeNewestBlobEntitiesByDtdName(info.dtdName, numRowsToDelete)
        } else {
          dao.removeOldestBlobEntitiesByDtdName(info.dtdName, numRowsToDelete)
        }
        // If for some reason due to race conditions or other circumstances, the trim doesn't bring
        // the number of entries below the maxRowCount for the specified type, all entries are
        // cleared.
        if (dao.countBlobsByDtdName(info.dtdName) > info.quotaInfo.maxRowCount) {
          dao.removeBlobEntitiesByDtdName(info.dtdName)
        }
      }
    }
  }

  override suspend fun deletePackage(packageName: String): Int {
    return dao.removeBlobAndPackageEntitiesByPackageName(packageName)
  }

  override suspend fun reconcilePackages(allowedPackages: Set<String>): Int {
    return dao.deleteNotAllowedPackages(allowedPackages)
  }
}
