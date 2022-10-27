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

package com.google.android.libraries.pcc.chronicle.storage.datacache.impl

import com.google.android.libraries.pcc.chronicle.api.ChronicleAnalyticsClient
import com.google.android.libraries.pcc.chronicle.api.ChronicleDeletionListener
import com.google.android.libraries.pcc.chronicle.api.PackageDeletionListener
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage

class DataCacheStoragePackageDeletionListener(
  private val client: ChronicleAnalyticsClient,
  private val dataStorageCache: DataCacheStorage
) : PackageDeletionListener {
  override suspend fun deleteFor(
    value: PackageDeletionListener.PackageInstallInfo
  ): ChronicleDeletionListener.PackageDeletionInfo {
    val removed = dataStorageCache.purgeAllEntitiesForPackage(value.packageName)
    return ChronicleDeletionListener.PackageDeletionInfo(client, removed)
  }

  override suspend fun reconcile(
    fullSet: Set<PackageDeletionListener.PackageInstallInfo>
  ): ChronicleDeletionListener.PackageDeletionInfo {
    val names = fullSet.asSequence().map { it.packageName }.toSet()
    val removed = dataStorageCache.purgeAllEntitiesNotInPackages(names)
    return ChronicleDeletionListener.PackageDeletionInfo(client, removed)
  }
}
