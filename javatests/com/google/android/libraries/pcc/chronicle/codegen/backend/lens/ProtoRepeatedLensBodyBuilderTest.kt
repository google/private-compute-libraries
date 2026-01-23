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
class ProtoRepeatedLensBodyBuilderTest {
  @Test
  fun supportsField() {
    assertThat(ProtoRepeatedLensBodyBuilder.supportsField(TYPE, FIELD)).isTrue()
  }

  @Test
  fun supportsField_notListValue_returnsFalse() {
    val field = FIELD.copy(category = FieldCategory.ByteValue)
    val type = TYPE.copy(fields = listOf(FIELD))

    assertThat(ProtoRepeatedLensBodyBuilder.supportsField(type, field)).isFalse()
  }

  @Test
  fun supportsField_notProtoTooling_returnsFalse() {
    val type = TYPE.copy(tooling = Type.Tooling.AUTOVALUE)

    assertThat(ProtoRepeatedLensBodyBuilder.supportsField(type, FIELD)).isFalse()
  }

  @Test
  fun buildGetterBody() {
    val body = ProtoRepeatedLensBodyBuilder.buildGetterBody(TYPE, FIELD, "entity")

    assertThat(body.toString()).contains("entity.myFieldList")
  }

  @Test
  fun buildSetterBuilderCode() {
    val builderCode =
      ProtoRepeatedLensBodyBuilder.buildSetterBuilderCode(TYPE, FIELD, "entity", "newValue")

    assertThat(builderCode.toString())
      .contains(
        """
        clearMyField()
        addAllMyField(newValue)
        """
          .trimIndent()
      )
  }

  companion object {
    private val LIST_LOCATION = TypeLocation("List", pkg = "java.util")
    private val FIELD =
      FieldEntry(
        name = "myField",
        category = FieldCategory.ListValue(LIST_LOCATION, FieldCategory.StringValue),
        sourceName = "myFieldList",
      )
    private val TYPE =
      Type(
        location = TypeLocation("MyProto", pkg = "com.google"),
        fields = listOf(FIELD),
        tooling = Type.Tooling.PROTO,
      )
  }
}
