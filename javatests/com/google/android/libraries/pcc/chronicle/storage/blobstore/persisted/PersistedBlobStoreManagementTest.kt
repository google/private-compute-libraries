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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStoreManagement
import com.google.android.libraries.pcc.chronicle.storage.blobstore.PersistedManagementInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.QuotaInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TestMessage
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TestPerson
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TrimOrder
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobDao
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobDatabase
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobEntity
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistedBlobStoreManagementTest {
  private val context: Context = ApplicationProvider.getApplicationContext()
  private lateinit var dao: BlobDao
  private lateinit var blobStorePersistedManagement: BlobStoreManagement

  private val blobKey1Dtd1 =
    BlobEntity(
      key = KEY_1,
      locusId = LOCUS_ID_1,
      createdTimestampMillis = CREATED_1,
      updateTimestampMillis = CREATED_1,
      dtdName = DTD_NAME_1,
      blob = "Test blob 1".toByteArray(),
    )
  private val blobKey2Dtd1 =
    BlobEntity(
      key = KEY_2,
      locusId = LOCUS_ID_2,
      createdTimestampMillis = CREATED_2,
      updateTimestampMillis = CREATED_2,
      dtdName = DTD_NAME_1,
      blob = "Test blob 2".toByteArray(),
    )
  private val blobKey3Dtd1 =
    BlobEntity(
      key = KEY_3,
      locusId = LOCUS_ID_4,
      createdTimestampMillis = CREATED_3,
      updateTimestampMillis = CREATED_3,
      dtdName = DTD_NAME_1,
      blob = "Test blob 4".toByteArray(),
    )
  private val blobKey1Dtd2 =
    BlobEntity(
      key = KEY_1,
      locusId = LOCUS_ID_3,
      createdTimestampMillis = CREATED_1,
      updateTimestampMillis = CREATED_1,
      dtdName = DTD_NAME_2,
      blob = "Test blob 3".toByteArray(),
    )

  private val managementInfo1 =
    PersistedManagementInfo<TestMessage>(
      dtdName = DTD_NAME_1,
      ttlMillis = 1500,
      QuotaInfo(3, 1, TrimOrder.OLDEST),
      TestMessage::parseFrom
    )
  private val managementInfo2 =
    PersistedManagementInfo<TestPerson>(
      dtdName = DTD_NAME_2,
      ttlMillis = 2500,
      QuotaInfo(3, 1, TrimOrder.NEWEST),
      TestPerson::parseFrom
    )

  @Before
  fun setUp() {
    val db =
      Room.databaseBuilder(context, BlobDatabase::class.java, "BlobStore")
        .setQueryExecutor(Executors.newSingleThreadExecutor())
        .setTransactionExecutor(Executors.newSingleThreadExecutor())
        .build()
    dao = db.blobDao()
    blobStorePersistedManagement = PersistedBlobStoreManagement(dao)
  }

  @Test
  fun clearAll() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    val result = blobStorePersistedManagement.clearAll()

    assertThat(result).isEqualTo(4)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(0)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(0)
  }

  @Test
  fun deleteExpiredEntitiesOfType() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(2)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    blobStorePersistedManagement.deleteExpiredEntities(
      CREATED_3,
      setOf(managementInfo1, managementInfo2)
    )

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(1)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)
  }

  @Test
  fun deleteEntitiesBetween() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    val result =
      blobStorePersistedManagement.deleteEntitiesCreatedBetween(CREATED_1 - 100, CREATED_1 + 100)

    assertThat(result).isEqualTo(2)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(2)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(0)
  }

  @Test
  fun trimOfType_oldest() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    blobStorePersistedManagement.trim(setOf(managementInfo1, managementInfo2))

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    blobStorePersistedManagement.trim(
      setOf(managementInfo1.copy(quotaInfo = QuotaInfo(2, 1, TrimOrder.OLDEST)), managementInfo2)
    )

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, 0)).isNotNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, 0)).isNotNull()
  }

  @Test
  fun trimOfType_newest() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    blobStorePersistedManagement.trim(
      setOf(managementInfo1.copy(quotaInfo = QuotaInfo(3, 1, TrimOrder.NEWEST)), managementInfo2)
    )

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    blobStorePersistedManagement.trim(
      setOf(managementInfo1.copy(quotaInfo = QuotaInfo(2, 1, TrimOrder.NEWEST)), managementInfo2)
    )

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, 0)).isNotNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, 0)).isNotNull()
  }

  @Test
  fun deletePackage() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    val result = blobStorePersistedManagement.deletePackage(PACKAGE_1)

    assertThat(result).isEqualTo(3)
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, 0)).isNotNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, 0)).isNull()
  }

  @Test
  fun reconcilePackages() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.countBlobsByDtdName(blobKey1Dtd1.dtdName)).isEqualTo(3)
    assertThat(dao.countBlobsByDtdName(blobKey1Dtd2.dtdName)).isEqualTo(1)

    // This should delete an entity if ANY of its packages are deleted. So, since entities with id2
    // and id3 both have packages that are not in the allowed list, even though entity with id2 also
    // has a package in the allowed list, they should both be deleted.
    val result = blobStorePersistedManagement.reconcilePackages(setOf(PACKAGE_1))

    assertThat(result).isEqualTo(2)
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, 0)).isNotNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, 0)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, 0)).isNotNull()
  }

  companion object {
    private const val CREATED_1 = 10000L
    private const val CREATED_2 = 11000L
    private const val CREATED_3 = 12000L
    private const val THRESHOLD_1 = 5000L
    private const val THRESHOLD_2 = 1500L
    private const val KEY_1 = "blob_key_1"
    private const val KEY_2 = "blob_key_2"
    private const val KEY_3 = "blob_key_3"
    private const val DTD_NAME_1 = "test_dtd_1"
    private const val DTD_NAME_2 = "test_dtd_2"
    private const val DTD_NAME_3 = "test_dtd_3"
    private const val LOCUS_ID_1 = "locus_id_1"
    private const val LOCUS_ID_2 = "locus_id_2"
    private const val LOCUS_ID_3 = "locus_id_3"
    private const val LOCUS_ID_4 = "locus_id_4"
    private const val PACKAGE_1 = "package_1"
    private const val PACKAGE_2 = "package_2"
    private const val PACKAGE_3 = "package_3"
    private const val PACKAGE_4 = "package_4"
  }
}
