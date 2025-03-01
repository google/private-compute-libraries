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

import com.google.android.libraries.pcc.chronicle.codegen.backend.api.FileSpecContentsProvider
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

/**
 * Generates implementations of a [com.google.android.libraries.pcc.chronicle.api.ReadConnection]
 * and [com.google.android.libraries.pcc.chronicle.api.WriteConnection] and a corresponding
 * ConnectionProvider for each given `ReadConnection`, `WriteConnection` and `DataClass`.
 *
 * For example:
 * ```
 * class ${ReadConnection}Impl(cache: TypedDataCacheReader<${DataClass}>) :
 *   ${ReadConnection}, DataCacheReader<${DataClass}> by DataCacheReader.createDefault(cache)
 *
 * class ${WriteConnection}Impl(cache: TypedDataCacheWriter<${DataClass}>, timeSource: TimeSource) :
 *   ${WriteConnection},
 *   DataCacheWriter<${DataClass}> by DataCacheWriter.createDefault(cache, timeSource)
 *
 * class ${DataClass}ConnectionProvider(
 *   cache: ManagedDataCache<${DataClass}>,
 *   timeSource: TimeSource,
 * ) :
 *   DefaultManagedDataCacheConnectionProvider<${DataClass}>(
 *     cache,
 *     mapOf(
 *       ${ReadConnection}::class.java to { ${ReadConnection}Impl(cache) },
 *       ${WriteConnection}::class.java to { ${WriteConnection}Impl(cache, timeSource) },
 *     )
 *   )
 *
 * ```
 */
class ConnectionProviderTypeProvider(
  val dataClass: TypeName,
  val readerConnections: List<TypeName>,
  val writerConnections: List<TypeName>,
) : FileSpecContentsProvider {
  override fun provideContentsInto(builder: FileSpec.Builder) {
    val connectionMappings = mutableListOf<CodeBlock>()

    readerConnections.forEach {
      val readerImpl = buildReaderConnectionImpl(it)
      builder.addType(readerImpl)
      val readerMap = buildReaderMapEntry(it, readerImpl)
      connectionMappings.add(readerMap)
    }
    writerConnections.forEach {
      val impl = buildWriterConnectionImpl(it)
      builder.addType(impl)
      val mapEntry = buildWriterMapEntry(it, impl)
      connectionMappings.add(mapEntry)
    }

    val connectionProvider = buildConnectionProvider(connectionMappings)
    builder.addType(connectionProvider)
  }

  private fun buildReaderConnectionImpl(readerConnection: TypeName) =
    TypeSpec.classBuilder("${readerConnection.name}Impl")
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter("cache", TYPED_DATA_CACHE_READER.parameterizedBy(dataClass))
          .build()
      )
      .addSuperinterface(readerConnection)
      .addSuperinterface(
        DATA_CACHE_READER.parameterizedBy(dataClass),
        CodeBlock.of("%N.createDefault(cache)", DATA_CACHE_READER.simpleName),
      )
      .build()

  private fun buildWriterConnectionImpl(writerConnection: TypeName) =
    TypeSpec.classBuilder("${writerConnection.name}Impl")
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter("cache", TYPED_DATA_CACHE_WRITER.parameterizedBy(dataClass))
          .addParameter("timeSource", TIME_SOURCE)
          .build()
      )
      .addSuperinterface(writerConnection)
      .addSuperinterface(
        DATA_CACHE_WRITER.parameterizedBy(dataClass),
        CodeBlock.of("%N.createDefault(cache, timeSource)", DATA_CACHE_WRITER.simpleName),
      )
      .build()

  private fun buildReaderMapEntry(readerConnection: TypeName, readerImpl: TypeSpec) =
    CodeBlock.of("%N::class.java to { %N(cache) }", readerConnection.name, readerImpl)

  private fun buildWriterMapEntry(writerConnection: TypeName, writerImpl: TypeSpec) =
    CodeBlock.of("%N::class.java to { %N(cache, timeSource) }", writerConnection.name, writerImpl)

  private fun buildConnectionProvider(connectionMappings: List<CodeBlock>) =
    TypeSpec.classBuilder("${dataClass.name}ConnectionProvider")
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter("cache", MANAGED_DATA_CACHE.parameterizedBy(dataClass))
          .addParameter("timeSource", TIME_SOURCE)
          .build()
      )
      .superclass(DEFAULT_MANAGED_DATA_CACHE_CONNECTION_PROVIDER.parameterizedBy(dataClass))
      .addSuperclassConstructorParameter("cache")
      .addSuperclassConstructorParameter(
        "mapOf(%L)",
        CodeBlock.of("%L, ".repeat(connectionMappings.size), *connectionMappings.toTypedArray()),
      )
      .build()

  companion object {
    private const val UTIL_PACKAGE_NAME = "com.google.android.libraries.pcc.chronicle.util"
    private val TIME_SOURCE = ClassName(UTIL_PACKAGE_NAME, "TimeSource")

    private const val DATACACHE_PACKAGE_NAME =
      "com.google.android.libraries.pcc.chronicle.storage.datacache"
    private val TYPED_DATA_CACHE_READER = ClassName(DATACACHE_PACKAGE_NAME, "TypedDataCacheReader")
    private val TYPED_DATA_CACHE_WRITER = ClassName(DATACACHE_PACKAGE_NAME, "TypedDataCacheWriter")
    private val DATA_CACHE_READER = ClassName(DATACACHE_PACKAGE_NAME, "DataCacheReader")
    private val DATA_CACHE_WRITER = ClassName(DATACACHE_PACKAGE_NAME, "DataCacheWriter")
    private val MANAGED_DATA_CACHE = ClassName(DATACACHE_PACKAGE_NAME, "ManagedDataCache")
    private val DEFAULT_MANAGED_DATA_CACHE_CONNECTION_PROVIDER =
      ClassName(DATACACHE_PACKAGE_NAME, "DefaultManagedDataCacheConnectionProvider")

    private val TypeName.name: String
      get() = (this as ClassName).simpleName
  }
}
