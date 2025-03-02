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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionRequestTest {
  @Test
  fun connectionRequestWithConnectionType_connectionNameIsSet() {
    val request = ConnectionRequest(FooReader::class.java, testProcessorNode, null)
    assertThat(request.connectionName).isInstanceOf(ConnectionName.Reader::class.java)
    assertThat(request.connectionType).isNotNull()
  }

  @Test
  fun connectionRequestWithConnectionName_connectionTypeIsNotSet() {
    val request =
      ConnectionRequest(ConnectionName.Reader<ReadConnection>(Name("foo")), testProcessorNode, null)
    assertThat(request.connectionType).isNull()
  }

  @Test
  fun connectionTypeOfReadConnection_isReadConnection() {
    val request = ConnectionRequest(FooReader::class.java, testProcessorNode, null)
    assertThat(request.isReadConnection()).isTrue()
  }

  @Test
  fun connectionTypeOfWriteConnection_isNotReadConnection() {
    val request = ConnectionRequest(FooWriter::class.java, testProcessorNode, null)
    assertThat(request.isReadConnection()).isFalse()
  }

  @Test
  fun connectionNameReader_isReadConnection() {
    val request =
      ConnectionRequest(ConnectionName.Reader<ReadConnection>(Name("foo")), testProcessorNode, null)
    assertThat(request.isReadConnection()).isTrue()
  }

  @Test
  fun connectionNameWriter_isNotReadConnection() {
    val request =
      ConnectionRequest(
        ConnectionName.Writer<WriteConnection>(Name("foo")),
        testProcessorNode,
        null,
      )
    assertThat(request.isReadConnection()).isFalse()
  }

  companion object {
    private val testProcessorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> = emptySet()
      }

    class FooReader : ReadConnection

    class FooWriter : WriteConnection
  }
}
