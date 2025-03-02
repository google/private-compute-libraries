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

package com.google.android.libraries.pcc.chronicle.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataTypeTest {
  val testDataType =
    object : DataType {
      override val descriptor = dataTypeDescriptor("unused", Unit::class)
      override val managementStrategy = ManagementStrategy.PassThru
      override val connectionTypes =
        setOf(DummyReadConnection::class.java, DummyWriteConnection::class.java)
    }

  @Test
  fun ttl_returnsTtlInManagementStrategy() {
    assertThat(testDataType.ttl).isEqualTo(Duration.ZERO)
    val anotherTestDataType =
      object : DataType {
        override val descriptor = testDataType.descriptor
        override val managementStrategy =
          ManagementStrategy.Stored(
            encrypted = false,
            media = StorageMedia.MEMORY,
            ttl = Duration.ofDays(2),
            deletionTriggers = emptySet(),
          )
        override val connectionTypes = emptySet<Class<Connection>>()
      }
    assertThat(anotherTestDataType.ttl).isEqualTo(Duration.ofDays(2))
  }

  interface DummyReadConnection : ReadConnection

  interface DummyWriteConnection : WriteConnection
}
