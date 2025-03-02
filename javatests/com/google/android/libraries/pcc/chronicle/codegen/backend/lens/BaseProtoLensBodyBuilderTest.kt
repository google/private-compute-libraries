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

import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory
import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.CodeBlock
import kotlin.test.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BaseProtoLensBodyBuilderTest {
  @Test
  fun buildSetterBody() {
    val impl =
      object : BaseProtoLensBodyBuilder() {
        override fun supportsField(type: Type, field: FieldEntry) = true

        override fun buildGetterBody(
          type: Type,
          field: FieldEntry,
          entityParamName: String,
        ): CodeBlock = fail("Shouldn't be called.")

        override fun buildSetterBuilderCode(
          type: Type,
          field: FieldEntry,
          entityParamName: String,
          newValueParamName: String,
        ): CodeBlock = CodeBlock.of("println(\"Hello world\")\n")
      }

    val setterBody =
      impl.buildSetterBody(
        type = Type(TypeLocation("foo", pkg = ""), emptyList()),
        field = FieldEntry("field", FieldCategory.IntValue),
        entityParamName = "entity",
        newValueParamName = "newValue",
      )

    assertThat(setterBody.toString())
      .contains(
        """
          entity.toBuilder()
            .apply {
              println("Hello world")
            }
            .build()
        """
          .trimIndent()
      )
  }
}
