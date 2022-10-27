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

package com.google.android.libraries.pcc.chronicle.codegen.processor

import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage
import com.google.android.libraries.pcc.chronicle.storage.datacache.ManagedDataCache
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TestUtilsTest {
  data class TestData(val data: Int)

  private val testDataDTD =
    dataTypeDescriptor("TestData", Unit::class) { "data" to FieldType.String }

  private val mockDataStorage = mock<DataCacheStorage>()

  private val testDataStorage =
    ManagedDataCache(
      TestData::class.java,
      mockDataStorage,
      100,
      Duration.ofMinutes(5),
      testDataDTD,
    )

  @Test
  fun configEquals_returnsTrueForEqualConfigs() {
    val identicalConfigDataStorage =
      ManagedDataCache(
        TestData::class.java,
        mockDataStorage,
        100,
        Duration.ofMinutes(5),
        testDataDTD,
      )

    assertThat(testDataStorage.configEquals(identicalConfigDataStorage)).isTrue()
  }

  @Test
  fun configEquals_returnsFalseForDifferentTypes() {
    val number = 42

    assertThat(testDataStorage.configEquals(number)).isFalse()
  }

  @Test
  fun configEquals_returnsFalseForDifferentEntityClass() {
    val differentConfigDataStorage =
      ManagedDataCache<String>(
        String::class.java,
        mockDataStorage,
        100,
        Duration.ofMinutes(5),
        testDataDTD,
      )

    assertThat(testDataStorage.configEquals(differentConfigDataStorage)).isFalse()
  }

  @Test
  fun configEquals_returnsFalseForDifferentCache() {
    val differentMockDataStorage = mock<DataCacheStorage>()
    val differentConfigDataStorage =
      ManagedDataCache(
        TestData::class.java,
        differentMockDataStorage,
        100,
        Duration.ofMinutes(5),
        testDataDTD,
      )

    assertThat(testDataStorage.configEquals(differentConfigDataStorage)).isFalse()
  }

  @Test
  fun configEquals_returnsFalseForDifferentMaxSize() {
    val differentConfigDataStorage =
      ManagedDataCache(
        TestData::class.java,
        mockDataStorage,
        42,
        Duration.ofMinutes(5),
        testDataDTD,
      )

    assertThat(testDataStorage.configEquals(differentConfigDataStorage)).isFalse()
  }

  @Test
  fun configEquals_returnsFalseForDifferentTtl() {
    val differentConfigDataStorage =
      ManagedDataCache(
        TestData::class.java,
        mockDataStorage,
        100,
        Duration.ofMinutes(42),
        testDataDTD,
      )

    assertThat(testDataStorage.configEquals(differentConfigDataStorage)).isFalse()
  }

  @Test
  fun configEquals_returnsFalseForDifferentDtd() {
    val differentDtd = dataTypeDescriptor("Foo", Unit::class)
    val differentConfigDataStorage =
      ManagedDataCache(
        TestData::class.java,
        mockDataStorage,
        100,
        Duration.ofMinutes(5),
        differentDtd,
      )

    assertThat(testDataStorage.configEquals(differentConfigDataStorage)).isFalse()
  }
}
