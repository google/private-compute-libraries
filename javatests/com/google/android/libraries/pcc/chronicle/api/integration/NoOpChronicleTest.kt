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

package com.google.android.libraries.pcc.chronicle.api.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.ConnectionResult
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.error.Disabled
import com.google.android.libraries.pcc.chronicle.api.getConnection
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoOpChronicleTest {
  private val dummyProcessorNode =
    object : ProcessorNode {
      override val requiredConnectionTypes = setOf(DummyReadConnection::class.java)
    }

  private val dummyPolicy =
    policy("dummy", "None") {
      target(dataTypeDescriptor("dummy", Foo::class), Duration.ofMillis(1500)) {
        retention(StorageMedium.RAM)
      }
    }

  private interface DummyReadConnection : ReadConnection

  @Test
  fun getAvailableConnectionTypesKClass_returnsEmpty() {
    val impl = NoOpChronicle()

    assertThat(impl.getAvailableConnectionTypes(Foo::class))
      .isEqualTo(Chronicle.ConnectionTypes.EMPTY)
  }

  @Test
  fun getAvailableConnectionTypesClass_returnsEmpty() {
    val impl = NoOpChronicle()

    assertThat(impl.getAvailableConnectionTypes(Foo::class.java))
      .isEqualTo(Chronicle.ConnectionTypes.EMPTY)
  }

  @Test
  fun getConnection_returnsDisabled() {
    val result =
      NoOpChronicle().getConnection<DummyReadConnection>(dummyProcessorNode, dummyPolicy)
        as ConnectionResult.Failure
    assertThat(result.error).isInstanceOf(Disabled::class.java)
  }

  private data class Foo(val name: String)
}
