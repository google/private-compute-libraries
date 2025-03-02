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

package com.google.android.libraries.pcc.chronicle.analysis.impl

import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PERSON_DESCRIPTOR
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import com.google.android.libraries.pcc.chronicle.util.Key
import com.google.android.libraries.pcc.chronicle.util.MutableTypedMap
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PolicyContextVerificationTest {
  object NameKey : Key<String>

  object NamePolicy : PolicyContextRule {
    override val name: String = "NamePolicy"
    override val operands: List<PolicyContextRule> = emptyList()

    override fun invoke(context: TypedMap): Boolean {
      return "test" == context[NameKey]
    }
  }

  val TEST_POLICY =
    policy("Test", "TestingEgress") {
      allowedContext = NamePolicy
      target(PERSON_DESCRIPTOR, maxAge = Duration.ZERO) {
        retention(StorageMedium.RAM, true)

        "name" { rawUsage(UsageType.EGRESS) }
        "age" { rawUsage(UsageType.EGRESS) }
      }
    }

  @Test
  fun verifyContext_succeeds() {
    val mutableTypedMap = MutableTypedMap()
    mutableTypedMap[NameKey] = "test"
    val context = TypedMap(mutableTypedMap)

    assertThat(TEST_POLICY.verifyContext(context)).isEmpty()
  }

  @Test
  fun verifyContext_fails() {
    val mutableTypedMap = MutableTypedMap()
    mutableTypedMap[NameKey] = "other name"
    val context = TypedMap(mutableTypedMap)
    val policyChecks = TEST_POLICY.verifyContext(context)

    assertThat(policyChecks).isNotEmpty()
    assertThat(policyChecks[0].check)
      .isEqualTo("Connection context fails to meet required policy conditions")
  }
}
