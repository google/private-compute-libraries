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

import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.FileSpecContentsProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.asTypeName
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConnectionProviderTypeProviderTest {
  @Test
  fun generatedCode_containsReadAndWriteConnectionsImplementationsAndConnectionProvider() {
    val typeProvider =
      ConnectionProviderTypeProvider(
        MyDataClass::class.asTypeName(),
        listOf(MyReadConnection::class.asTypeName()),
        listOf(MyWriteConnection::class.asTypeName()),
      )
    val fileContents = typeProvider.getGeneratedSource()
    assertThat(fileContents).isEqualTo(expectedOutputOneReadConnectionOneWriteConnection)
  }

  @Test
  fun generatedCode_containsOnlyReadConnectionImplementationsAndConnectionProvider() {
    val typeProvider =
      ConnectionProviderTypeProvider(
        MyDataClass::class.asTypeName(),
        listOf(MyReadConnection::class.asTypeName()),
        listOf(),
      )
    val fileContents = typeProvider.getGeneratedSource()
    assertThat(fileContents).isEqualTo(expectedOutputOneReadConnection)
  }

  @Test
  fun generatedCode_containsMultipleWriteConnectionImplementationsAndConnectionProvider() {
    val typeProvider =
      ConnectionProviderTypeProvider(
        MyDataClass::class.asTypeName(),
        listOf(),
        listOf(MyWriteConnection0::class.asTypeName(), MyWriteConnection1::class.asTypeName()),
      )
    val fileContents = typeProvider.getGeneratedSource()
    assertThat(fileContents).isEqualTo(expectedOutputMultipleWriteConnection)
  }

  private fun FileSpecContentsProvider.getGeneratedSource(): String {
    val fileSpec =
      FileSpec.builder("com.google", "FileName").apply { provideContentsInto(this) }.build()

    return StringBuilder().also { fileSpec.writeTo(it) }.toString()
  }

  companion object {
    private val expectedOutputOneReadConnectionOneWriteConnection =
      """
    package com.google

    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyDataClass
    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyReadConnection
    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyWriteConnection
    import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheReader
    import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheWriter
    import com.google.android.libraries.pcc.chronicle.storage.datacache.DefaultManagedDataCacheConnectionProvider
    import com.google.android.libraries.pcc.chronicle.storage.datacache.ManagedDataCache
    import com.google.android.libraries.pcc.chronicle.storage.datacache.TypedDataCacheReader
    import com.google.android.libraries.pcc.chronicle.storage.datacache.TypedDataCacheWriter
    import com.google.android.libraries.pcc.chronicle.util.TimeSource

    public class MyReadConnectionImpl(
      cache: TypedDataCacheReader<MyDataClass>,
    ) : MyReadConnection, DataCacheReader<MyDataClass> by DataCacheReader.createDefault(cache)

    public class MyWriteConnectionImpl(
      cache: TypedDataCacheWriter<MyDataClass>,
      timeSource: TimeSource,
    ) : MyWriteConnection, DataCacheWriter<MyDataClass> by DataCacheWriter.createDefault(cache,
        timeSource)

    public class MyDataClassConnectionProvider(
      cache: ManagedDataCache<MyDataClass>,
      timeSource: TimeSource,
    ) : DefaultManagedDataCacheConnectionProvider<MyDataClass>(cache, mapOf(MyReadConnection::class.java
        to { MyReadConnectionImpl(cache) }, MyWriteConnection::class.java to {
        MyWriteConnectionImpl(cache, timeSource) }, ))

    """
        .trimIndent()

    private val expectedOutputOneReadConnection =
      """
    package com.google

    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyDataClass
    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyReadConnection
    import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheReader
    import com.google.android.libraries.pcc.chronicle.storage.datacache.DefaultManagedDataCacheConnectionProvider
    import com.google.android.libraries.pcc.chronicle.storage.datacache.ManagedDataCache
    import com.google.android.libraries.pcc.chronicle.storage.datacache.TypedDataCacheReader
    import com.google.android.libraries.pcc.chronicle.util.TimeSource

    public class MyReadConnectionImpl(
      cache: TypedDataCacheReader<MyDataClass>,
    ) : MyReadConnection, DataCacheReader<MyDataClass> by DataCacheReader.createDefault(cache)

    public class MyDataClassConnectionProvider(
      cache: ManagedDataCache<MyDataClass>,
      timeSource: TimeSource,
    ) : DefaultManagedDataCacheConnectionProvider<MyDataClass>(cache, mapOf(MyReadConnection::class.java
        to { MyReadConnectionImpl(cache) }, ))

    """
        .trimIndent()

    private val expectedOutputMultipleWriteConnection =
      """
    package com.google

    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyDataClass
    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyWriteConnection0
    import com.google.android.libraries.pcc.chronicle.codegen.backend.MyWriteConnection1
    import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheWriter
    import com.google.android.libraries.pcc.chronicle.storage.datacache.DefaultManagedDataCacheConnectionProvider
    import com.google.android.libraries.pcc.chronicle.storage.datacache.ManagedDataCache
    import com.google.android.libraries.pcc.chronicle.storage.datacache.TypedDataCacheWriter
    import com.google.android.libraries.pcc.chronicle.util.TimeSource

    public class MyWriteConnection0Impl(
      cache: TypedDataCacheWriter<MyDataClass>,
      timeSource: TimeSource,
    ) : MyWriteConnection0, DataCacheWriter<MyDataClass> by DataCacheWriter.createDefault(cache,
        timeSource)

    public class MyWriteConnection1Impl(
      cache: TypedDataCacheWriter<MyDataClass>,
      timeSource: TimeSource,
    ) : MyWriteConnection1, DataCacheWriter<MyDataClass> by DataCacheWriter.createDefault(cache,
        timeSource)

    public class MyDataClassConnectionProvider(
      cache: ManagedDataCache<MyDataClass>,
      timeSource: TimeSource,
    ) : DefaultManagedDataCacheConnectionProvider<MyDataClass>(cache,
        mapOf(MyWriteConnection0::class.java to { MyWriteConnection0Impl(cache, timeSource) },
        MyWriteConnection1::class.java to { MyWriteConnection1Impl(cache, timeSource) }, ))

    """
        .trimIndent()
  }
}

class MyDataClass()

interface MyReadConnection : ReadConnection

interface MyWriteConnection : WriteConnection

interface MyWriteConnection0 : WriteConnection

interface MyWriteConnection1 : WriteConnection
