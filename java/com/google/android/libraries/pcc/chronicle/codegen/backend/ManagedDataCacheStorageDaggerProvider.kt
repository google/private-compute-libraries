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

import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleContentsProvider
import com.google.android.libraries.pcc.chronicle.codegen.util.upperSnake
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import java.time.Duration

/**
 * Generates the method for providing a ManagedDataCache.
 *
 * Looks like:
 *
 * ```
 * @Provides
 * @Singleton
 * static ManagedDataCache<MyClass> provideMyClassDataCacheStorage(DataCacheStorage cache) {
 *   return new ManagedDataCache<MyClass>(
 *       TypeName.class, cache, <maxSize>, <ttl>, MyClass_GeneratedKt.MY_CLASS_GENERATED_DTD);
 * }
 * ```
 *
 * @param elementName name of the class annotated with `@DataCacheStore`.
 * @param chronicleDataType the [TypeName] of the class annotated with `@DataCacheStore`.
 * @param maxItems the maximum size of the `ManagedDataCache` being generated.
 * @param ttlDuration the duration of the time-to-live for items in the generated
 * `ManagedDataCache`.
 */
class ManagedDataCacheStorageDaggerProvider(
  private val elementName: String,
  private val chronicleDataType: TypeName,
  private val maxItems: Int,
  private val ttlDuration: Duration
) :
  DaggerModuleContentsProvider.ProvidesMethod(
    name = "provide${elementName}ManagedDataCacheStorage",
    providedType = ParameterizedTypeName.get(MANAGED_DATA_CACHE, chronicleDataType),
    isSingleton = true,
    qualifierAnnotations = listOf()
  ) {
  override val parameters: List<ParameterSpec> =
    listOf(ParameterSpec.builder(DATA_CACHE_STORAGE, "cache").build())

  override fun provideBody(): CodeBlock {
    return CodeBlock.builder()
      .add("return new \$T<\$T>(\n", MANAGED_DATA_CACHE, chronicleDataType)
      .indent()
      .indent()
      .add("\$T.class,\n", chronicleDataType)
      .add("cache,\n")
      .add("\$L,\n", maxItems)
      .add("\$T.parse(\$S),\n", DURATION, ttlDuration)
      .add("${elementName}_GeneratedKt.${elementName.upperSnake()}_GENERATED_DTD")
      .add(");")
      .unindent()
      .unindent()
      .build()
  }

  companion object {
    private val DURATION = ClassName.get("java.time", "Duration")
    private val MANAGED_DATA_CACHE =
      ClassName.get(
        "com.google.android.libraries.pcc.chronicle.storage.datacache",
        "ManagedDataCache"
      )
    private val DATA_CACHE_STORAGE =
      ClassName.get(
        "com.google.android.libraries.pcc.chronicle.storage.datacache",
        "DataCacheStorage"
      )
  }
}
