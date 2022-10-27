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
import com.google.android.libraries.pcc.chronicle.codegen.util.capitalize
import com.squareup.kotlinpoet.CodeBlock

/**
 * [LensBodyBuilder] implementation used to generate bodies of lenses for scalar fields of protos.
 *
 * A scalar field is defined as one which is not a `repeated`, map, set, duration, instant,
 * foreign-reference, or an opaque type.
 */
object ProtoScalarLensBodyBuilder : BaseProtoLensBodyBuilder() {
  override fun supportsField(type: Type, field: FieldEntry): Boolean {
    return when (field.category) {
      is FieldCategory.ListValue,
      is FieldCategory.MapValue,
      is FieldCategory.SetValue,
      FieldCategory.DurationValue,
      FieldCategory.InstantValue,
      is FieldCategory.ForeignReference,
      is FieldCategory.OpaqueTypeValue,
      is FieldCategory.TupleValue -> false
      FieldCategory.BooleanValue,
      FieldCategory.ByteValue,
      FieldCategory.ByteArrayValue,
      FieldCategory.CharValue,
      FieldCategory.DoubleValue,
      is FieldCategory.EnumValue,
      FieldCategory.FloatValue,
      FieldCategory.IntValue,
      FieldCategory.LongValue,
      is FieldCategory.NestedTypeValue,
      is FieldCategory.NullableValue,
      FieldCategory.ShortValue,
      FieldCategory.StringValue -> type.tooling == Type.Tooling.PROTO
    }
  }

  override fun buildGetterBody(type: Type, field: FieldEntry, entityParamName: String): CodeBlock {
    if (field.category !is FieldCategory.NullableValue) {
      return CodeBlock.builder().addStatement("$entityParamName.${field.sourceName}").build()
    }
    return CodeBlock.builder()
      .beginControlFlow("if ($entityParamName.has${field.sourceName.capitalize()}())")
      .addStatement("$entityParamName.${field.sourceName}")
      .nextControlFlow("else")
      .addStatement("null")
      .endControlFlow()
      .build()
  }

  override fun buildSetterBuilderCode(
    type: Type,
    field: FieldEntry,
    entityParamName: String,
    newValueParamName: String
  ): CodeBlock {
    if (field.category !is FieldCategory.NullableValue) {
      return CodeBlock.builder().addStatement("${field.sourceName} = $newValueParamName").build()
    }

    return CodeBlock.builder()
      .addStatement(
        "${field.sourceName} = requireNotNull($newValueParamName) { %S }",
        "Optional proto field: ${field.sourceName} may only take on non-null values."
      )
      .build()
  }
}
