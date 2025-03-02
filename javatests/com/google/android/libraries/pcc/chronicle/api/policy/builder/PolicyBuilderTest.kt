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

/*
 * Copyright 2021 Google LLC.
 *
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 *
 * Code distributed by Google as part of this project is also subject to an additional IP rights
 * grant found at
 * http://polymer.github.io/PATENTS.txt
 */
package com.google.android.libraries.pcc.chronicle.api.policy.builder

import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.All
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PolicyBuilderTest {
  /** Test object that always evaluates to false, regardless of the context. */
  object TestContextRule : PolicyContextRule {
    override val name: String = "TestContextRule"
    override val operands: List<PolicyContextRule> = emptyList()

    override fun invoke(context: TypedMap): Boolean = false
  }

  @Test
  fun minimal() {
    val actual = policy("MyPolicy", "Analytics")

    assertThat(actual.name).isEqualTo("MyPolicy")
    assertThat(actual.egressType).isEqualTo("Analytics")
    assertThat(actual.description).isEqualTo("")
    assertThat(actual.allowedContext).isEqualTo(All)
    assertThat(actual.configs).isEmpty()
    assertThat(actual.targets).isEmpty()
  }

  @Test
  fun withDescription() {
    val actual = policy("MyPolicy", "Analytics") { description = "This is my description." }

    assertThat(actual.name).isEqualTo("MyPolicy")
    assertThat(actual.egressType).isEqualTo("Analytics")
    assertThat(actual.description).isEqualTo("This is my description.")
    assertThat(actual.allowedContext).isEqualTo(All)
    assertThat(actual.configs).isEmpty()
    assertThat(actual.targets).isEmpty()
  }

  @Test
  fun withAllowedContext() {
    val actual = policy("MyPolicy", "Analytics") { allowedContext = TestContextRule }

    assertThat(actual.name).isEqualTo("MyPolicy")
    assertThat(actual.egressType).isEqualTo("Analytics")
    assertThat(actual.description).isEqualTo("")
    assertThat(actual.allowedContext).isEqualTo(TestContextRule)
    assertThat(actual.configs).isEmpty()
    assertThat(actual.targets).isEmpty()
  }

  @Test
  fun withTargets() {
    val actual =
      policy("MyPolicy", "Analytics") {
        target(dataTypeDescriptor = FOO_DTD, maxAge = Duration.ofMinutes(15)) {
          retention(StorageMedium.RAM)
        }
        target(
          dataTypeDescriptor = dataTypeDescriptor("Bar", Unit::class),
          maxAge = Duration.ofDays(2),
        ) {
          retention(StorageMedium.DISK, encryptionRequired = true)
        }
      }

    assertThat(actual.name).isEqualTo("MyPolicy")
    assertThat(actual.egressType).isEqualTo("Analytics")
    assertThat(actual.description).isEqualTo("")
    assertThat(actual.allowedContext).isEqualTo(All)
    assertThat(actual.configs).isEmpty()
    assertThat(actual.targets)
      .containsExactly(
        target(FOO_DTD, maxAge = Duration.ofMinutes(15)) { retention(StorageMedium.RAM) },
        target(BAR_DTD, maxAge = Duration.ofDays(2)) {
          retention(StorageMedium.DISK, encryptionRequired = true)
        },
      )
  }

  @Test
  fun withConfigs() {
    val actual =
      policy("MyPolicy", "Analytics") {
        config("DiskStorage") { "engine" to "innoDB" }
        config("Cache") { "maxItems" to "15" }
      }

    assertThat(actual.name).isEqualTo("MyPolicy")
    assertThat(actual.egressType).isEqualTo("Analytics")
    assertThat(actual.description).isEqualTo("")
    assertThat(actual.allowedContext).isEqualTo(All)
    assertThat(actual.configs)
      .containsExactly(
        "DiskStorage",
        PolicyConfigBuilder().apply { "engine" to "innoDB" }.build(),
        "Cache",
        PolicyConfigBuilder().apply { "maxItems" to "15" }.build(),
      )
    assertThat(actual.targets).isEmpty()
  }

  @Test
  fun copyConstructor_deepCopiesPolicyBuilder() {
    val pb1 =
      PolicyBuilder("name", "egress").apply {
        description = "base"
        target(TEST_PERSON_GENERATED_DTD, Duration.ZERO) {}
        configs["base"] = mapOf("base" to "base")
      }
    val pb2 =
      PolicyBuilder(pb1).apply {
        description = "pb2"
        allowedContext = TestContextRule
        target(FOO_DTD, Duration.ZERO) {}
        configs["base"] = mapOf("mod" to "mod")
      }

    // `pb1` must not be modified by changes to copy
    assertThat(pb1.description).isEqualTo("base")
    assertThat(pb1.allowedContext).isEqualTo(All)
    assertThat(pb1.targets).hasSize(1)
    assertThat(pb1.configs["base"]).isEqualTo(mapOf("base" to "base"))

    // Confirm changes to `pb2`
    assertThat(pb2.allowedContext).isEqualTo(TestContextRule)
    assertThat(pb2.targets).hasSize(2)
    // The `configs` of `pb2` must be modified.
    assertThat(pb2.configs["base"]).isEqualTo(mapOf("mod" to "mod"))
  }

  companion object {
    private val FOO_DTD = dataTypeDescriptor("Foo", Unit::class)
    private val BAR_DTD = dataTypeDescriptor("Bar", Unit::class)
  }
}
