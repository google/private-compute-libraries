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
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.PropertyProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.asTypeName
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConnectionsPropertyProviderTest {
  @Test
  fun emptyConnectionTypeNames_providesEmptySet() {
    val provider =
      ConnectionsPropertyProvider(
        entityClassSimpleName = "MyEntity",
        connectionTypeNames = emptySet()
      )

    val fileContents = provider.getGeneratedSource()

    assertThat(fileContents)
      .contains("public val MY_ENTITY_GENERATED_CONNECTIONS: Set<Class<out Connection>> = setOf()")
  }

  @Test
  fun nonEmptyConnectionTypeNames() {
    val provider =
      ConnectionsPropertyProvider(
        entityClassSimpleName = "MyEntity",
        connectionTypeNames =
          listOf(MyEntityReader::class.asTypeName(), MyEntityWriter::class.asTypeName())
      )

    val fileContents = provider.getGeneratedSource()

    assertThat(fileContents)
      .contains(
        "public val MY_ENTITY_GENERATED_CONNECTIONS: Set<Class<out Connection>> =\n" +
          "    setOf(MyEntityReader::class.java, MyEntityWriter::class.java)"
      )
  }

  private fun PropertyProvider.getGeneratedSource(): String {
    val fileSpec =
      FileSpec.builder("com.google", "FileName").apply { provideContentsInto(this) }.build()

    return StringBuilder().also { fileSpec.writeTo(it) }.toString()
  }
}

interface MyEntityReader : ReadConnection

interface MyEntityWriter : WriteConnection
