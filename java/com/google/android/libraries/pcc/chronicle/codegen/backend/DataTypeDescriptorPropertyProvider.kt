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
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.PropertyProvider
import com.google.android.libraries.pcc.chronicle.codegen.util.upperSnake
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * [PropertyProvider] implementation which generates a [PropertySpec] for a [DataTypeDescriptor].
 *
 * For example:
 * ```
 * val MY_ENTITY_GENERATED_DTD: DataTypeDescriptor = dataTypeDescriptor("com.google.MyEntity") {
 *     "foo" to FieldType.String
 *     "bar" to FieldType.Long
 *   }
 * ```
 */
data class DataTypeDescriptorPropertyProvider(
  private val entityClassSimpleName: CharSequence,
  private val typeSet: TypeSet,
  private val propertyName: String = entityClassSimpleName.toPropertyName(),
) : PropertyProvider() {
  override fun provideProperty(): PropertySpec {
    return PropertySpec.builder(propertyName, DataTypeDescriptor::class)
      .addAnnotations(
        listOf(
          AnnotationSpec.builder(SuppressWarnings::class.java).addMember("\"deprecation\"").build(),
          AnnotationSpec.builder(Suppress::class.java).addMember("\"DEPRECATION\"").build(),
          AnnotationSpec.builder(JvmField::class.java).build(),
        )
      )
      .initializer(typeSet.dataTypeDescriptor())
      .build()
  }

  companion object {
    private fun CharSequence.toPropertyName(): String = "${upperSnake()}_GENERATED_DTD"
  }
}
