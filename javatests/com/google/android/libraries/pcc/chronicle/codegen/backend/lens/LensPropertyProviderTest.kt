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

package com.google.android.libraries.pcc.chronicle.codegen.backend.lens

import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory
import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.FileSpec
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LensPropertyProviderTest {
  @Parameterized.Parameter lateinit var testCase: TestCase

  @Test
  fun provideProperty() {
    val provider = LensPropertyProvider(TYPE, testCase.field)
    val fileSpec =
      FileSpec.builder("com.google.test", "MyFile.kt")
        .addProperty(provider.provideProperty())
        .build()

    assertThat(fileSpec.toString().trim()).isEqualTo(loadGolden(testCase.goldenFile))
  }

  @Test
  fun provideProperty_throwsWhenTypeIsUnsupported_unknown() {
    val provider = LensPropertyProvider(TYPE.copy(tooling = Type.Tooling.UNKNOWN), testCase.field)

    assertFailsWith<IllegalArgumentException> { provider.provideProperty() }
  }

  @Test
  fun provideProperty_throwsWhenTypeIsUnsupported_autoValue() {
    val provider = LensPropertyProvider(TYPE.copy(tooling = Type.Tooling.AUTOVALUE), testCase.field)

    assertFailsWith<IllegalArgumentException> { provider.provideProperty() }
  }

  @Test
  fun provideProperty_throwsWhenTypeIsUnsupported_dataClass() {
    val provider =
      LensPropertyProvider(TYPE.copy(tooling = Type.Tooling.DATA_CLASS), testCase.field)

    assertFailsWith<IllegalArgumentException> { provider.provideProperty() }
  }

  private fun loadGolden(fileName: String): String {
    return this::class
      .java
      .getResourceAsStream(fileName)
      ?.bufferedReader()
      ?.lineSequence()
      ?.joinToString("\n")
      ?.trim() ?: ""
  }

  data class TestCase(val field: FieldEntry, val goldenFile: String) {
    override fun toString(): String = field.name
  }

  companion object {
    private val LIST_TYPE_LOCATION = TypeLocation("List", pkg = "java.util")
    private val MAP_TYPE_LOCATION = TypeLocation("Map", pkg = "java.util")
    private val PROTO_TYPE_LOCATION = TypeLocation("TestProto", pkg = "proto")
    private val JVM_TYPE_LOCATION = TypeLocation("TestProto", pkg = "com.google")

    private val INT_FIELD =
      FieldEntry(name = "intField", category = FieldCategory.IntValue, sourceName = "intField")
    private val NULLABLE_INT_FIELD =
      FieldEntry(
        name = "nullableIntField",
        category = FieldCategory.NullableValue(FieldCategory.IntValue),
        sourceName = "nullableIntField",
      )
    private val STRING_FIELD =
      FieldEntry(
        name = "stringField",
        category = FieldCategory.StringValue,
        sourceName = "stringField",
      )
    private val NULLABLE_STRING_FIELD =
      FieldEntry(
        name = "nullableStringField",
        category = FieldCategory.NullableValue(FieldCategory.StringValue),
        sourceName = "nullableStringField",
      )
    private val LIST_FIELD =
      FieldEntry(
        name = "listField",
        category = FieldCategory.ListValue(LIST_TYPE_LOCATION, FieldCategory.StringValue),
        sourceName = "listFieldList",
      )
    private val NESTED_FIELD =
      FieldEntry(
        name = "nestedField",
        category = FieldCategory.NestedTypeValue(PROTO_TYPE_LOCATION, JVM_TYPE_LOCATION),
        sourceName = "nestedField",
      )
    private val NULLABLE_NESTED_FIELD =
      FieldEntry(
        name = "nullableNestedField",
        category =
          FieldCategory.NullableValue(
            FieldCategory.NestedTypeValue(PROTO_TYPE_LOCATION, JVM_TYPE_LOCATION)
          ),
        sourceName = "nullableNestedField",
      )
    private val MAP_FIELD =
      FieldEntry(
        name = "mapField",
        category =
          FieldCategory.MapValue(
            location = MAP_TYPE_LOCATION,
            keyType = FieldCategory.StringValue,
            valueType = FieldCategory.LongValue,
          ),
        sourceName = "mapFieldMap",
      )
    private val FIELDS =
      listOf(
        INT_FIELD,
        NULLABLE_INT_FIELD,
        STRING_FIELD,
        NULLABLE_STRING_FIELD,
        LIST_FIELD,
        MAP_FIELD,
        NESTED_FIELD,
        NULLABLE_NESTED_FIELD,
      )

    private val TYPE =
      Type(
        location = PROTO_TYPE_LOCATION,
        fields = FIELDS,
        jvmLocation = JVM_TYPE_LOCATION,
        tooling = Type.Tooling.PROTO,
      )

    @JvmStatic
    @get:Parameterized.Parameters(name = "{0}")
    val TEST_CASES =
      listOf(
        TestCase(INT_FIELD, "goldens/proto/IntGolden.txt"),
        TestCase(NULLABLE_INT_FIELD, "goldens/proto/NullableIntGolden.txt"),
        TestCase(STRING_FIELD, "goldens/proto/StringGolden.txt"),
        TestCase(NULLABLE_STRING_FIELD, "goldens/proto/NullableStringGolden.txt"),
        TestCase(LIST_FIELD, "goldens/proto/ListGolden.txt"),
        TestCase(MAP_FIELD, "goldens/proto/MapGolden.txt"),
        TestCase(NESTED_FIELD, "goldens/proto/NestedGolden.txt"),
        TestCase(NULLABLE_NESTED_FIELD, "goldens/proto/NullableNestedGolden.txt"),
      )
  }
}
