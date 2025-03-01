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

import com.google.android.libraries.pcc.chronicle.api.optics.Lens
import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleContentsProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.lens.LensPropertyProvider.Companion.lensPropertyName
import com.google.android.libraries.pcc.chronicle.codegen.util.capitalize
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName

/**
 * Implementation of [DaggerModuleContentsProvider] which generates the body of a `@Provides
 *
 * @IntoSet` method to inject the lens for the given [type]'s [field] into a multibinds set.
 */
class LensDaggerProvider(
  type: Type,
  field: FieldEntry,
  methodName: String = providerMethodName(type, field),
  private val lensPropertyName: String = lensPropertyName(type, field),
  private val lensClassName: String = "${type.name}_GeneratedKt",
) :
  DaggerModuleContentsProvider.ProvidesMethod(
    name = methodName,
    providedType = LENS_WILDCARD_TYPE,
    isSingleton = true,
    isIntoSet = true,
    qualifierAnnotations = listOf(),
  ) {
  override val parameters: List<ParameterSpec> = emptyList()

  override fun provideBody(): CodeBlock = CodeBlock.of("return $lensClassName.$lensPropertyName;")

  companion object {
    private val LENS_WILDCARD_TYPE =
      ParameterizedTypeName.get(
        ClassName.get(Lens::class.java),
        WildcardTypeName.subtypeOf(TypeName.OBJECT),
        WildcardTypeName.subtypeOf(TypeName.OBJECT),
        WildcardTypeName.subtypeOf(TypeName.OBJECT),
        WildcardTypeName.subtypeOf(TypeName.OBJECT),
      )

    private fun providerMethodName(type: Type, field: FieldEntry): String =
      "provide${type.name}${field.name.capitalize()}Lens"
  }
}
