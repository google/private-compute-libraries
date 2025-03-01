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

package com.google.android.libraries.pcc.chronicle.storage.blobstore

import com.google.android.libraries.pcc.chronicle.api.ChronicleAnalyticsClient
import com.google.android.libraries.pcc.chronicle.api.ChronicleDeletionListener
import com.google.android.libraries.pcc.chronicle.api.PackageDeletionListener
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/**
 * The BlobStoreManager is responsible for making sure that persisted and in memory data remains
 * compliant with the privacy and system health requirements.
 */
class BlobStoreManager(val timeSource: TimeSource, val managements: Set<BlobStoreManagement>) :
  PackageDeletionListener {
  private var managementInfos = atomic(emptySet<ManagementInfo>())

  /**
   * This function takes in a [ManagementInfo] as a parameter and attempts to add it to the set of
   * [managementInfos]. If the dtdName of the provided [ManagementInfo] exists already in the set of
   * [managementInfos], the add fails. We do not allow devs to update a [ManagementInfo] once
   * created and we do not allow different types of [ManagementInfo]/blobstores for the same DTD.
   *
   * If the provided [ManagementInfo] was successfully added to [managementInfos], this same
   * [ManagementInfo] is returned. If the provided [ManagementInfo] wasn't added, the existing
   * [ManagementInfo] for that dtdName is returned. A null is returned if something goes
   * catastrophically wrong, but this should never happen.
   */
  internal fun addManagementInfo(managementInfo: ManagementInfo): ManagementInfo? {
    managementInfos.update { infos ->
      if (infos.any { it.dtdName == managementInfo.dtdName }) {
        return@update infos
      }
      return@update infos + managementInfo
    }
    return managementInfos.value.find { it.dtdName == managementInfo.dtdName }
  }

  /**
   * This function iterates through [ManagementInfos][ManagementInfo] and deletes expired entities
   * and trims entities in excess of the quota limit.
   */
  suspend fun performMaintenance() {
    val currentTimeMillis = timeSource.now().toEpochMilli()
    managements.forEach { management ->
      management.deleteExpiredEntities(currentTimeMillis, managementInfos.value)
      management.trim(managementInfos.value)
    }
  }

  /**
   * This function deletes all entities associated with the provided package [value] when it's
   * uninstalled from the device.
   */
  override suspend fun deleteFor(
    value: PackageDeletionListener.PackageInstallInfo
  ): ChronicleDeletionListener.PackageDeletionInfo {
    var numberOfEntitiesDeleted =
      managements.sumOf { management -> management.deletePackage(value.packageName) }
    return ChronicleDeletionListener.PackageDeletionInfo(
      ChronicleAnalyticsClient.BLOBSTORE,
      numberOfEntitiesDeleted,
    )
  }

  /**
   * This function deletes all entities associated with packages that are not contained in the
   * provided packages [fullSet].
   */
  override suspend fun reconcile(
    fullSet: Set<PackageDeletionListener.PackageInstallInfo>
  ): ChronicleDeletionListener.PackageDeletionInfo {
    val allowedPackages = fullSet.map { it.packageName }.toSet()
    var numberOfEntitiesDeleted =
      managements.sumOf { management -> management.reconcilePackages(allowedPackages) }
    return ChronicleDeletionListener.PackageDeletionInfo(
      ChronicleAnalyticsClient.BLOBSTORE,
      numberOfEntitiesDeleted,
    )
  }

  /** This function deletes all entities that with created timestamp in the given time range. */
  suspend fun clearDataCreatedBetween(startTimeMillis: Long, endTimeMillis: Long): Boolean {
    managements.forEach { management ->
      management.deleteEntitiesCreatedBetween(startTimeMillis, endTimeMillis)
    }
    return true
  }

  /** This function deletes all entities. */
  suspend fun clearAllData(): Boolean {
    managements.forEach { management -> management.clearAll() }
    return true
  }
}
