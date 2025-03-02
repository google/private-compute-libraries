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

package com.google.android.libraries.pcc.chronicle.analysis

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultPolicySetTest {
  @Test
  fun findByName_whenFound_returnsPolicy() {
    val policies = DefaultPolicySet(setOf(POLICY_ONE, POLICY_TWO))

    assertThat(policies.findByName(POLICY_ONE.name)).isEqualTo(POLICY_ONE)
    assertThat(policies.findByName(POLICY_TWO.name)).isEqualTo(POLICY_TWO)
  }

  @Test
  fun findByName_whenNotFound_returnsNull() {
    val policies = DefaultPolicySet(setOf(POLICY_ONE, POLICY_TWO))

    assertThat(policies.findByName("not a real id")).isNull()
  }

  @Test
  fun contains_withContainingPolicy_returnsTrue() {
    val policies = DefaultPolicySet(setOf(POLICY_ONE, POLICY_TWO))

    assertThat(POLICY_ONE in policies).isTrue()
  }

  @Test
  fun contains_withNonContainedPolicy_returnsFalse() {
    val policies = DefaultPolicySet(setOf(POLICY_ONE))

    assertThat(POLICY_TWO in policies).isFalse()
  }

  @Test
  fun findManagementStrategy_withMultipleTargets() {
    val policies = DefaultPolicySet(setOf(POLICY_ONE, POLICY_TWO))

    val strategies = policies.findManagementStrategies(FOO_DATATYPE)

    assertThat(strategies)
      .containsExactly(
        ManagementStrategy.Stored(
          encrypted = false,
          media = StorageMedia.MEMORY,
          ttl = Duration.ofMinutes(15),
        ),
        ManagementStrategy.Stored(
          encrypted = false,
          media = StorageMedia.MEMORY,
          ttl = Duration.ofHours(1),
        ),
      )
  }

  @Test
  fun equals_sameObject_returnsTrue() {
    val policies = DefaultPolicySet(setOf(POLICY_ONE, POLICY_TWO))

    assertThat(policies).isEqualTo(policies)
  }

  @Test
  fun equals_differentType_returnsFalse() {
    val policies = DefaultPolicySet(setOf(POLICY_ONE, POLICY_TWO))

    assertThat(policies).isNotEqualTo("Duh")
  }

  @Test
  fun equals_differentPolicySet_returnsFalse() {
    val p1 = DefaultPolicySet(setOf(POLICY_ONE, POLICY_TWO))
    val p2 = DefaultPolicySet(setOf(POLICY_ONE))

    assertThat(p1).isNotEqualTo(p2)
  }

  @Test
  fun hashCode_equalsSetHashcode() {
    val rawPolicies = setOf(POLICY_ONE)
    val policies = DefaultPolicySet(rawPolicies)

    assertThat(policies.hashCode()).isEqualTo(rawPolicies.hashCode())
  }

  @Test
  fun copyOfPolicySet_returnsFalse() {
    val testSet = setOf(POLICY_ONE, POLICY_TWO)
    val p1 = DefaultPolicySet(testSet)
    val result = p1.toSet()

    assertThat(result).isEqualTo(testSet)
    assertThat(result).isNotSameInstanceAs(testSet)
  }

  companion object {
    private val FOO_DATATYPE =
      dataTypeDescriptor("Foo", Unit::class) { "field" to FieldType.String }

    private val POLICY_ONE =
      policy("Policy1", "testEgress") {
        target(FOO_DATATYPE, Duration.ofHours(1)) { retention(StorageMedium.RAM) }
      }

    private val POLICY_TWO =
      policy("Policy2", "testEgress") {
        target(FOO_DATATYPE, Duration.ofMinutes(15)) { retention(StorageMedium.RAM) }
      }
  }
}
