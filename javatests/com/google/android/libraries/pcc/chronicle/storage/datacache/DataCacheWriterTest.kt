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
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class DataCacheWriterTest {
  /** A data class to test storage. */
  data class DummyTestLabel(val value: Int, val version: Long = 0)

  private val mockDataStorage = mock<ManagedDataCache<DummyTestLabel>>()
  private val mockTimeSource = mock<TimeSource>()

  @Test
  fun write_invokesDataCacheWrite() {
    val writer = DataCacheWriter.createDefault(mockDataStorage, mockTimeSource)

    whenever(mockTimeSource.now()).thenReturn(Instant.ofEpochSecond(11000))
    writer.write(DummyTestLabel(value = 1), "1", Instant.ofEpochSecond(10000), "package_1")

    whenever(mockTimeSource.now()).thenReturn(Instant.ofEpochSecond(12000))
    writer.write(DummyTestLabel(value = 2), "2", Instant.ofEpochSecond(20000), "package_2")

    verify(mockDataStorage, times(1))
      .put(
        WrappedEntity(
          metadata =
            EntityMetadata(
              id = "1",
              associatedPackageName = "package_1",
              created = Instant.ofEpochSecond(10000),
              updated = Instant.ofEpochSecond(11000)
            ),
          entity = DummyTestLabel(value = 1)
        )
      )
    verify(mockDataStorage, times(1))
      .put(
        WrappedEntity(
          metadata =
            EntityMetadata(
              id = "2",
              associatedPackageName = "package_2",
              created = Instant.ofEpochSecond(20000),
              updated = Instant.ofEpochSecond(12000)
            ),
          entity = DummyTestLabel(value = 2)
        )
      )
  }

  @Test
  fun remove_invokesDataCacheRemove() {
    val writer = DataCacheWriter.createDefault(mockDataStorage, mockTimeSource)
    writer.remove("10")
    writer.remove("5")
    verify(mockDataStorage, times(1)).remove("10")
    verify(mockDataStorage, times(1)).remove("5")
  }
}
