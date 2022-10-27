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

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.PropertyProvider
import com.google.android.libraries.pcc.chronicle.codegen.util.upperSnake
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName

/**
 * [PropertyProvider] implementation which generates a [PropertySpec] for a set of Chronicle
 * [Connection] classes.
 *
 * For example:
 *
 * ```
 * val MY_ENTITY_GENERATED_CONNECTIONS = setOf(
 *     MyEntityReadConnection::class.java,
 *     MyEntityWriteConnection::class.java
 *   )
 * ```
 */
data class ConnectionsPropertyProvider(
  private val entityClassSimpleName: CharSequence,
  private val connectionTypeNames: Collection<TypeName>
) : PropertyProvider() {
  override fun provideProperty(): PropertySpec {
    return PropertySpec.builder(entityClassSimpleName.toPropertyName(), SET_TYPE_NAME)
      .addAnnotation(JvmField::class)
      .initializer(
        CodeBlock.builder()
          .add("setOf(")
          .apply {
            connectionTypeNames.forEachIndexed { index, typeName ->
              if (index > 0) {
                add(", ")
              }
              add("%T::class.java", typeName)
            }
          }
          .add(")\n")
          .build()
      )
      .build()
  }

  companion object {
    private val SET_TYPE_NAME =
      Set::class
        .asClassName()
        .parameterizedBy(
          Class::class
            .asClassName()
            .parameterizedBy(WildcardTypeName.Companion.producerOf(Connection::class.asClassName()))
        )

    private fun CharSequence.toPropertyName(): String = "${upperSnake()}_GENERATED_CONNECTIONS"
  }
}
