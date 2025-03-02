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

package com.google.android.libraries.pcc.chronicle.api.policy.proto

import arcs.core.data.proto.PolicyProto
import arcs.core.data.proto.PolicyRetentionProto
import arcs.core.data.proto.PolicyTargetProto
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyRetention
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyTarget
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.Annotation
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.All
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PolicyProtoTest {
  /** Arbitrary rule that is intentionally separate from AllowedAllContextsRule for tests */
  private object TestContextRule : PolicyContextRule {
    override val name: String = "TestContextRule"
    override val operands: List<PolicyContextRule> = emptyList()

    override fun invoke(context: TypedMap): Boolean = true
  }

  @Test
  fun roundTrip_policy() {
    val policy =
      Policy(
        name = "foo",
        description = "bar",
        egressType = "baz",
        targets = emptyList(),
        configs = emptyMap(),
        annotations = listOf(ANNOTATION),
      )
    assertThat(policy.encode().decode()).isEqualTo(policy)
  }

  @Test
  fun roundTrip_policy_withAllowedContext() {
    val policy =
      Policy(
        name = "foo",
        description = "bar",
        egressType = "baz",
        targets = emptyList(),
        configs = emptyMap(),
        annotations = listOf(ANNOTATION),
        allowedContext = All,
      )
    assertThat(policy.encode().decode()).isEqualTo(policy)
  }

  @Test
  fun encode_policy_requiresAllContextsRule() {
    val policy =
      Policy(
        name = "foo",
        description = "bar",
        egressType = "baz",
        targets = emptyList(),
        configs = emptyMap(),
        annotations = listOf(ANNOTATION),
        allowedContext = TestContextRule,
      )

    val e = assertFailsWith<IllegalArgumentException> { policy.encode() }
    assertThat(e).hasMessageThat().startsWith("allowedContext must allow all contexts.")
  }

  @Test
  fun decode_policy_requiresName() {
    val e = assertFailsWith<IllegalArgumentException> { PolicyProto.getDefaultInstance().decode() }
    assertThat(e).hasMessageThat().startsWith("Policy name is missing.")
  }

  @Test
  fun decode_policy_requiresEgressType() {
    val e =
      assertFailsWith<IllegalArgumentException> {
        PolicyProto.newBuilder().setName("foo").build().decode()
      }
    assertThat(e).hasMessageThat().startsWith("Egress type is missing.")
  }

  @Test
  fun roundTrip_target() {
    val policy =
      Policy(
        name = "foo",
        egressType = "Logging",
        targets =
          listOf(
            PolicyTarget(
              schemaName = "schema",
              maxAgeMs = 123,
              retentions =
                listOf(PolicyRetention(medium = StorageMedium.DISK, encryptionRequired = true)),
              fields = emptyList(),
              annotations = listOf(ANNOTATION),
            )
          ),
      )
    assertThat(policy.encode().decode()).isEqualTo(policy)
  }

  @Test
  fun decode_retention_requiresMedium() {
    val proto =
      PolicyProto.newBuilder()
        .setName("foo")
        .setEgressType("Logging")
        .addTargets(
          PolicyTargetProto.newBuilder().addRetentions(PolicyRetentionProto.getDefaultInstance())
        )
        .build()
    val e = assertFailsWith<UnsupportedOperationException> { proto.decode() }
    assertThat(e).hasMessageThat().startsWith("Unknown retention medium:")
  }

  @Test
  fun roundTrip_fields() {
    val policy =
      Policy(
        name = "foo",
        egressType = "Logging",
        targets =
          listOf(
            PolicyTarget(
              schemaName = "schema",
              retentions =
                listOf(PolicyRetention(medium = StorageMedium.RAM, encryptionRequired = true)),
              fields =
                listOf(
                  PolicyField(
                    fieldPath = listOf("field1"),
                    rawUsages = setOf(UsageType.JOIN),
                    redactedUsages = mapOf("label" to setOf(UsageType.EGRESS, UsageType.JOIN)),
                    subfields = emptyList(),
                    annotations = listOf(ANNOTATION),
                  ),
                  PolicyField(
                    fieldPath = listOf("field2"),
                    rawUsages = setOf(UsageType.ANY),
                    subfields = emptyList(),
                    annotations = listOf(ANNOTATION),
                  ),
                ),
            )
          ),
      )
    assertThat(policy.encode().decode()).isEqualTo(policy)
  }

  @Test
  fun roundTrip_subfields() {
    val policy =
      Policy(
        name = "foo",
        egressType = "Logging",
        targets =
          listOf(
            PolicyTarget(
              schemaName = "schema",
              fields =
                listOf(
                  PolicyField(
                    fieldPath = listOf("parent"),
                    rawUsages = emptySet(),
                    redactedUsages = emptyMap(),
                    subfields =
                      listOf(
                        PolicyField(
                          fieldPath = listOf("parent", "child"),
                          rawUsages = emptySet(),
                          redactedUsages = emptyMap(),
                          subfields = emptyList(),
                          annotations = emptyList(),
                        )
                      ),
                    annotations = emptyList(),
                  )
                ),
            )
          ),
      )
    assertThat(policy.encode().decode()).isEqualTo(policy)
  }

  @Test
  fun roundTrip_configs() {
    val policy =
      Policy(
        name = "foo",
        egressType = "Logging",
        configs = mapOf("config" to mapOf("k1" to "v1", "k2" to "v2")),
      )
    assertThat(policy.encode().decode()).isEqualTo(policy)
  }

  companion object {
    val ANNOTATION = Annotation("custom", mapOf())
  }
}
