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

import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyRetention
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PolicyTargetBuilderTest {
  @Test
  fun minimal() {
    val actual =
      target(dataTypeDescriptor("MySchema", Unit::class), maxAge = Duration.ofMillis(1234))

    assertThat(actual.schemaName).isEqualTo("MySchema")
    assertThat(actual.maxAgeMs).isEqualTo(1234L)
    assertThat(actual.fields).isEmpty()
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.retentions).isEmpty()
  }

  @Test
  fun withRetention() {
    val actual =
      target(MY_SCHEMA_DTD, maxAge = Duration.ZERO) {
        retention(StorageMedium.DISK, encryptionRequired = true)
        retention(StorageMedium.RAM)
      }

    assertThat(actual.schemaName).isEqualTo("MySchema")
    assertThat(actual.maxAgeMs).isEqualTo(0L)
    assertThat(actual.fields).isEmpty()
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.retentions)
      .containsExactly(
        PolicyRetention(StorageMedium.DISK, encryptionRequired = true),
        PolicyRetention(StorageMedium.RAM),
      )
  }

  @Test
  fun withField() {
    val actual =
      target(MY_SCHEMA_DTD, maxAge = Duration.ZERO) { "name" { rawUsage(UsageType.EGRESS) } }

    assertThat(actual.schemaName).isEqualTo("MySchema")
    assertThat(actual.maxAgeMs).isEqualTo(0L)
    assertThat(actual.fields)
      .containsExactly(
        PolicyFieldBuilder(null, listOf("name")).apply { rawUsage(UsageType.EGRESS) }.build()
      )
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.retentions).isEmpty()
  }

  @Test
  fun withOpaqueField() {
    val actual =
      target(MY_SCHEMA_WITH_OPAQUE_DTD, maxAge = Duration.ZERO) {
        "binder" { rawUsage(UsageType.EGRESS) }
      }

    assertThat(actual.schemaName).isEqualTo("MySchemaWithOpaque")
    assertThat(actual.maxAgeMs).isEqualTo(0L)
    assertThat(actual.fields)
      .containsExactly(
        PolicyFieldBuilder(null, listOf("binder")).apply { rawUsage(UsageType.EGRESS) }.build()
      )
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.retentions).isEmpty()
  }

  @Test
  fun withTupleFields() {
    val actual =
      target(MY_SCHEMA_WITH_TUPLES_DTD, maxAge = Duration.ZERO) {
        "nameAndPrice" { rawUsage(UsageType.EGRESS) }
        "nameWeightAndAge" { rawUsage(UsageType.EGRESS) }
      }

    assertThat(actual.schemaName).isEqualTo("MySchemaWithTuples")
    assertThat(actual.maxAgeMs).isEqualTo(0L)
    assertThat(actual.fields)
      .containsExactly(
        PolicyFieldBuilder(null, listOf("nameAndPrice"))
          .apply { rawUsage(UsageType.EGRESS) }
          .build(),
        PolicyFieldBuilder(null, listOf("nameWeightAndAge"))
          .apply { rawUsage(UsageType.EGRESS) }
          .build(),
      )
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.retentions).isEmpty()
  }

  @Test
  fun withEnumFields() {
    val actual =
      target(MY_SCHEMA_WITH_ENUM_DTD, maxAge = Duration.ZERO) {
        "status" { rawUsage(UsageType.EGRESS) }
      }

    assertThat(actual.schemaName).isEqualTo("MySchemaWithEnum")
    assertThat(actual.maxAgeMs).isEqualTo(0L)
    assertThat(actual.fields)
      .containsExactly(
        PolicyFieldBuilder(null, listOf("status")).apply { rawUsage(UsageType.EGRESS) }.build()
      )
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.retentions).isEmpty()
  }

  @Test
  fun withSchema_createsPolicyTarget() {
    val actual =
      target(TEST_PERSON_GENERATED_DTD, maxAge = Duration.ZERO) {
        "persons" { "pets" { "breed" { rawUsage(UsageType.ANY) } } }
      }
    assertThat(actual.schemaName)
      .isEqualTo("com.google.android.libraries.pcc.chronicle.api.policy.builder.TestPerson")
    assertThat(actual.maxAgeMs).isEqualTo(0L)
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.retentions).isEmpty()
    assertThat(actual.fields)
      .containsExactly(
        PolicyField(
          fieldPath = listOf("persons"),
          subfields =
            listOf(
              PolicyField(
                fieldPath = listOf("persons", "pets"),
                subfields =
                  listOf(
                    PolicyField(
                      fieldPath = listOf("persons", "pets", "breed"),
                      rawUsages = setOf(UsageType.ANY),
                    )
                  ),
              )
            ),
        )
      )
  }

  @Test
  fun withSchema_validatesFieldNames() {
    val e =
      assertFailsWith<IllegalArgumentException> {
        target(TEST_PERSON_GENERATED_DTD, maxAge = Duration.ZERO) {
          "persons" {
            // testing typo "pet" rather than "pets"
            "pet" { "breed" { rawUsage(UsageType.ANY) } }
          }
        }
      }
    assertThat(e)
      .hasMessageThat()
      .contains(
        "Field 'pet' not found in 'com.google.android.libraries.pcc.chronicle.api.policy" +
          ".builder.NestedPerson'"
      )
  }

  @Test
  fun withSchema_validatesNestedFieldsNames() {
    val e =
      assertFailsWith<IllegalArgumentException> {
        target(TEST_PERSON_GENERATED_DTD, maxAge = Duration.ZERO) {
          "persons" { "pets" { "bread" { rawUsage(UsageType.ANY) } } }
        }
      }
    assertThat(e)
      .hasMessageThat()
      .contains(
        "Field 'bread' not found in 'com.google.android.libraries.pcc.chronicle.api.policy" +
          ".builder.NestedPet'"
      )
  }

  @Test
  fun deletionTrigger_addsExpectedAnnotation() {
    val policy =
      policy("test", "test") {
        target(TEST_PERSON_GENERATED_DTD, Duration.ZERO) {
          deletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pets")
        }
      }
    assertThat(policy.targets[0].deletionTriggers())
      .containsExactly(DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pets"))
  }

  companion object {
    private val MY_SCHEMA_DTD =
      dataTypeDescriptor("MySchema", Unit::class) { "name" to FieldType.String }
    private val MY_SCHEMA_WITH_OPAQUE_DTD =
      dataTypeDescriptor("MySchemaWithOpaque", Unit::class) {
        "binder" to FieldType.Opaque("android.os.IBinder")
      }
    private val MY_SCHEMA_WITH_TUPLES_DTD =
      dataTypeDescriptor("MySchemaWithTuples", Unit::class) {
        "nameAndPrice" to FieldType.Tuple(listOf(FieldType.String, FieldType.Double))
        "nameWeightAndAge" to
          FieldType.Tuple(listOf(FieldType.String, FieldType.Double, FieldType.Integer))
      }
    private val MY_SCHEMA_WITH_ENUM_DTD =
      dataTypeDescriptor("MySchemaWithEnum", Unit::class) {
        "status" to FieldType.Enum("Status", listOf("ON", "OFF"))
      }
  }
}
