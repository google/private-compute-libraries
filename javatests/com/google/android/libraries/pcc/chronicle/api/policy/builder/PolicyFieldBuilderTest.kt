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

import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PolicyFieldBuilderTest {
  @Test
  fun minimal() {
    val actual = PolicyFieldBuilder(null, listOf("foo")).build()

    assertThat(actual.annotations).isEmpty()
    assertThat(actual.fieldPath).containsExactly("foo").inOrder()
    assertThat(actual.rawUsages).isEmpty()
    assertThat(actual.redactedUsages).isEmpty()
    assertThat(actual.subfields).isEmpty()
  }

  @Test
  fun withRawUsages() {
    val actual =
      PolicyFieldBuilder(null, listOf("foo"))
        .apply { rawUsage(UsageType.JOIN, UsageType.EGRESS) }
        .build()

    assertThat(actual.annotations).isEmpty()
    assertThat(actual.fieldPath).containsExactly("foo").inOrder()
    assertThat(actual.rawUsages).containsExactly(UsageType.JOIN, UsageType.EGRESS)
    assertThat(actual.redactedUsages).isEmpty()
    assertThat(actual.subfields).isEmpty()
  }

  @Test
  fun withConditionalUsages() {
    val stringApi = PolicyFieldBuilder(null, listOf("foo"))
        .apply {
          conditionalUsage("bucketed", UsageType.JOIN, UsageType.EGRESS)
          conditionalUsage("truncatedToDays", UsageType.ANY)
        }
        .build()
    val enumApi = PolicyFieldBuilder(null, listOf("foo"))
        .apply {
          ConditionalUsage.Bucketed.whenever(UsageType.JOIN, UsageType.EGRESS)
          ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        }
        .build()
    setOf(stringApi, enumApi).forEach { actual ->
      assertThat(actual.annotations).isEmpty()
      assertThat(actual.fieldPath).containsExactly("foo").inOrder()
      assertThat(actual.rawUsages).isEmpty()
      assertThat(actual.redactedUsages)
        .containsExactly(
          "bucketed",
          setOf(UsageType.JOIN, UsageType.EGRESS),
          "truncatedToDays",
          setOf(UsageType.ANY)
        )
      assertThat(actual.subfields).isEmpty()
    }
  }

  @Test
  fun withSchema_createsPolicyField() {
    val actual =
      PolicyFieldBuilder(NESTED_PERSON_GENERATED_DTD, listOf("person"))
        .apply { "name" { rawUsage(UsageType.EGRESS) } }
        .build()
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.fieldPath).containsExactly("person").inOrder()
    assertThat(actual.rawUsages).isEmpty()
    assertThat(actual.redactedUsages).isEmpty()
    assertThat(actual.subfields)
      .containsExactly(
        PolicyField(fieldPath = listOf("person", "name"), rawUsages = setOf(UsageType.EGRESS)),
      )
  }

  @Test
  fun withSchema_createsPolicyFieldWithSubFields() {
    val actual =
      PolicyFieldBuilder(NESTED_PERSON_GENERATED_DTD, listOf("person"))
        .apply {
          "name" { rawUsage(UsageType.EGRESS) }
          "pets" { "breed" { rawUsage(UsageType.ANY) } }
        }
        .build()
    assertThat(actual.annotations).isEmpty()
    assertThat(actual.fieldPath).containsExactly("person").inOrder()
    assertThat(actual.rawUsages).isEmpty()
    assertThat(actual.redactedUsages).isEmpty()
    assertThat(actual.subfields)
      .containsExactly(
        PolicyField(fieldPath = listOf("person", "name"), rawUsages = setOf(UsageType.EGRESS)),
        PolicyField(
          fieldPath = listOf("person", "pets"),
          subfields =
            listOf(
              PolicyField(
                fieldPath = listOf("person", "pets", "breed"),
                rawUsages = setOf(UsageType.ANY),
              )
            ),
        )
      )
  }

  @Test
  fun withSchema_validatesFieldNames() {
    val e =
      assertFailsWith<IllegalArgumentException> {
        PolicyFieldBuilder(NESTED_PERSON_GENERATED_DTD, listOf("persons"))
          .apply { "pet" { "breed" { rawUsage(UsageType.ANY) } } }
          .build()
      }
    assertThat(e)
      .hasMessageThat()
      .contains("Field 'pet' not found in '${NestedPerson::class.java.name}'")
  }

  @Test
  fun withSchema_validatesNestedFieldsNames() {
    val e =
      assertFailsWith<IllegalArgumentException> {
        PolicyFieldBuilder(NESTED_PERSON_GENERATED_DTD, listOf("persons"))
          .apply { "pets" { "bread" { rawUsage(UsageType.ANY) } } }
          .build()
      }
    assertThat(e)
      .hasMessageThat()
      .contains("Field 'bread' not found in '${NestedPet::class.java.name}'")
  }

  @Test
  fun withSchema_reportsErrorWhenLookingUpFieldsInNonEntity() {
    val e =
      assertFailsWith<IllegalArgumentException> {
        PolicyFieldBuilder(NESTED_PERSON_GENERATED_DTD, listOf("persons"))
          .apply { "pets" { "breed" { "kind" { rawUsage(UsageType.ANY) } } } }
          .build()
      }
    assertThat(e).hasMessageThat().contains("Trying to lookup field 'kind' in a non-entity type.")
  }
}
