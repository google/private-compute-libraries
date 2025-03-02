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

package com.google.android.libraries.pcc.chronicle.storage.datacache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class DataCacheReaderTest {
  /** A data class to test storage. */
  data class DummyTestLabel(val value: Int, val version: Long = 0)

  private val mockDataStorage = mock<ManagedDataCache<DummyTestLabel>>()

  @Test
  fun reader_all_returnsAllFromStorage_orderedByUpdatedTimestamp() {
    val reader = DataCacheReader.createDefault(mockDataStorage)

    whenever(mockDataStorage.all()).thenReturn(listOf(DUMMY_LABEL_WRAPPER_1, DUMMY_LABEL_WRAPPER_2))

    val all = reader.allTimestamped()

    assertThat(all)
      .containsExactly(
        DUMMY_LABEL_WRAPPER_2.asTimestampedInstance(),
        DUMMY_LABEL_WRAPPER_1.asTimestampedInstance(),
      )
  }

  @Test
  fun reader_forEntity_returnsCorrectEntity() {
    val reader = DataCacheReader.createDefault(mockDataStorage)

    whenever(mockDataStorage.get("1")).thenReturn(DUMMY_LABEL_WRAPPER_1)
    whenever(mockDataStorage.get("2")).thenReturn(DUMMY_LABEL_WRAPPER_2)
    whenever(mockDataStorage.get("3")).thenReturn(null)

    assertThat(reader.forEntity("1")).isEqualTo(DUMMY_LABEL_WRAPPER_1.entity)
    assertThat(reader.forEntity("2")).isEqualTo(DUMMY_LABEL_WRAPPER_2.entity)
    assertThat(reader.forEntity("3")).isEqualTo(null)
  }

  @Test
  fun reader_forEntityTimestamped_returnsCorrectEntity() {
    val reader = DataCacheReader.createDefault(mockDataStorage)

    whenever(mockDataStorage.get("1")).thenReturn(DUMMY_LABEL_WRAPPER_1)
    whenever(mockDataStorage.get("2")).thenReturn(DUMMY_LABEL_WRAPPER_2)
    whenever(mockDataStorage.get("3")).thenReturn(null)

    assertThat(reader.forEntityTimestamped("1"))
      .isEqualTo(
        Timestamped(
          DUMMY_LABEL_WRAPPER_1.entity,
          DUMMY_LABEL_WRAPPER_1.metadata.updated.toInstant(),
        )
      )
    assertThat(reader.forEntityTimestamped("2"))
      .isEqualTo(
        Timestamped(
          DUMMY_LABEL_WRAPPER_2.entity,
          DUMMY_LABEL_WRAPPER_2.metadata.updated.toInstant(),
        )
      )
    assertThat(reader.forEntityTimestamped("3")).isEqualTo(null)
  }

  companion object {
    val DUMMY_LABEL_WRAPPER_1: WrappedEntity<DummyTestLabel> =
      WrappedEntity(
        metadata = EntityMetadata("one", "package_1", Instant.ofEpochSecond(10000)),
        entity = DummyTestLabel(value = 1),
      )
    val DUMMY_LABEL_WRAPPER_2: WrappedEntity<DummyTestLabel> =
      WrappedEntity(
        metadata = EntityMetadata("two", "package_1", Instant.ofEpochSecond(20000)),
        entity = DummyTestLabel(value = 2),
      )
  }
}
