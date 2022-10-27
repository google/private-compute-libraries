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
 * Implementation of [LensBodyBuilder] intended to construct lenses for `repeated` fields of protos.
 */
object ProtoRepeatedLensBodyBuilder : BaseProtoLensBodyBuilder() {
  override fun supportsField(type: Type, field: FieldEntry): Boolean =
    field.category is FieldCategory.ListValue && type.tooling == Type.Tooling.PROTO

  override fun buildGetterBody(type: Type, field: FieldEntry, entityParamName: String): CodeBlock =
    CodeBlock.builder().addStatement("$entityParamName.${field.sourceName}").build()

  override fun buildSetterBuilderCode(
    type: Type,
    field: FieldEntry,
    entityParamName: String,
    newValueParamName: String
  ): CodeBlock {
    return CodeBlock.builder()
      .addStatement("clear${field.name.capitalize()}()")
      .addStatement("addAll${field.name.capitalize()}($newValueParamName)")
      .build()
  }
}
