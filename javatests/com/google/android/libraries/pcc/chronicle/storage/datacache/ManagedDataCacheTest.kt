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
import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ManagedDataCacheTest {
  data class TestData(val data: Int)

  private val testDataDTD =
    dataTypeDescriptor("TestData", Unit::class) { "data" to FieldType.String }

  private val testDataManagementStrategy =
    ManagementStrategy.Stored(
      encrypted = false,
      media = StorageMedia.MEMORY,
      ttl = Duration.ofMinutes(5),
      deletionTriggers =
        setOf(DeletionTrigger(trigger = Trigger.PACKAGE_UNINSTALLED, targetField = "packageName")),
    )

  private val mockDataStorage = mock<DataCacheStorage>()

  private var testTime = Instant.ofEpochMilli(12345)

  private val testTimeSource = TimeSource { testTime }

  private val testDataStorage =
    ManagedDataCache(TestData::class.java, mockDataStorage, 100, Duration.ofMinutes(5), testDataDTD)

  private val testTen =
    WrappedEntity(metadata = EntityMetadata("ten", "Package10", testTime), entity = TestData(10))
  private val testThirty =
    WrappedEntity(metadata = EntityMetadata("thirty", "Package30", testTime), entity = TestData(30))

  @Test
  fun constructor_initializesDataTypeDescriptor() {
    assertThat(testDataStorage.dataTypeDescriptor).isEqualTo(testDataDTD)
  }

  @Test
  fun constructor_initializesManagementStrategy() {
    assertThat(testDataStorage.managementStrategy).isEqualTo(testDataManagementStrategy)
  }

  @Test
  fun size_invokesCacheSize() {
    testDataStorage.size()
    verify(mockDataStorage, times(1)).size(TestData::class.java)
  }

  @Test
  fun size_returnsCacheSize() {
    whenever(mockDataStorage.size(TestData::class.java)).thenReturn(100)
    assertThat(testDataStorage.size()).isEqualTo(100)
  }

  @Test
  fun get_invokesCacheGet() {
    testDataStorage.get("10")
    verify(mockDataStorage, times(1)).get(TestData::class.java, "10")
  }

  @Test
  fun get_returnsCacheGetResults() {
    whenever(mockDataStorage.get(TestData::class.java, "10")).thenReturn(testTen)
    whenever(mockDataStorage.get(TestData::class.java, "30")).thenReturn(testThirty)
    whenever(mockDataStorage.get(TestData::class.java, "5")).thenReturn(null)

    assertThat(testDataStorage.get("10")).isEqualTo(testTen)
    assertThat(testDataStorage.get("30")).isEqualTo(testThirty)
    assertThat(testDataStorage.get("5")).isNull()
  }

  @Test
  fun put_invokesCachePut() {
    testDataStorage.put(testTen)
    verify(mockDataStorage, times(1)).put(TestData::class.java, testTen)
  }

  @Test
  fun put_returnsCachePutResults() {
    whenever(mockDataStorage.put(TestData::class.java, testTen)).thenReturn(true)

    assertThat(testDataStorage.put(testTen)).isEqualTo(true)
  }

  @Test
  fun remove_invokesCacheRemove() {
    testDataStorage.remove("ten")
    verify(mockDataStorage, times(1)).remove(TestData::class.java, "ten")
  }

  @Test
  fun remove_returnsCacheRemoveResults() {
    whenever(mockDataStorage.remove(TestData::class.java, "ten")).thenReturn(testTen)
    whenever(mockDataStorage.remove(TestData::class.java, "thirty")).thenReturn(testThirty)
    whenever(mockDataStorage.remove(TestData::class.java, "5")).thenReturn(null)

    assertThat(testDataStorage.remove("ten")).isEqualTo(testTen)
    assertThat(testDataStorage.remove("thirty")).isEqualTo(testThirty)
    assertThat(testDataStorage.remove("5")).isNull()
  }

  @Test
  fun all_invokesCacheAll() {
    testDataStorage.all()
    verify(mockDataStorage, times(1)).all(TestData::class.java)
  }

  @Test
  fun all_returnsCacheAllResults() {
    whenever(mockDataStorage.all(TestData::class.java)).thenReturn(listOf(testTen, testThirty))
    assertThat(testDataStorage.all()).isEqualTo(listOf(testTen, testThirty))
  }

  @Test
  fun removeAll_invokesCacheRemoveAll() {
    testDataStorage.removeAll()
    verify(mockDataStorage, times(1)).removeAll(TestData::class.java)
  }
}
