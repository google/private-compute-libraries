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

import com.google.android.libraries.pcc.chronicle.codegen.ExpectedTypes
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.PropertyProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.FileSpec
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataTypeDescriptorPropertyProviderTest {
  @Test
  fun emptyType() {
    val provider =
      DataTypeDescriptorPropertyProvider(
        "MyEntity",
        TypeSet(
          primary = Type(TypeLocation("MyEntity", emptyList(), "com.google"), fields = emptyList())
        )
      )

    val source = provider.getGeneratedSource()

    assertThat(source)
      .contains(
        """
          public val MY_ENTITY_GENERATED_DTD: DataTypeDescriptor = dataTypeDescriptor(name =
              "com.google.MyEntity", cls = MyEntity::class) {
              }
        """.trimIndent()
      )
  }

  @Test
  fun simpleType() {
    val provider =
      DataTypeDescriptorPropertyProvider(
        ExpectedTypes.simpleUnenclosedType.name,
        TypeSet(ExpectedTypes.simpleUnenclosedType)
      )

    val source = provider.getGeneratedSource()

    /* ktlint-disable max-line-length */
    assertThat(source)
      .contains(
        "val SIMPLE_UNENCLOSED_TYPE_GENERATED_DTD: DataTypeDescriptor = dataTypeDescriptor(name =\n" +
          "    \"com.google.android.libraries.pcc.chronicle.codegen.SimpleUnenclosedType\", cls =\n" +
          "    SimpleUnenclosedType::class) {\n" +
          "      \"field1\" to FieldType.String\n" +
          "      \"field2\" to FieldType.Integer\n" +
          "    }"
      )
    /* ktlint-enable max-line-length */
  }

  @Test
  fun customPropertyName() {
    val provider =
      DataTypeDescriptorPropertyProvider(
        entityClassSimpleName = ExpectedTypes.simpleUnenclosedType.name,
        typeSet = TypeSet(ExpectedTypes.simpleUnenclosedType),
        propertyName = "myDtd"
      )

    val source = provider.getGeneratedSource()

    /* ktlint-disable max-line-length */
    assertThat(source)
      .contains(
        "val myDtd: DataTypeDescriptor = dataTypeDescriptor(name =\n" +
          "    \"com.google.android.libraries.pcc.chronicle.codegen.SimpleUnenclosedType\", cls =\n" +
          "    SimpleUnenclosedType::class) {\n" +
          "      \"field1\" to FieldType.String\n" +
          "      \"field2\" to FieldType.Integer\n" +
          "    }"
      )
    /* ktlint-enable max-line-length */
  }

  private fun PropertyProvider.getGeneratedSource(): String {
    val fileSpec =
      FileSpec.builder("com.google", "FileName").apply { provideContentsInto(this) }.build()

    return StringBuilder().also { fileSpec.writeTo(it) }.toString()
  }
}
