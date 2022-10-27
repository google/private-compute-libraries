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
import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStore
import com.google.android.libraries.pcc.chronicle.storage.blobstore.BlobStoreManagement
import com.google.android.libraries.pcc.chronicle.storage.blobstore.InMemoryManagementInfo
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TestMessage
import com.google.android.libraries.pcc.chronicle.storage.blobstore.TestPerson
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InMemoryBlobStoreManagementTest {
  private val inMemoryStorage = InMemoryStorage()
  private lateinit var messageBlobStore: BlobStore<TestMessage>
  private lateinit var personBlobStore: BlobStore<TestPerson>
  private lateinit var inMemoryBlobStoreManagement: BlobStoreManagement

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

  private val wrapped1 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_1,
        associatedPackageNames = listOf(PACKAGE_1, PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED),
        updated = Instant.ofEpochMilli(CREATED)
      ),
      testMessage
    )

  private val wrapped2 =
    WrappedEntity<TestMessage>(
      EntityMetadata(
        id = KEY_2,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED),
        updated = Instant.ofEpochMilli(CREATED)
      ),
      testMessage
    )

  private val wrapped3 =
    WrappedEntity<TestPerson>(
      EntityMetadata(
        id = KEY_1,
        associatedPackageNames = listOf(PACKAGE_1),
        created = Instant.ofEpochMilli(CREATED),
        updated = Instant.ofEpochMilli(CREATED)
      ),
      testPerson
    )

  private val wrapped4 =
    WrappedEntity<TestPerson>(
      EntityMetadata(
        id = KEY_2,
        associatedPackageNames = listOf(PACKAGE_2),
        created = Instant.ofEpochMilli(CREATED),
        updated = Instant.ofEpochMilli(CREATED)
      ),
      testPerson
    )

  private val managementInfo1 = InMemoryManagementInfo(DTD_MESSAGE, 500, 10)
  private val managementInfo2 = InMemoryManagementInfo(DTD_PERSON, 500, 10)

  private var fakeTime = Instant.ofEpochMilli(CREATED)
  private val timeSource = TimeSource { fakeTime }

  @Before
  fun setUp() {
    inMemoryBlobStoreManagement = InMemoryBlobStoreManagement(inMemoryStorage)
    messageBlobStore =
      InMemoryBlobStore(inMemoryStorage.registerDataTypeStore(managementInfo1), timeSource)
    personBlobStore =
      InMemoryBlobStore(inMemoryStorage.registerDataTypeStore(managementInfo2), timeSource)
  }

  @Test
  fun clearAll() = runBlocking {
    messageBlobStore.putEntities(listOf(wrapped1, wrapped2))
    personBlobStore.putEntities(listOf(wrapped3, wrapped4))

    val result = inMemoryBlobStoreManagement.clearAll()

    assertThat(result).isEqualTo(4)
    assertThat(messageBlobStore.getAllEntities()).hasSize(0)
    assertThat(personBlobStore.getAllEntities()).hasSize(0)
  }

  @Test
  fun deleteExpiredEntities() = runBlocking {
    messageBlobStore.putEntity(wrapped1)
    personBlobStore.putEntity(wrapped3)

    fakeTime = fakeTime.plusMillis(750)

    messageBlobStore.putEntity(wrapped2)
    personBlobStore.putEntity(wrapped4)

    inMemoryBlobStoreManagement.deleteExpiredEntities(2000, setOf(managementInfo1, managementInfo2))

    assertThat(messageBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(messageBlobStore.getEntityByKey(KEY_2)).isNotNull()
    assertThat(personBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(personBlobStore.getEntityByKey(KEY_2)).isNotNull()
  }

  @Test
  fun deleteEntitiesBetween() = runBlocking {
    messageBlobStore.putEntity(wrapped1)
    personBlobStore.putEntity(wrapped3)

    fakeTime = fakeTime.plusMillis(750)

    messageBlobStore.putEntity(wrapped2)
    personBlobStore.putEntity(wrapped4)

    val result =
      inMemoryBlobStoreManagement.deleteEntitiesCreatedBetween(CREATED - 100, CREATED + 100)

    assertThat(result).isEqualTo(2)
    assertThat(messageBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(messageBlobStore.getEntityByKey(KEY_2)).isNotNull()
    assertThat(personBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(personBlobStore.getEntityByKey(KEY_2)).isNotNull()
  }

  @Test
  fun deletePackage() = runBlocking {
    messageBlobStore.putEntities(listOf(wrapped1, wrapped2))
    personBlobStore.putEntities(listOf(wrapped3, wrapped4))

    val result = inMemoryBlobStoreManagement.deletePackage("package_2")

    assertThat(result).isEqualTo(2)
    assertThat(messageBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(messageBlobStore.getEntityByKey(KEY_2)).isNotNull()
    assertThat(personBlobStore.getEntityByKey(KEY_1)).isNotNull()
    assertThat(personBlobStore.getEntityByKey(KEY_2)).isNull()
  }

  @Test
  fun reconcilePackages() = runBlocking {
    messageBlobStore.putEntities(listOf(wrapped1, wrapped2))
    personBlobStore.putEntities(listOf(wrapped3, wrapped4))

    val result = inMemoryBlobStoreManagement.reconcilePackages(setOf("package_2"))

    assertThat(result).isEqualTo(3)
    assertThat(messageBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(messageBlobStore.getEntityByKey(KEY_2)).isNull()
    assertThat(personBlobStore.getEntityByKey(KEY_1)).isNull()
    assertThat(personBlobStore.getEntityByKey(KEY_2)).isNotNull()
  }

  companion object {
    private const val KEY_1 = "key_1"
    private const val KEY_2 = "key_2"
    private const val PACKAGE_1 = "package_1"
    private const val PACKAGE_2 = "package_2"
    private val DTD_MESSAGE = TestMessage::class.java.toString()
    private val DTD_PERSON = TestPerson::class.java.toString()
    private const val CREATED = 1000L
    private const val VERSION = 1L
    private const val NAME = "test"
    private const val CONTENT = "this is a test"
    private const val FIRST_NAME = "John"
    private const val LAST_NAME = "Doe"
    private const val AGE = 30
  }
}
