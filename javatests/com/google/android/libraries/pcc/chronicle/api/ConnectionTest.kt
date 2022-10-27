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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConnectionTest {
  @Test
  fun isReadConnection() {
    assertThat(TestReadConnection::class.java.isReadConnection).isTrue()
    assertThat(TestReadWriteConnection::class.java.isReadConnection).isTrue()
    assertThat(GrandchildReadConnection::class.java.isReadConnection).isTrue()

    assertThat(TestWriteConnection::class.java.isReadConnection).isFalse()
    assertThat(GrandchildWriteConnection::class.java.isReadConnection).isFalse()
    assertThat(String::class.java.isReadConnection).isFalse()
  }

  @Test
  fun isWriteConnection() {
    assertThat(TestWriteConnection::class.java.isWriteConnection).isTrue()
    assertThat(TestReadWriteConnection::class.java.isWriteConnection).isTrue()
    assertThat(GrandchildWriteConnection::class.java.isWriteConnection).isTrue()

    assertThat(TestReadConnection::class.java.isWriteConnection).isFalse()
    assertThat(GrandchildReadConnection::class.java.isWriteConnection).isFalse()
    assertThat(String::class.java.isWriteConnection).isFalse()
  }

  interface TestReadConnection : ReadConnection
  interface TestWriteConnection : WriteConnection
  interface TestReadWriteConnection : ReadConnection, WriteConnection
  interface GrandchildReadConnection : TestReadConnection
  interface GrandchildWriteConnection : TestWriteConnection
}
