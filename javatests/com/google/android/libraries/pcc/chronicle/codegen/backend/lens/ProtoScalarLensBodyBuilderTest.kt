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
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProtoScalarLensBodyBuilderTest {
  @Test
  fun supportsField_fieldCategoryList_returnsFalse() {
    val (type, field) =
      createTypeAndField(FieldCategory.ListValue(LIST_TYPE_LOCATION, FieldCategory.IntValue))
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryMap_returnsFalse() {
    val (type, field) =
      createTypeAndField(
        FieldCategory.MapValue(MAP_TYPE_LOCATION, FieldCategory.IntValue, FieldCategory.StringValue)
      )
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_fieldCategorySet_returnsFalse() {
    val (type, field) =
      createTypeAndField(FieldCategory.SetValue(SET_TYPE_LOCATION, FieldCategory.IntValue))
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryDurationValue_returnsFalse() {
    val (type, field) = createTypeAndField(FieldCategory.DurationValue)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryInstantValue_returnsFalse() {
    val (type, field) = createTypeAndField(FieldCategory.InstantValue)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryForeignReference_returnsFalse() {
    val (type, field) = createTypeAndField(FieldCategory.ForeignReference("schema", false))
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryOpaqueValue_returnsFalse() {
    val (type, field) = createTypeAndField(FieldCategory.OpaqueTypeValue(OPAQUE_TYPE_LOCATION))
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryBooleanValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.BooleanValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.BooleanValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryByteValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.ByteValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.ByteValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryByteArrayValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.ByteArrayValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.ByteArrayValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryCharValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.CharValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.CharValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryDoubleValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.DoubleValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.DoubleValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryEnumValue() {
    val (type1, field1) =
      createTypeAndField(
        FieldCategory.EnumValue(ENUM_TYPE_LOCATION, emptyList()),
        Type.Tooling.PROTO
      )
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) =
      createTypeAndField(
        FieldCategory.EnumValue(ENUM_TYPE_LOCATION, emptyList()),
        Type.Tooling.DATA_CLASS
      )
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryFloatValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.FloatValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.FloatValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryIntValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.IntValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.IntValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryLongValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.LongValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.LongValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryNestedValue() {
    val (type1, field1) =
      createTypeAndField(FieldCategory.NestedTypeValue(NESTED_TYPE_LOCATION), Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) =
      createTypeAndField(
        FieldCategory.NestedTypeValue(NESTED_TYPE_LOCATION),
        Type.Tooling.DATA_CLASS
      )
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryNullableValue() {
    val (type1, field1) =
      createTypeAndField(FieldCategory.NullableValue(FieldCategory.StringValue), Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) =
      createTypeAndField(
        FieldCategory.NullableValue(FieldCategory.StringValue),
        Type.Tooling.DATA_CLASS
      )
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryShortValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.ShortValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.ShortValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun supportsField_fieldCategoryStringValue() {
    val (type1, field1) = createTypeAndField(FieldCategory.StringValue, Type.Tooling.PROTO)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type1, field1)).isTrue()

    val (type2, field2) = createTypeAndField(FieldCategory.StringValue, Type.Tooling.DATA_CLASS)
    assertThat(ProtoScalarLensBodyBuilder.supportsField(type2, field2)).isFalse()
  }

  @Test
  fun buildGetterBody() {
    val block = ProtoScalarLensBodyBuilder.buildGetterBody(TYPE, FIELD, entityParamName = "entity")
    assertThat(block.toString()).contains("entity.myField")
  }

  @Test
  fun buildGetterBody_changedSourceName() {
    val block =
      ProtoScalarLensBodyBuilder.buildGetterBody(
        type = TYPE,
        field = FIELD.copy(sourceName = "lookAtMeImNew"),
        entityParamName = "entity"
      )
    assertThat(block.toString()).contains("entity.lookAtMeImNew")
  }

  @Test
  fun buildSetterBuilderCode() {
    val block =
      ProtoScalarLensBodyBuilder.buildSetterBody(
        type = TYPE,
        field = FIELD,
        entityParamName = "entity",
        newValueParamName = "newValue"
      )
    assertThat(block.toString()).contains("myField = newValue")
  }

  @Test
  fun buildSetterBuilderCode_changedSourceName() {
    val block =
      ProtoScalarLensBodyBuilder.buildSetterBody(
        type = TYPE,
        field = FIELD.copy(sourceName = "lookAtMeImNew"),
        entityParamName = "entity",
        newValueParamName = "newValue"
      )
    assertThat(block.toString()).contains("lookAtMeImNew = newValue")
  }

  companion object {
    private val NESTED_TYPE_LOCATION = TypeLocation("Nested", pkg = "com.google")
    private val ENUM_TYPE_LOCATION = TypeLocation("MyEnum", pkg = "com.google")
    private val OPAQUE_TYPE_LOCATION = TypeLocation("ActivityId", pkg = "android.app")
    private val LIST_TYPE_LOCATION = TypeLocation("List", pkg = "java.util")
    private val MAP_TYPE_LOCATION = TypeLocation("Map", pkg = "java.util")
    private val SET_TYPE_LOCATION = TypeLocation("Set", pkg = "java.util")
    private val FIELD =
      FieldEntry(
        name = "myField",
        category = FieldCategory.IntValue,
        sourceName = "myField",
        presenceCondition = "hasMyField"
      )

    private val TYPE =
      Type(
        location = TypeLocation("MyType", pkg = "com.google"),
        fields = listOf(FIELD),
        jvmLocation = TypeLocation("MyType", pkg = "com.google.jvm"),
        tooling = Type.Tooling.PROTO
      )

    private fun createTypeAndField(
      fieldCategory: FieldCategory = FieldCategory.IntValue,
      tooling: Type.Tooling = Type.Tooling.PROTO
    ): Pair<Type, FieldEntry> {
      val field = FIELD.copy(category = fieldCategory)
      val type = TYPE.copy(fields = listOf(field), tooling = tooling)
      return type to field
    }
  }
}
