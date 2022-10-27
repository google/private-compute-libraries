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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStore
import com.google.android.libraries.pcc.chronicle.storage.blobstore.InMemoryManagementInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TestMessage
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InMemoryBlobStoreTest {
  private lateinit var blobStore: BlobStore<TestMessage>
  private val inMemoryStorage = InMemoryStorage()

  private val testMessage =
    TestMessage.newBuilder()
      .apply {
        version = VERSION
        name = NAME
        content = CONTENT
      }
      .build()

  private val wrapped1 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_1,
        associatedPackageNames = listOf(PACKAGE_1, PACKAGE_2),
        created = Instant.ofEpochMilli(999),
        updated = Instant.ofEpochMilli(999)
      ),
      testMessage
    )

  private val wrapped2 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_2,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(999),
        updated = Instant.ofEpochMilli(999)
      ),
      testMessage
    )

  private val wrapped3 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_3,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(999),
        updated = Instant.ofEpochMilli(999)
      ),
      testMessage
    )

  private var fakeTime = Instant.ofEpochMilli(CREATED)
  private val timeSource = TimeSource { fakeTime }

  @Before
  fun setUp() {
    blobStore =
      InMemoryBlobStore(
        inMemoryStorage.registerDataTypeStore(InMemoryManagementInfo(DTD, 500, 2)),
        timeSource
      )
  }

  @Test
  fun insertEntities_entityNotExpired_updateEntity() = runBlocking {
    blobStore.putEntities(listOf(wrapped1, wrapped2))

    var results = blobStore.getAllEntities()

    assertThat(results).hasSize(2)
    checkResult(
      blobStore.getEntityByKey(KEY_1),
      KEY_1,
      CREATED,
      CREATED,
      listOf(PACKAGE_1, PACKAGE_2),
      testMessage
    )
    checkResult(
      blobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED,
      CREATED,
      listOf(PACKAGE_1),
      testMessage
    )

    fakeTime = fakeTime.plusMillis(250)

    val message =
      TestMessage.newBuilder()
        .apply {
          version = 2L
          name = "updated_name"
          content = "updated_content"
        }
        .build()

    blobStore.putEntity(
      WrappedEntity(
        metadata =
          EntityMetadata(
            id = KEY_1,
            associatedPackageNames = listOf(PACKAGE_1),
            created = Instant.ofEpochMilli(1001),
            updated = Instant.ofEpochMilli(1001)
          ),
        entity = message
      )
    )

    results = blobStore.getAllEntities()

    assertThat(results).hasSize(2)
    checkResult(
      blobStore.getEntityByKey(KEY_1),
      KEY_1,
      CREATED,
      1250L,
      listOf(PACKAGE_1, PACKAGE_2),
      message
    )
    checkResult(
      blobStore.getEntityByKey(KEY_2),
      KEY_2,
      CREATED,
      CREATED,
      listOf(PACKAGE_1),
      testMessage
    )
  }

  @Test
  fun insertEntities_entityExpired_insertNewEntity() = runBlocking {
    blobStore.putEntity(wrapped1)

    var results = blobStore.getAllEntities()

    assertThat(results).hasSize(1)
    checkResult(
      blobStore.getEntityByKey(KEY_1),
      KEY_1,
      CREATED,
      CREATED,
      listOf(PACKAGE_1, PACKAGE_2),
      testMessage
    )

    fakeTime = fakeTime.plusMillis(1000)

    val message =
      TestMessage.newBuilder()
        .apply {
          version = 2L
          name = "updated_name"
          content = "updated_content"
        }
        .build()

    blobStore.putEntity(
      WrappedEntity(
        metadata =
          EntityMetadata(
            id = KEY_1,
            associatedPackageNames = listOf(PACKAGE_1),
            created = Instant.ofEpochMilli(1001),
            updated = Instant.ofEpochMilli(1001)
          ),
        entity = message
      )
    )

    results = blobStore.getAllEntities()

    assertThat(results).hasSize(1)
    checkResult(blobStore.getEntityByKey(KEY_1), KEY_1, 2000L, 2000L, listOf(PACKAGE_1), message)
  }

  @Test
  fun getEntityByKey() = runBlocking {
    blobStore.putEntity(wrapped1)

    val result = blobStore.getEntityByKey(KEY_1)

    checkResult(result, KEY_1, CREATED, CREATED, listOf(PACKAGE_1, PACKAGE_2), testMessage)
  }

  @Test
  fun getEntityByKey_filtersOutExpiredEntity() = runBlocking {
    blobStore.putEntity(
      WrappedEntity<TestMessage>(
        EntityMetadata(
          id = KEY_1,
          associatedPackageNames = listOf(PACKAGE_1, PACKAGE_2),
          created = Instant.ofEpochMilli(CREATED),
          updated = Instant.ofEpochMilli(CREATED)
        ),
        testMessage
      )
    )

    fakeTime = fakeTime.plusMillis(1000)

    assertThat(blobStore.getEntityByKey(KEY_1)).isNull()
  }

  @Test
  fun getAllEntities() = runBlocking {
    blobStore.putEntities(listOf(wrapped1, wrapped2))

    val results = blobStore.getAllEntities()

    assertThat(results).hasSize(2)
    checkResult(results[0], KEY_1, CREATED, CREATED, listOf(PACKAGE_1, PACKAGE_2), testMessage)
    checkResult(results[1], KEY_2, CREATED, CREATED, listOf(PACKAGE_1), testMessage)
  }

  @Test
  fun getAllEntities_filtersOutExpiredEntity() = runBlocking {
    blobStore.putEntity(
      WrappedEntity<TestMessage>(
        EntityMetadata(
          id = KEY_1,
          associatedPackageNames = listOf(PACKAGE_1, PACKAGE_2),
          created = Instant.ofEpochMilli(CREATED),
          updated = Instant.ofEpochMilli(CREATED)
        ),
        testMessage
      )
    )

    fakeTime = fakeTime.plusMillis(1000)

    blobStore.putEntity(
      WrappedEntity<TestMessage>(
        EntityMetadata(
          id = KEY_2,
          associatedPackageNames = listOf(PACKAGE_1),
          created = Instant.ofEpochMilli(CREATED),
          updated = Instant.ofEpochMilli(CREATED)
        ),
        testMessage
      )
    )

    val results = blobStore.getAllEntities()

    assertThat(results).hasSize(1)
    checkResult(results[0], KEY_2, CREATED + 1000, CREATED + 1000, listOf(PACKAGE_1), testMessage)
  }

  @Test
  fun putEntities_enforcesMaxItems() = runBlocking {
    blobStore.putEntities(listOf(wrapped1, wrapped2, wrapped3))

    val results = blobStore.getAllEntities()

    assertThat(results).hasSize(2)
    checkResult(results[0], KEY_2, CREATED, CREATED, listOf(PACKAGE_1), testMessage)
    checkResult(results[1], KEY_3, CREATED, CREATED, listOf(PACKAGE_1), testMessage)
  }

  @Test
  fun removeEntityByKey() = runBlocking {
    blobStore.putEntities(listOf(wrapped1, wrapped2))

    val results1 = blobStore.getAllEntities()
    assertThat(results1).hasSize(2)

    checkResult(results1[0], KEY_1, CREATED, CREATED, listOf(PACKAGE_1, PACKAGE_2), testMessage)
    checkResult(results1[1], KEY_2, CREATED, CREATED, listOf(PACKAGE_1), testMessage)

    blobStore.removeEntityByKey(KEY_1)

    val results2 = blobStore.getAllEntities()
    assertThat(results2).hasSize(1)
    checkResult(results2[0], KEY_2, CREATED, CREATED, listOf(PACKAGE_1), testMessage)
  }

  @Test
  fun removeAll() = runBlocking {
    blobStore.putEntities(listOf(wrapped1, wrapped2))

    val results1 = blobStore.getAllEntities()
    assertThat(results1).hasSize(2)

    checkResult(results1[0], KEY_1, CREATED, CREATED, listOf(PACKAGE_1, PACKAGE_2), testMessage)
    checkResult(results1[1], KEY_2, CREATED, CREATED, listOf(PACKAGE_1), testMessage)

    blobStore.removeAll()

    val results2 = blobStore.getAllEntities()
    assertThat(results2).hasSize(0)
  }

  private fun checkResult(
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

  companion object {
    private const val KEY_1 = "key_1"
    private const val KEY_2 = "key_2"
    private const val KEY_3 = "key_3"
    private const val PACKAGE_1 = "package_1"
    private const val PACKAGE_2 = "package_2"
    private val DTD = TestMessage::class.java.toString()
    private const val CREATED = 1000L
    private const val VERSION = 1L
    private const val NAME = "test"
    private const val CONTENT = "this is a test"
  }
}
