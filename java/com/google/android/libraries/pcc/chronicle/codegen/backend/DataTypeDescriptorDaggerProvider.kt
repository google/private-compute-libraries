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

package com.google.android.libraries.pcc.chronicle.codegen.backend

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleContentsProvider
import com.google.android.libraries.pcc.chronicle.codegen.util.upperSnake
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName

/**
 * Generates a method for providing a [DataTypeDescriptor] into a multibinds set.
 *
 * Looks like:
 *
 * ```
 * @Provides
 * @Singleton
 * @IntoSet
 * public static DataTypeDescriptor provideMyClassDataTypeDescriptor() {
 *   return MyClass_GeneratedKt.MY_CLASS_GENERATED_DTD;
 * }
 * ```
 *
 * @param elementName name of the class annotated with `@ChronicleData`.
 * @param dtdContainingClassName the name of the class (as seen from Java) containing the DTD value.
 */
class DataTypeDescriptorDaggerProvider(
  private val elementName: CharSequence,
  private val dtdContainingClassName: String = "${elementName}_GeneratedKt"
) :
  DaggerModuleContentsProvider.ProvidesMethod(
    name = "provide${elementName}DataTypeDescriptor",
    providedType = TypeName.get(DataTypeDescriptor::class.java),
    isSingleton = true,
    isIntoSet = true,
    qualifierAnnotations = listOf()
  ) {
  override val parameters: List<ParameterSpec> = emptyList()

  override fun provideBody(): CodeBlock {
    return CodeBlock.builder()
      .addStatement("return $dtdContainingClassName.${elementName.upperSnake()}_GENERATED_DTD")
      .build()
  }
}
