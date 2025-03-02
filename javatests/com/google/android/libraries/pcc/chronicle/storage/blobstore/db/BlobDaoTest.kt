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

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.storage.blobstore.QuotaInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TrimOrder
import com.google.common.truth.Truth.assertThat
import java.lang.IllegalArgumentException
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlobDaoTest {
  private lateinit var context: Context
  private lateinit var db: BlobDatabase
  private lateinit var sqliteDb: SupportSQLiteDatabase
  private lateinit var dao: BlobDao

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
      createdTimestampMillis = CREATED_2,
      updateTimestampMillis = CREATED_2,
      dtdName = DTD_NAME_1,
      blob = "Test blob 4".toByteArray(),
    )
  private val blobKey1Dtd2 =
    BlobEntity(
      key = KEY_1,
      locusId = LOCUS_ID_3,
      createdTimestampMillis = CREATED_2,
      updateTimestampMillis = CREATED_2,
      dtdName = DTD_NAME_2,
      blob = "Test blob 3".toByteArray(),
    )

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db =
      Room.inMemoryDatabaseBuilder(context, BlobDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    sqliteDb = db.openHelper.writableDatabase
    dao = db.blobDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun insertBlobWithPackages_queryBlobWithPackagesByKeyAndDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
  }

  @Test
  fun insertBlobWithPackages_queryBlobWithPackagesByKeyAndDtdName_excludesExpiredEntityBlobKey1Dtd1() =
    runBlocking {
      dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
      dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
      val blobWithPackages1 =
        dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_2)
      val blobWithPackages2 =
        dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_2)

      assertThat(blobWithPackages1).isNull()
      checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    }

  @Test
  fun insertBlobsWithPackages_queryBlobEntitiesWithPackagesByDtdName() = runBlocking {
    dao.insertOrUpdateBlobsWithPackages(
      mapOf(
        blobKey1Dtd1 to listOf(PACKAGE_1),
        blobKey2Dtd1 to listOf(PACKAGE_1, PACKAGE_2),
        blobKey3Dtd1 to listOf(PACKAGE_3),
      ),
      blobKey1Dtd1.dtdName,
      QuotaInfo(maxRowCount = 3, minRowsAfterTrim = 2, trimOrder = TrimOrder.OLDEST),
      THRESHOLD_1,
    )

    assertThat(dao.blobEntitiesWithPackagesByDtdName(DTD_NAME_1, THRESHOLD_1)).hasSize(3)
  }

  @Test
  fun insertBlobsWithPackages_numberOfBlobsExceedsMaxRows_throwsException() = runBlocking {
    val thrown =
      assertFailsWith<IllegalArgumentException> {
        dao.insertOrUpdateBlobsWithPackages(
          mapOf(
            blobKey1Dtd1 to listOf(PACKAGE_1),
            blobKey2Dtd1 to listOf(PACKAGE_1, PACKAGE_2),
            blobKey3Dtd1 to listOf(PACKAGE_3),
          ),
          blobKey1Dtd1.dtdName,
          QuotaInfo(maxRowCount = 2, minRowsAfterTrim = 1, trimOrder = TrimOrder.OLDEST),
          THRESHOLD_1,
        )
      }

    assertThat(thrown)
      .hasMessageThat()
      .contains("Number of entities to insert exceeds quota limit.")
  }

  @Test
  fun insertBlobsWithPackages_trimAfterInsert() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1))
      .isNotNull()

    dao.insertOrUpdateBlobsWithPackages(
      mapOf(blobKey2Dtd1 to listOf(PACKAGE_1, PACKAGE_2), blobKey3Dtd1 to listOf(PACKAGE_3)),
      blobKey1Dtd1.dtdName,
      QuotaInfo(maxRowCount = 2, minRowsAfterTrim = 1, trimOrder = TrimOrder.OLDEST),
      THRESHOLD_1,
    )

    assertThat(dao.blobEntitiesWithPackagesByDtdName(DTD_NAME_1, THRESHOLD_1)).hasSize(1)
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, THRESHOLD_1))
      .isNotNull()
  }

  @Test
  fun insertBlobWithPackages_entryNotExpired_updateBlob() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)

    dao.insertOrUpdateBlobWithPackages(
      blobKey1Dtd1.copy(
        createdTimestampMillis = UPDATE_1,
        updateTimestampMillis = UPDATE_1,
        blob = "Test blob 3".toByteArray(),
      ),
      emptyList(),
      THRESHOLD_1,
    )

    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages4 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    assertThat(blobWithPackages3?.blobEntity?.id).isEqualTo(blobWithPackages3?.blobEntity?.id)
    checkBlob(
      blobKey1Dtd1.copy(updateTimestampMillis = UPDATE_1, blob = "Test blob 3".toByteArray()),
      listOf(PACKAGE_1),
      blobWithPackages3,
    )
    assertThat(blobWithPackages4).isEqualTo(blobWithPackages2)
  }

  @Test
  fun insertBlobWithPackages_entryExpired_insertNewBlobWithPackages() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(
      blobKey2Dtd1.copy(createdTimestampMillis = CREATED_1, updateTimestampMillis = CREATED_1),
      listOf(PACKAGE_1, PACKAGE_2),
      THRESHOLD_1,
    )
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(
      blobKey2Dtd1.copy(createdTimestampMillis = CREATED_1, updateTimestampMillis = CREATED_1),
      listOf(PACKAGE_1, PACKAGE_2),
      blobWithPackages2,
    )

    dao.insertOrUpdateBlobWithPackages(
      blobKey1Dtd1.copy(
        createdTimestampMillis = UPDATE_1,
        updateTimestampMillis = UPDATE_1,
        blob = "Test blob 3".toByteArray(),
      ),
      listOf(PACKAGE_1, PACKAGE_2),
      THRESHOLD_2,
    )
    dao.insertOrUpdateBlobWithPackages(
      blobKey2Dtd1.copy(createdTimestampMillis = UPDATE_1, updateTimestampMillis = UPDATE_1),
      listOf(PACKAGE_2),
      THRESHOLD_2,
    )

    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages4 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    assertThat(blobWithPackages3?.blobEntity?.id).isNotEqualTo(blobWithPackages1?.blobEntity?.id)
    assertThat(blobWithPackages4?.blobEntity?.id).isNotEqualTo(blobWithPackages2?.blobEntity?.id)

    checkBlob(
      blobKey1Dtd1.copy(
        createdTimestampMillis = UPDATE_1,
        updateTimestampMillis = UPDATE_1,
        blob = "Test blob 3".toByteArray(),
      ),
      listOf(PACKAGE_1, PACKAGE_2),
      blobWithPackages3,
    )
    checkBlob(
      blobKey2Dtd1.copy(createdTimestampMillis = UPDATE_1, updateTimestampMillis = UPDATE_1),
      listOf(PACKAGE_2),
      blobWithPackages4,
    )
  }

  @Test
  fun queryBlobEntityWithPackagesByKeyAndDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_3), THRESHOLD_1)
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)
    val blobWithPackages4 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_2, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    checkBlob(blobKey1Dtd2, listOf(PACKAGE_3), blobWithPackages3)
    assertThat(blobWithPackages4).isNull()
  }

  @Test
  fun queryBlobEntityWithPackagesByKeyAndDtdName_excludesExpiredEntityBlobKey1Dtd1() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_2)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_2)

    assertThat(blobWithPackages1).isNull()
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
  }

  @Test
  fun queryBlobEntitiesWithPackagesByDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_3), THRESHOLD_1)
    val blobsWithPackages1 = dao.blobEntitiesWithPackagesByDtdName(DTD_NAME_1, THRESHOLD_1)
    val blobsWithPackages2 = dao.blobEntitiesWithPackagesByDtdName(DTD_NAME_2, THRESHOLD_1)
    val blobsWithPackages3 = dao.blobEntitiesWithPackagesByDtdName(DTD_NAME_3, THRESHOLD_1)

    assertThat(blobsWithPackages1).hasSize(2)
    assertThat(blobsWithPackages2).hasSize(1)
    assertThat(blobsWithPackages3).isEmpty()
  }

  @Test
  fun queryBlobEntitiesWithPackagesByDtdName_excludesExpiredEntityBlobKey1Dtd1() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_3), THRESHOLD_1)
    val blobsWithPackages1 = dao.blobEntitiesWithPackagesByDtdName(DTD_NAME_1, THRESHOLD_2)
    val blobsWithPackages2 = dao.blobEntitiesWithPackagesByDtdName(DTD_NAME_2, THRESHOLD_2)

    assertThat(blobsWithPackages1).hasSize(1)
    assertThat(blobsWithPackages2).hasSize(1)
  }

  @Test
  fun queryBlobEntitiesWithPackagesByLocusIdAndDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(
      blobKey2Dtd1.copy(locusId = LOCUS_ID_1),
      listOf(PACKAGE_1, PACKAGE_2),
      THRESHOLD_1,
    )
    dao.insertOrUpdateBlobWithPackages(
      blobKey1Dtd2.copy(locusId = LOCUS_ID_1),
      listOf(PACKAGE_3),
      THRESHOLD_1,
    )
    val blobsWithPackages1 =
      dao.blobEntitiesWithPackagesByLocusIdAndDtdName(LOCUS_ID_1, DTD_NAME_1, THRESHOLD_1)
    val blobsWithPackages2 =
      dao.blobEntitiesWithPackagesByLocusIdAndDtdName(LOCUS_ID_1, DTD_NAME_2, THRESHOLD_1)
    val blobsWithPackages3 =
      dao.blobEntitiesWithPackagesByLocusIdAndDtdName(LOCUS_ID_1, DTD_NAME_3, THRESHOLD_1)

    assertThat(blobsWithPackages1).hasSize(2)
    assertThat(blobsWithPackages2).hasSize(1)
    assertThat(blobsWithPackages3).isEmpty()
  }

  @Test
  fun queryBlobEntitiesWithPackagesByLocusIdAndDtdName_excludesExpiredEntities() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(
      blobKey2Dtd1.copy(locusId = LOCUS_ID_1),
      listOf(PACKAGE_1, PACKAGE_2),
      THRESHOLD_1,
    )
    dao.insertOrUpdateBlobWithPackages(
      blobKey1Dtd2.copy(locusId = LOCUS_ID_1),
      listOf(PACKAGE_3),
      THRESHOLD_1,
    )
    val blobsWithPackages1 =
      dao.blobEntitiesWithPackagesByLocusIdAndDtdName(LOCUS_ID_1, DTD_NAME_1, THRESHOLD_2)
    val blobsWithPackages2 =
      dao.blobEntitiesWithPackagesByLocusIdAndDtdName(LOCUS_ID_1, DTD_NAME_2, THRESHOLD_2)

    assertThat(blobsWithPackages1).hasSize(1)
    assertThat(blobsWithPackages2).hasSize(1)
  }

  @Test
  fun queryBlobEntitiesWithPackagesByPackageNameAndDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_3), THRESHOLD_1)
    val packageBlobMap1 =
      dao.blobEntitiesWithPackagesByPackageNameAndDtdName(PACKAGE_1, DTD_NAME_1, THRESHOLD_1)
    val packageBlobMap2 =
      dao.blobEntitiesWithPackagesByPackageNameAndDtdName(PACKAGE_2, DTD_NAME_1, THRESHOLD_1)
    val packageBlobMap3 =
      dao.blobEntitiesWithPackagesByPackageNameAndDtdName(PACKAGE_1, DTD_NAME_3, THRESHOLD_1)

    val blobsWithPackagesList1 = packageBlobMap1.values.flatten()
    assertThat(blobsWithPackagesList1).hasSize(2)

    val blobsWithPackagesList2 = packageBlobMap2.values.flatten()
    assertThat(blobsWithPackagesList2).hasSize(1)

    val blobsWithPackagesList3 = packageBlobMap3.values.flatten()
    assertThat(blobsWithPackagesList3).isEmpty()
  }

  @Test
  fun queryBlobEntitiesWithPackagesByPackageNameAndDtdName_excludesExpiredEntities() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_3), THRESHOLD_1)
    val packageBlobMap1 =
      dao.blobEntitiesWithPackagesByPackageNameAndDtdName(PACKAGE_1, DTD_NAME_1, THRESHOLD_2)
    val packageBlobMap2 =
      dao.blobEntitiesWithPackagesByPackageNameAndDtdName(PACKAGE_2, DTD_NAME_1, THRESHOLD_2)

    val blobsWithPackagesList1 = packageBlobMap1.values.flatten()
    assertThat(blobsWithPackagesList1).hasSize(1)

    val blobsWithPackagesList2 = packageBlobMap2.values.flatten()
    assertThat(blobsWithPackagesList2).hasSize(1)
  }

  @Test
  fun queryPackagesByPackageName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_3), THRESHOLD_1)
    val packages1 = dao.packagesByPackageName(PACKAGE_1)
    val packages2 = dao.packagesByPackageName(PACKAGE_2)
    val packages3 = dao.packagesByPackageName(PACKAGE_4)

    assertThat(packages1).hasSize(2)
    assertThat(packages2).hasSize(1)
    assertThat(packages3).isEmpty()
  }

  @Test
  fun queryPackagesByBlobId() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_3), THRESHOLD_1)
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)
    val packages1 = blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }
    val packages2 = blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }
    val packages3 = blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it + 1) }

    assertThat(packages1).hasSize(1)
    assertThat(packages2).hasSize(2)
    assertThat(packages3).isEmpty()
  }

  @Test
  fun removeBlobEntityById() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)

    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)

    blobWithPackages1?.blobEntity?.id?.let { dao.removeBlobEntityById(it) }
    blobWithPackages2?.blobEntity?.id?.let { dao.removeBlobEntityById(it) }

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
  }

  @Test
  fun removeBlobEntityByKeyAndDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)

    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)

    dao.removeBlobEntityByKeyAndDtdName(KEY_1, DTD_NAME_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1))
      .isNotNull()
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
  }

  @Test
  fun removeBlobAndPackageEntitiesByPackageName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)

    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)

    dao.removeBlobAndPackageEntitiesByPackageName(PACKAGE_3)

    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages4 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages3)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages4)
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages4?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)

    dao.removeBlobAndPackageEntitiesByPackageName(PACKAGE_2)

    val blobWithPackages5 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages5)
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(blobWithPackages5?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages4?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()

    dao.removeBlobAndPackageEntitiesByPackageName(PACKAGE_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(blobWithPackages5?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
  }

  @Test
  fun removeBlobEntitiesByDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_2), THRESHOLD_1)

    // Double checking inserts here though it's redundant to make sure entries are in the db before
    // being deleted.
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    checkBlob(blobKey1Dtd2, listOf(PACKAGE_2), blobWithPackages3)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)

    dao.removeBlobEntitiesByDtdName(blobKey1Dtd1.dtdName)

    val blobWithPackages4 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    checkBlob(blobKey1Dtd2, listOf(PACKAGE_2), blobWithPackages4)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages4?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
  }

  @Test
  fun removeAllBlobEntities() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_2), THRESHOLD_1)

    // Double checking inserts here though it's redundant to make sure entries are in the db before
    // being deleted.
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    checkBlob(blobKey1Dtd2, listOf(PACKAGE_2), blobWithPackages3)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)

    dao.removeAllBlobEntities()

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)).isNull()
  }

  @Test
  fun removeExpiredBlobEntitiesByDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)

    // Double checking inserts here though it's redundant to make sure entries are in the db before
    // being deleted.
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)

    dao.removeExpiredBlobEntitiesByDtdName(DTD_NAME_1, THRESHOLD_2)

    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages3)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
  }

  @Test
  fun removeBlobEntitiesBetween() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd2, listOf(PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_2), THRESHOLD_1)

    // Double checking inserts here though it's redundant to make sure entries are in the db before
    // being deleted.
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)
    val blobWithPackages4 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    checkBlob(blobKey1Dtd2, listOf(PACKAGE_2), blobWithPackages3)
    checkBlob(blobKey3Dtd1, listOf(PACKAGE_2), blobWithPackages4)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages4?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)

    dao.removeBlobEntitiesCreatedBetween(CREATED_1 - 100, CREATED_1 + 100)

    val blobWithPackages5 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages6 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_2, THRESHOLD_1)
    val blobWithPackages7 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, THRESHOLD_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages5)
    checkBlob(blobKey1Dtd2, listOf(PACKAGE_2), blobWithPackages6)
    checkBlob(blobKey3Dtd1, listOf(PACKAGE_2), blobWithPackages7)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages5?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
    assertThat(blobWithPackages6?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages7?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
  }

  @Test
  fun removeOldestBlobEntitiesByDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)

    // Double checking inserts here though it's redundant to make sure entries are in the db before
    // being deleted.
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)

    dao.removeOldestBlobEntitiesByDtdName(DTD_NAME_1, 1)

    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)).isNull()
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages3)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
  }

  @Test
  fun removeNewestBlobEntitiesByDtdName() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)

    // Double checking inserts here though it's redundant to make sure entries are in the db before
    // being deleted.
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)

    dao.removeNewestBlobEntitiesByDtdName(DTD_NAME_1, 1)

    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)

    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)).isNull()
    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages3)
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
  }

  @Test
  fun deleteNotAllowedPackages() = runBlocking {
    dao.insertOrUpdateBlobWithPackages(blobKey1Dtd1, listOf(PACKAGE_1), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), THRESHOLD_1)
    dao.insertOrUpdateBlobWithPackages(blobKey3Dtd1, listOf(PACKAGE_2), THRESHOLD_1)

    // Double checking inserts here though it's redundant to make sure entries are in the db before
    // being deleted.
    val blobWithPackages1 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages2 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)
    val blobWithPackages3 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages1)
    checkBlob(blobKey2Dtd1, listOf(PACKAGE_1, PACKAGE_2), blobWithPackages2)
    checkBlob(blobKey3Dtd1, listOf(PACKAGE_2), blobWithPackages3)
    assertThat(blobWithPackages1?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(2)
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)

    dao.deleteNotAllowedPackages(allowedPackages = setOf(PACKAGE_1))

    val blobWithPackages4 =
      dao.blobEntityWithPackagesByKeyAndDtdName(KEY_1, DTD_NAME_1, THRESHOLD_1)

    checkBlob(blobKey1Dtd1, listOf(PACKAGE_1), blobWithPackages4)
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_2, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(dao.blobEntityWithPackagesByKeyAndDtdName(KEY_3, DTD_NAME_1, THRESHOLD_1)).isNull()
    assertThat(blobWithPackages4?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).hasSize(1)
    assertThat(blobWithPackages2?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
    assertThat(blobWithPackages3?.blobEntity?.id?.let { dao.packagesByBlobId(it) }).isEmpty()
  }

  private fun checkBlob(
    expectedBlob: BlobEntity,
    expectedPackages: List<String>,
    result: BlobEntityWithPackages?,
  ) {
    with(result!!.blobEntity) {
      assertThat(key).isEqualTo(expectedBlob.key)
      assertThat(locusId).isEqualTo(expectedBlob.locusId)
      assertThat(createdTimestampMillis).isEqualTo(expectedBlob.createdTimestampMillis)
      assertThat(updateTimestampMillis).isEqualTo(expectedBlob.updateTimestampMillis)
      assertThat(dtdName).isEqualTo(expectedBlob.dtdName)
      assertThat(blob).isEqualTo(expectedBlob.blob)
    }
    val packagesResult = result.packages.map { it.packageName }
    assertThat(packagesResult).containsExactlyElementsIn(expectedPackages)
  }

  companion object {
    private const val CREATED_1 = 1000L
    private const val CREATED_2 = 2000L
    private const val UPDATE_1 = 1700L
    private const val THRESHOLD_1 = 500L
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
