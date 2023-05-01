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
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.testutil.toTestRequest
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class DefaultManagedDataCacheConnectionProviderTest {
  data class TestData(val data: Int)

  private val testDataDTD =
    dataTypeDescriptor("TestData", Unit::class) { "data" to FieldType.String }

  private val testDataManagementStrategy =
    ManagementStrategy.Stored(
      encrypted = false,
      media = StorageMedia.MEMORY,
      ttl = Duration.ofMinutes(5),
      deletionTriggers =
        setOf(DeletionTrigger(trigger = Trigger.PACKAGE_UNINSTALLED, targetField = "packageName"))
    )

  private val testTimeSource = TimeSource { Instant.ofEpochMilli(12345) }

  private val mockDataStorage =
    mock<ManagedDataCache<TestData>>().also {
      whenever(it.dataTypeDescriptor).thenReturn(testDataDTD)
      whenever(it.managementStrategy).thenReturn(testDataManagementStrategy)
    }

  interface TestReadConnection : ReadConnection
  interface TestWriteConnection : WriteConnection {
    companion object {
      var instances: Int = 0
    }
  }

  class TestWriteConnectionImpl : TestWriteConnection {
    init {
      TestWriteConnection.instances = TestWriteConnection.instances + 1
    }
  }

  val readConnection = object : TestReadConnection {}
  val writeConnection = object : TestWriteConnection {}

  val connectionProvider =
    object :
      DefaultManagedDataCacheConnectionProvider<TestData>(
        mockDataStorage,
        mapOf(
          TestReadConnection::class.java to { readConnection },
          TestWriteConnection::class.java to { TestWriteConnectionImpl() }
        )
      ) {}

  @Test
  fun constructor_initializesManagedDataTypeCorrectly() {
    val managedDataType = connectionProvider.dataType
    assertThat(managedDataType.descriptor).isEqualTo(testDataDTD)
    assertThat(managedDataType.managementStrategy).isEqualTo(testDataManagementStrategy)
    assertThat(managedDataType.connectionTypes)
      .containsExactly(TestReadConnection::class.java, TestWriteConnection::class.java)
  }

  @Test
  fun getConnection_returnsResultofPassedInLambdas() {
    assertThat(connectionProvider.getConnection(TestReadConnection::class.toTestRequest()))
      .isSameInstanceAs(readConnection)
    assertThat(TestWriteConnection.instances).isEqualTo(0)
    val connection1 = connectionProvider.getConnection(TestWriteConnection::class.toTestRequest())
    assertThat(TestWriteConnection.instances).isEqualTo(1)
    val connection2 = connectionProvider.getConnection(TestWriteConnection::class.toTestRequest())
    assertThat(TestWriteConnection.instances).isEqualTo(2)
    assertThat(connection1).isNotSameInstanceAs(connection2)
  }
}
