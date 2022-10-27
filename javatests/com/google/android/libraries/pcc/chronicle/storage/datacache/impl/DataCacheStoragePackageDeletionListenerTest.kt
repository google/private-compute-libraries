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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.ChronicleAnalyticsClient
import com.google.android.libraries.pcc.chronicle.api.ChronicleDeletionListener.PackageDeletionInfo
import com.google.android.libraries.pcc.chronicle.api.PackageDeletionListener.PackageInstallInfo
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataCacheStoragePackageDeletionListenerTest {
  private val dataCacheStorage = mock<DataCacheStorage>()

  val testInstance =
    DataCacheStoragePackageDeletionListener(ChronicleAnalyticsClient.PECAN, dataCacheStorage)

  @Test
  fun deleteFor_triggersPurgeAllEntitiesForPackage_returnsCount() =
    runTest(UnconfinedTestDispatcher()) {
      val expectedCount = 42
      val testPackage = "TestPackage"
      whenever(dataCacheStorage.purgeAllEntitiesForPackage(testPackage)).thenReturn(expectedCount)

      val result = testInstance.deleteFor(PackageInstallInfo(testPackage, 123))

      val expectedResult = PackageDeletionInfo(ChronicleAnalyticsClient.PECAN, expectedCount)
      assertThat(result).isEqualTo(expectedResult)
      verify(dataCacheStorage).purgeAllEntitiesForPackage(testPackage)
      verifyNoMoreInteractions(dataCacheStorage)
    }

  @Test
  fun reconcile_triggersPurgeAllEntitiesForPackage_returnsCount() =
    runTest(UnconfinedTestDispatcher()) {
      val expectedCount = 43
      val existingPackages = setOf("TestPackage1", "TestPackage2")
      val existingPackageInfos = existingPackages.map { PackageInstallInfo(it, 1223) }.toSet()
      whenever(dataCacheStorage.purgeAllEntitiesNotInPackages(existingPackages))
        .thenReturn(expectedCount)

      val result = testInstance.reconcile(existingPackageInfos)

      val expectedResult = PackageDeletionInfo(ChronicleAnalyticsClient.PECAN, expectedCount)
      assertThat(result).isEqualTo(expectedResult)
      verify(dataCacheStorage).purgeAllEntitiesNotInPackages(existingPackages)
      verifyNoMoreInteractions(dataCacheStorage)
    }
}
