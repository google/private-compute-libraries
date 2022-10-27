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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.PackageDeletionListener
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import com.google.android.libraries.pcc.chronicle.storage.blobstore.inmemory.InMemoryBlobStore
import com.google.android.libraries.pcc.chronicle.storage.blobstore.persisted.PersistedBlobStore
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.lang.IllegalArgumentException
import java.time.Instant
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlobStoreIntegrationTest {
  private lateinit var blobStoreProvider: BlobStoreProvider
  private lateinit var blobStoreManager: BlobStoreManager
  private lateinit var testMessagePersistedBlobStore: BlobStore<TestMessage>
  private lateinit var testPersonPersistedBlobStore: BlobStore<TestPerson>
  private lateinit var testPlaceInMemoryBlobStore: BlobStore<TestPlace>
  private lateinit var testShapeInMemoryBlobStore: BlobStore<TestShape>
  private val context: Context = ApplicationProvider.getApplicationContext()

  private val testMessage =
    TestMessage.newBuilder()
      .apply {
        version = VERSION
        name = NAME
        content = CONTENT
      }
      .build()

  private val testPerson =
    TestPerson.newBuilder()
      .apply {
        firstName = FIRST_NAME
        lastName = LAST_NAME
        age = AGE
      }
      .build()

  private val testPlace =
    TestPlace.newBuilder()
      .apply {
        city = CITY
        country = COUNTRY
      }
      .build()

  private val testShape =
    TestShape.newBuilder()
      .apply {
        shape = SHAPE
        color = COLOR
      }
      .build()

  private val wrapped1 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_1,
        associatedPackageNames = listOf(PACKAGE_1, PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testMessage
    )

  private val wrapped2 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_2,
        associatedPackageNames = listOf(PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testMessage
    )

  private val wrapped3 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_3,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testMessage
    )

  private val wrapped4 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_4,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testMessage
    )

  private val wrapped5 =
    WrappedEntity<TestPerson>(
      EntityMetadata(
        id = KEY_1,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testPerson
    )

  private val wrapped6 =
    WrappedEntity<TestPerson>(
      EntityMetadata(
        id = KEY_2,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testPerson
    )

  private val wrapped7 =
    WrappedEntity<TestPerson>(
      EntityMetadata(
        id = KEY_3,
        associatedPackageNames = listOf(PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testPerson
    )

  private val wrapped8 =
    WrappedEntity<TestPlace>(
      EntityMetadata(
        id = KEY_1,
        associatedPackageNames = listOf(PACKAGE_1, PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testPlace
    )

  private val wrapped9 =
    WrappedEntity<TestPlace>(
      EntityMetadata(
        id = KEY_2,
        associatedPackageNames = listOf(PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testPlace
    )

  private val wrapped10 =
    WrappedEntity<TestPlace>(
      EntityMetadata(
        id = KEY_3,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testPlace
    )

  private val wrapped11 =
    WrappedEntity<TestPlace>(
      EntityMetadata(
        id = KEY_4,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testPlace
    )

  private val wrapped12 =
    WrappedEntity<TestShape>(
      EntityMetadata(
        id = KEY_1,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testShape
    )

  private val wrapped13 =
    WrappedEntity<TestShape>(
      EntityMetadata(
        id = KEY_2,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testShape
    )

  private val wrapped14 =
    WrappedEntity<TestShape>(
      EntityMetadata(
        id = KEY_3,
        associatedPackageNames = listOf(PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED_1),
        updated = Instant.ofEpochMilli(CREATED_1)
      ),
      testShape
    )

  private var fakeTime = Instant.ofEpochMilli(CREATED_1)
  private val timeSource = TimeSource { fakeTime }

  @Before
  fun setUp() = runBlocking {
    val blobStoreCore = BlobStoreCore(context, timeSource)
    blobStoreProvider = blobStoreCore
    blobStoreManager = blobStoreCore.provideManager()
    testMessagePersistedBlobStore =
      blobStoreProvider.provideBlobStore<TestMessage>(
        PersistedManagementInfo<TestMessage>(
          dtdName = DTD_MESSAGE,
          ttlMillis = 200,
          quotaInfo = QuotaInfo(3, 2, TrimOrder.OLDEST),
          deserializer = TestMessage::parseFrom
        )
      )
    testPersonPersistedBlobStore =
      blobStoreProvider.provideBlobStore<TestPerson>(
        PersistedManagementInfo<TestPerson>(
          dtdName = DTD_PERSON,
          ttlMillis = 500,
          quotaInfo = QuotaInfo(2, 1, TrimOrder.OLDEST),
          deserializer = TestPerson::parseFrom
        )
      )

    testPlaceInMemoryBlobStore =
      blobStoreProvider.provideBlobStore<TestPlace>(
        InMemoryManagementInfo(dtdName = DTD_PLACE, ttlMillis = 220, maxItems = 3)
      )

    testShapeInMemoryBlobStore =
      blobStoreProvider.provideBlobStore<TestShape>(
        InMemoryManagementInfo(dtdName = DTD_SHAPE, ttlMillis = 600, maxItems = 2)
      )
  }

  @Test
  fun insertAndGetDataFromBlobStores() = runBlocking {
    testMessagePersistedBlobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))
    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPersonPersistedBlobStore.putEntities(listOf(wrapped5, wrapped6))
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped8, wrapped9, wrapped10))
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped12, wrapped13))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)

    checkTestMessage(
      testMessagePersistedBlobStore.getEntityByKey(KEY_1),
      KEY_1,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_1, PACKAGE_2),
      testMessage
    )
    checkTestPerson(
      testPersonPersistedBlobStore.getEntityByKey(KEY_1),
      KEY_1,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_1),
      testPerson
    )
    checkTestPlace(
      testPlaceInMemoryBlobStore.getEntityByKey(KEY_1),
      KEY_1,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_1, PACKAGE_2),
      testPlace
    )
    checkTestShape(
      testShapeInMemoryBlobStore.getEntityByKey(KEY_1),
      KEY_1,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_1),
      testShape
    )
  }

  @Test
  fun insertAndRemoveSingleEntityFromBlobStores() = runBlocking {
    testMessagePersistedBlobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))
    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPersonPersistedBlobStore.putEntities(listOf(wrapped5, wrapped6))
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped8, wrapped9, wrapped10))
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped12, wrapped13))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)

    testMessagePersistedBlobStore.removeEntityByKey(KEY_1)
    testPersonPersistedBlobStore.removeEntityByKey(KEY_1)
    testPlaceInMemoryBlobStore.removeEntityByKey(KEY_1)
    testShapeInMemoryBlobStore.removeEntityByKey(KEY_1)

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(2)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(2)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(1)
    assertThat(testMessagePersistedBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(testPersonPersistedBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(testPlaceInMemoryBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(testShapeInMemoryBlobStore.getEntityByKey(KEY_1)).isNull()
  }

  @Test
  fun insertAndRemoveAllEntityFromBlobStores() = runBlocking {
    testMessagePersistedBlobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))
    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPersonPersistedBlobStore.putEntities(listOf(wrapped5, wrapped6))
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped8, wrapped9, wrapped10))
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped12, wrapped13))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)

    testMessagePersistedBlobStore.removeAll()
    testPersonPersistedBlobStore.removeAll()
    testPlaceInMemoryBlobStore.removeAll()
    testShapeInMemoryBlobStore.removeAll()

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(0)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(0)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(0)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(0)
  }

  @Test
  fun performMaintenance() = runBlocking {
    testMessagePersistedBlobStore.putEntity(wrapped1)
    testPlaceInMemoryBlobStore.putEntity(wrapped8)

    fakeTime = fakeTime.plusMillis(50)

    testPersonPersistedBlobStore.putEntity(wrapped5)
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped12, wrapped13))

    fakeTime = fakeTime.plusMillis(50)

    testMessagePersistedBlobStore.putEntities(listOf(wrapped2, wrapped3))
    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped9, wrapped10))
    testPersonPersistedBlobStore.putEntity(wrapped6)
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped14))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    // Shape blobstore has maxItems = 2, and 3 wrapped entities have been inserted, so the first
    // (wrapped12 with key = KEY_1) has been removed already.
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)
    assertThat(testShapeInMemoryBlobStore.getEntityByKey(KEY_1)).isNull()

    fakeTime = fakeTime.plusMillis(140)

    /**
     * Calling perform maintenance will first delete expired entities based on the entity's type's
     * ttl. For persisted BlobStores, perform maintenance will then trim down the number of stored
     * entities of a type if the number of entities exceeds the type's quota.
     *
     * Wrapped1 (ttl = 200) and wrapped8 (ttl = 220) were inserted at 1000. Wrapped5 (ttl = 500) and
     * wrapped12 & wrapped 13 (ttl = 600) were inserted at 1050. Wrapped12 has already been deleted
     * due to 3 inserts into shape blobstore, but maxItems = 2. The rest were inserted at 1100. The
     * current time is 1240. So, the only expired entities are wrapped1 and wrapped8. This brings
     * message blobstore size = 3, person blobstore size = 3, place blobstore size = 2, and shape
     * blobstore size = 2.
     *
     * Now trimming for persisted stores happens. The quota limit for type message blobstore is 3 so
     * no trimming will occur for this type. The quota limit for type person blobstore is 2, so
     * trimming will occur down to the minRowsAfterTrim of 1 where oldest entities are deleted
     * first. So, the oldest two entities in person blobstore will trimmed leaving one entity
     * remaining.
     */
    blobStoreManager.performMaintenance()

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(1)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(2)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)
    assertThat(testMessagePersistedBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(testPersonPersistedBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(testPersonPersistedBlobStore.getEntityByKey(KEY_2)).isNull()
    assertThat(testPlaceInMemoryBlobStore.getEntityByKey(KEY_1)).isNull()
    checkTestMessage(
      testMessagePersistedBlobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_2),
      testMessage
    )
    checkTestMessage(
      testMessagePersistedBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_1),
      testMessage
    )
    checkTestMessage(
      testMessagePersistedBlobStore.getEntityByKey(KEY_4),
      KEY_4,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_1),
      testMessage
    )
    checkTestPerson(
      testPersonPersistedBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_2),
      testPerson
    )
    checkTestPlace(
      testPlaceInMemoryBlobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_2),
      testPlace
    )
    checkTestPlace(
      testPlaceInMemoryBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_1),
      testPlace
    )
    checkTestShape(
      testShapeInMemoryBlobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED_2,
      CREATED_2,
      listOf(PACKAGE_1),
      testShape
    )
    checkTestShape(
      testShapeInMemoryBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_2),
      testShape
    )
  }

  @Test
  fun deleteFor() = runBlocking {
    testMessagePersistedBlobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))
    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPersonPersistedBlobStore.putEntities(listOf(wrapped5, wrapped6))
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped8, wrapped9, wrapped10))
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped13, wrapped14))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)

    blobStoreManager.deleteFor(PackageDeletionListener.PackageInstallInfo(PACKAGE_1, 1))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(1)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(1)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(1)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(1)
    checkTestMessage(
      testMessagePersistedBlobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testMessage
    )
    checkTestPerson(
      testPersonPersistedBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testPerson
    )
    checkTestPlace(
      testPlaceInMemoryBlobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testPlace
    )
    checkTestShape(
      testShapeInMemoryBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testShape
    )
  }

  @Test
  fun reconcile() = runBlocking {
    testMessagePersistedBlobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))
    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPersonPersistedBlobStore.putEntities(listOf(wrapped5, wrapped6))
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped8, wrapped9, wrapped10))
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped13, wrapped14))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)

    blobStoreManager.reconcile(setOf(PackageDeletionListener.PackageInstallInfo(PACKAGE_2, 1)))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(1)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(1)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(1)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(1)
    checkTestMessage(
      testMessagePersistedBlobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testMessage
    )
    checkTestPerson(
      testPersonPersistedBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testPerson
    )
    checkTestPlace(
      testPlaceInMemoryBlobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testPlace
    )
    checkTestShape(
      testShapeInMemoryBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_1,
      CREATED_1,
      listOf(PACKAGE_2),
      testShape
    )
  }

  @Test
  fun clearDataCreatedBetween() = runBlocking {
    testMessagePersistedBlobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped9, wrapped10))

    fakeTime = fakeTime.plusMillis(50)

    testPersonPersistedBlobStore.putEntities(listOf(wrapped5, wrapped6))
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped12, wrapped13))

    fakeTime = fakeTime.plusMillis(50)

    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPlaceInMemoryBlobStore.putEntity(wrapped11)
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testShapeInMemoryBlobStore.putEntity(wrapped14)

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)

    blobStoreManager.clearDataCreatedBetween(CREATED_1.minus(100), CREATED_1.plus(75))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(1)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(1)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(1)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(1)
    checkTestMessage(
      testMessagePersistedBlobStore.getEntityByKey(KEY_4),
      KEY_4,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_1),
      testMessage
    )
    checkTestPerson(
      testPersonPersistedBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_2),
      testPerson
    )
    checkTestPlace(
      testPlaceInMemoryBlobStore.getEntityByKey(KEY_4),
      KEY_4,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_1),
      testPlace
    )
    checkTestShape(
      testShapeInMemoryBlobStore.getEntityByKey(KEY_3),
      KEY_3,
      CREATED_3,
      CREATED_3,
      listOf(PACKAGE_2),
      testShape
    )
  }

  @Test
  fun clearAll() = runBlocking {
    testMessagePersistedBlobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))
    testMessagePersistedBlobStore.putEntity(wrapped4)
    testPersonPersistedBlobStore.putEntities(listOf(wrapped5, wrapped6))
    testPersonPersistedBlobStore.putEntity(wrapped7)
    testPlaceInMemoryBlobStore.putEntities(listOf(wrapped9, wrapped10, wrapped11))
    testShapeInMemoryBlobStore.putEntities(listOf(wrapped13, wrapped14))

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(4)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(3)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(3)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(2)

    blobStoreManager.clearAllData()

    assertThat(testMessagePersistedBlobStore.getAllEntities()).hasSize(0)
    assertThat(testPersonPersistedBlobStore.getAllEntities()).hasSize(0)
    assertThat(testPlaceInMemoryBlobStore.getAllEntities()).hasSize(0)
    assertThat(testShapeInMemoryBlobStore.getAllEntities()).hasSize(0)
  }

  @Test
  fun provideBlobstore_differentManagementInfoButSameType_returnsBlobStore() = runBlocking {
    val persistedResult =
      blobStoreProvider.provideBlobStore<TestPerson>(
        PersistedManagementInfo<TestPerson>(
          dtdName = DTD_PERSON,
          ttlMillis = 10000,
          quotaInfo = QuotaInfo(2, 1, TrimOrder.OLDEST),
          deserializer = TestPerson::parseFrom
        )
      )
    val memoryResult =
      blobStoreProvider.provideBlobStore<TestPlace>(
        InMemoryManagementInfo(dtdName = DTD_PLACE, ttlMillis = 10000, maxItems = 3)
      )

    assertThat(persistedResult is PersistedBlobStore<TestPerson>).isTrue()
    assertThat(memoryResult is InMemoryBlobStore<TestPlace>).isTrue()
  }

  @Test
  fun provideBlobstore_differentManagementInfoOfDifferentType_throwsException() = runBlocking {
    val thrown1 =
      assertFailsWith<IllegalArgumentException> {
        blobStoreProvider.provideBlobStore<TestPerson>(
          InMemoryManagementInfo(dtdName = DTD_PERSON, ttlMillis = 10000, maxItems = 3)
        )
      }
    val thrown2 =
      assertFailsWith<IllegalArgumentException> {
        blobStoreProvider.provideBlobStore<TestPlace>(
          PersistedManagementInfo<TestPerson>(
            dtdName = DTD_PLACE,
            ttlMillis = 10000,
            quotaInfo = QuotaInfo(2, 1, TrimOrder.OLDEST),
            deserializer = TestPerson::parseFrom
          )
        )
      }

    assertThat(thrown1)
      .hasMessageThat()
      .contains("Persisted and in memory blob stores for the same DTD is not allowed.")
    assertThat(thrown2)
      .hasMessageThat()
      .contains("Persisted and in memory blob stores for the same DTD is not allowed.")
  }

  @Test
  fun provideBlobstore_illegalManagementInfo_throwsError() = runBlocking {
    val thrown =
      assertFailsWith<IllegalArgumentException> {
        blobStoreProvider.provideBlobStore<TestPlace>(
          PersistedManagementInfo<TestPerson>(
            dtdName = DTD_PLACE,
            ttlMillis = 10000,
            quotaInfo = QuotaInfo(2, 2, TrimOrder.OLDEST),
            deserializer = TestPerson::parseFrom
          )
        )
      }

    assertThat(thrown)
      .hasMessageThat()
      .contains("maxRowCount must be greater than minRowsAfterTrim.")
  }

  private fun checkTestMessage(
    result: WrappedEntity<TestMessage>?,
    key: String,
    created: Long,
    updated: Long,
    packages: List<String>,
    testMessage: TestMessage
  ) {
    assertThat(result?.metadata?.id).isEqualTo(key)
    assertThat(result?.metadata?.created?.toInstant()?.toEpochMilli()).isEqualTo(created)
    assertThat(result?.metadata?.updated?.toInstant()?.toEpochMilli()).isEqualTo(updated)
    assertThat(result?.metadata?.associatedPackageNamesList).containsExactlyElementsIn(packages)
    assertThat(result?.entity).isEqualTo(testMessage)
  }

  private fun checkTestPerson(
    result: WrappedEntity<TestPerson>?,
    key: String,
    created: Long,
    updated: Long,
    packages: List<String>,
    testPerson: TestPerson
  ) {
    assertThat(result?.metadata?.id).isEqualTo(key)
    assertThat(result?.metadata?.created?.toInstant()?.toEpochMilli()).isEqualTo(created)
    assertThat(result?.metadata?.updated?.toInstant()?.toEpochMilli()).isEqualTo(updated)
    assertThat(result?.metadata?.associatedPackageNamesList).containsExactlyElementsIn(packages)
    assertThat(result?.entity).isEqualTo(testPerson)
  }

  private fun checkTestPlace(
    result: WrappedEntity<TestPlace>?,
    key: String,
    created: Long,
    updated: Long,
    packages: List<String>,
    testPlace: TestPlace
  ) {
    assertThat(result?.metadata?.id).isEqualTo(key)
    assertThat(result?.metadata?.created?.toInstant()?.toEpochMilli()).isEqualTo(created)
    assertThat(result?.metadata?.updated?.toInstant()?.toEpochMilli()).isEqualTo(updated)
    assertThat(result?.metadata?.associatedPackageNamesList).containsExactlyElementsIn(packages)
    assertThat(result?.entity).isEqualTo(testPlace)
  }

  private fun checkTestShape(
    result: WrappedEntity<TestShape>?,
    key: String,
    created: Long,
    updated: Long,
    packages: List<String>,
    testShape: TestShape
  ) {
    assertThat(result?.metadata?.id).isEqualTo(key)
    assertThat(result?.metadata?.created?.toInstant()?.toEpochMilli()).isEqualTo(created)
    assertThat(result?.metadata?.updated?.toInstant()?.toEpochMilli()).isEqualTo(updated)
    assertThat(result?.metadata?.associatedPackageNamesList).containsExactlyElementsIn(packages)
    assertThat(result?.entity).isEqualTo(testShape)
  }

  companion object {
    private const val KEY_1 = "key_1"
    private const val KEY_2 = "key_2"
    private const val KEY_3 = "key_3"
    private const val KEY_4 = "key_4"
    private const val PACKAGE_1 = "package_1"
    private const val PACKAGE_2 = "package_2"
    private val DTD_MESSAGE = TestMessage::class.java.toString()
    private val DTD_PERSON = TestPerson::class.java.toString()
    private val DTD_PLACE = TestPlace::class.java.toString()
    private val DTD_SHAPE = TestShape::class.java.toString()
    private const val CREATED_1 = 1000L
    private const val CREATED_2 = 1050L
    private const val CREATED_3 = 1100L
    private const val VERSION = 1L
    private const val NAME = "test_1"
    private const val CONTENT = "this is a test"
    private const val FIRST_NAME = "John"
    private const val LAST_NAME = "Doe"
    private const val AGE = 30
    private const val CITY = "Seattle"
    private const val COUNTRY = "USA"
    private const val SHAPE = "circle"
    private const val COLOR = "red"
  }
}
