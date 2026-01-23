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
import com.google.android.libraries.pcc.chronicle.api.optics.OpticalAccessPath
import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.PropertyProvider
import com.google.android.libraries.pcc.chronicle.codegen.typeconversion.asClassName
import com.google.android.libraries.pcc.chronicle.codegen.typeconversion.kotlinType
import com.google.android.libraries.pcc.chronicle.codegen.util.upperSnake
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName

/**
 * [PropertyProvider] implementation capable of providing a [Lens] property for a given [field]
 * within a [type].
 */
class LensPropertyProvider(
  /** The [Type] of object being targeted by the generated [Lens]. */
  val type: Type,
  /** The field within the [type] being targeted by the generated [Lens]. */
  val field: FieldEntry,
  private val propertyName: String = lensPropertyName(type, field),
  /** List of supported [LensBodyBuilders][LensBodyBuilder]. */
  private val bodyBuilders: List<LensBodyBuilder> =
    listOf(
      // TODO(b/201565936): Support generating lenses for data and autovalue classes.
      ProtoScalarLensBodyBuilder,
      ProtoRepeatedLensBodyBuilder,
      ProtoMapLensBodyBuilder,
    ),
) : PropertyProvider() {
  private val typeClassName = type.jvmLocation.asClassName()
  private val fieldClassName = field.category.kotlinType()

  override fun provideProperty(): PropertySpec {
    val bodyBuilder =
      requireNotNull(bodyBuilders.find { it.supportsField(type, field) }) {
        "Lens generation for $field of $type is not currently supported"
      }
    val lensTypeName =
      Lens::class.asClassName()
        .parameterizedBy(typeClassName, typeClassName, fieldClassName, fieldClassName)
    return PropertySpec.builder(propertyName, lensTypeName)
      .addAnnotation(JvmField::class)
      .initializer(createLensInitializer(bodyBuilder))
      .build()
  }

  private fun createLensInitializer(bodyBuilder: LensBodyBuilder): CodeBlock {
    return CodeBlock.builder()
      .add(
        format =
          """
          %T.create<%T, %T>(
            focusAccessPath = %T(%S, %S),
          """
            .trimIndent(),
        Lens::class.asClassName(),
        typeClassName,
        fieldClassName,
        OpticalAccessPath::class,
        type.location.toString(),
        field.name,
      )
      .add("\n")
      .indent()
      .add("getter = { entity ->\n")
      .indent()
      .add(bodyBuilder.buildGetterBody(type, field, entityParamName = "entity"))
      .unindent()
      .add("},\n")
      .add("setter = { entity, newValue ->\n")
      .indent()
      .add(
        bodyBuilder.buildSetterBody(
          type,
          field,
          entityParamName = "entity",
          newValueParamName = "newValue",
        )
      )
      .unindent()
      .add("}\n")
      .unindent()
      .add(")")
      .build()
  }

  companion object {
    /**
     * Returns a generated property name for a lens based on the provided [type]'s given [field].
     */
    fun lensPropertyName(type: Type, field: FieldEntry): String =
      type.name.upperSnake() + "_" + field.name.upperSnake() + "_GENERATED_LENS"
  }
}
