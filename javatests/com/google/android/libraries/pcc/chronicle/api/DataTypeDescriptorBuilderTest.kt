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
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataTypeDescriptorBuilderTest {
  @Test
  fun minimal() {
    val actual = dataTypeDescriptor("Foo", Foo::class)
    assertThat(actual).isEqualTo(DataTypeDescriptor("Foo", emptyMap(), emptySet(), Foo::class))
  }

  @Test
  fun withPrimitiveFields() {
    val actual =
      dataTypeDescriptor("Foo", Foo::class) {
        "name" to FieldType.String
        "age" to FieldType.Integer
      }

    assertThat(actual)
      .isEqualTo(
        DataTypeDescriptor(
          name = "Foo",
          fields = mapOf("name" to FieldType.String, "age" to FieldType.Integer),
          innerTypes = emptySet(),
          cls = Foo::class,
        )
      )
  }

  @Test
  fun withNestedFields_byNested() {
    val actual =
      dataTypeDescriptor("Foo", Foo::class) {
        "other1" to FieldType.Nested("Other")
        "other2" to FieldType.Nested("Other")
      }

    assertThat(actual).isEqualTo(DTD_WITH_NESTED_FIELDS)
  }

  @Test
  fun withInnerTypes() {
    val actual =
      dataTypeDescriptor("Foo", Foo::class) {
        "other1" to
          dataTypeDescriptor("Other", Other::class) {
            "name" to FieldType.String
            "age" to FieldType.Integer
          }
        "other2" to
          dataTypeDescriptor("Other", Other::class) {
            "name" to FieldType.String
            "age" to FieldType.Integer
          }
      }

    assertThat(actual).isEqualTo(DTD_WITH_INNER_TYPES)
  }

  @Test
  fun withListOfInnerTypes() {
    val actual =
      dataTypeDescriptor("Foo", Foo::class) {
        "other1" to
          FieldType.List(
            dataTypeDescriptor("Other", Other::class) {
              "name" to FieldType.String
              "age" to FieldType.Integer
            }
          )
        "other2" to
          FieldType.List(
            dataTypeDescriptor("Other", Other::class) {
              "name" to FieldType.String
              "age" to FieldType.Integer
            }
          )
      }

    assertThat(actual).isEqualTo(DTD_WITH_LIST_OF_INNER_TYPES)
  }

  @Test
  fun withArrayOfInnerTypes() {
    val actual =
      dataTypeDescriptor("Foo", Foo::class) {
        "other1" to
          FieldType.Array(
            dataTypeDescriptor("Other", Other::class) {
              "name" to FieldType.String
              "age" to FieldType.Integer
            }
          )
        "other2" to
          FieldType.Array(
            dataTypeDescriptor("Other", Other::class) {
              "name" to FieldType.String
              "age" to FieldType.Integer
            }
          )
      }

    assertThat(actual).isEqualTo(DTD_WITH_ARRAY_OF_INNER_TYPES)
  }

  @Test
  fun redeclaringInnerTypeWithDifferentStructure_throws() {
    dataTypeDescriptor("Foo", Foo::class) {
      "other1" to
        dataTypeDescriptor("Other", Other::class) {
          "name" to FieldType.String
          "age" to FieldType.Integer
        }

      assertFailsWith<IllegalArgumentException> {
        "other1" to dataTypeDescriptor("Other", Other::class) { "age" to FieldType.Integer }
      }
    }
  }

  data class Other(val name: String, val age: Int)

  data class Foo(val other1: Other, val other2: Other)

  companion object {

    private val DTD_WITH_NESTED_FIELDS =
      DataTypeDescriptor(
        name = "Foo",
        fields =
          mapOf("other1" to FieldType.Nested("Other"), "other2" to FieldType.Nested("Other")),
        innerTypes = emptySet(),
        cls = Foo::class,
      )

    private val DTD_WITH_INNER_TYPES =
      DTD_WITH_NESTED_FIELDS.copy(
        innerTypes =
          setOf(
            DataTypeDescriptor(
              name = "Other",
              fields = mapOf("name" to FieldType.String, "age" to FieldType.Integer),
              innerTypes = emptySet(),
              cls = Other::class,
            )
          )
      )

    private val DTD_WITH_LIST_OF_INNER_TYPES =
      DTD_WITH_NESTED_FIELDS.copy(
        fields =
          mapOf(
            "other1" to FieldType.List(FieldType.Nested("Other")),
            "other2" to FieldType.List(FieldType.Nested("Other")),
          ),
        innerTypes =
          setOf(
            DataTypeDescriptor(
              name = "Other",
              fields = mapOf("name" to FieldType.String, "age" to FieldType.Integer),
              innerTypes = emptySet(),
              cls = Other::class,
            )
          ),
      )

    private val DTD_WITH_ARRAY_OF_INNER_TYPES =
      DTD_WITH_NESTED_FIELDS.copy(
        fields =
          mapOf(
            "other1" to FieldType.Array(FieldType.Nested("Other")),
            "other2" to FieldType.Array(FieldType.Nested("Other")),
          ),
        innerTypes =
          setOf(
            DataTypeDescriptor(
              name = "Other",
              fields = mapOf("name" to FieldType.String, "age" to FieldType.Integer),
              innerTypes = emptySet(),
              cls = Other::class,
            )
          ),
      )
  }
}
