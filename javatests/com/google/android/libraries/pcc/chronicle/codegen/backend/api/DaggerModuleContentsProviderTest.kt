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

package com.google.android.libraries.pcc.chronicle.codegen.backend.api

import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleContentsProvider.Companion.PRIVACY_REVIEWED_QUALIFIER
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DaggerModuleContentsProviderTest {
  @Test
  fun providesMethod_provideContentsInto_simple() {
    val providesMethod =
      object :
        DaggerModuleContentsProvider.ProvidesMethod(
          name = "provideTestStuff",
          providedType = ClassName.get("com.google", "TestStuff"),
          isSingleton = false
        ) {
        override val parameters: List<ParameterSpec> = emptyList()
        override fun provideBody(): CodeBlock =
          CodeBlock.of("throw UnsupportedOperationException();")
      }

    assertThat(providesMethod)
      .generatesTestModule(
        """
          class TestModule {
            @dagger.Provides
            public static com.google.TestStuff provideTestStuff() {
              throw UnsupportedOperationException();
            }
          }
        """.trimIndent()
      )
  }

  @Test
  fun providesMethod_provideContentsInto_singleton() {
    val providesMethod =
      object :
        DaggerModuleContentsProvider.ProvidesMethod(
          name = "provideTestStuff",
          providedType = ClassName.get("com.google", "TestStuff"),
          isSingleton = true
        ) {
        override val parameters: List<ParameterSpec> = emptyList()
        override fun provideBody(): CodeBlock =
          CodeBlock.of("throw UnsupportedOperationException();")
      }

    assertThat(providesMethod)
      .generatesTestModule(
        """
          class TestModule {
            @dagger.Provides
            @javax.inject.Singleton
            public static com.google.TestStuff provideTestStuff() {
              throw UnsupportedOperationException();
            }
          }
        """.trimIndent()
      )
  }

  @Test
  fun providesMethod_provideContentsInto_intoSet() {
    val providesMethod =
      object :
        DaggerModuleContentsProvider.ProvidesMethod(
          name = "provideTestStuff",
          providedType = ClassName.get("com.google", "TestStuff"),
          isSingleton = false,
          isIntoSet = true
        ) {
        override val parameters: List<ParameterSpec> = emptyList()
        override fun provideBody(): CodeBlock =
          CodeBlock.of("throw UnsupportedOperationException();")
      }

    assertThat(providesMethod)
      .generatesTestModule(
        """
          class TestModule {
            @dagger.Provides
            @dagger.multibindings.IntoSet
            public static com.google.TestStuff provideTestStuff() {
              throw UnsupportedOperationException();
            }
          }
        """.trimIndent()
      )
  }

  @Test
  fun providesMethod_provideContentsInto_withQualifierAnnotations() {
    val providesMethod =
      object :
        DaggerModuleContentsProvider.ProvidesMethod(
          name = "provideTestStuff",
          providedType = ClassName.get("com.google", "TestStuff"),
          isSingleton = false,
          qualifierAnnotations =
            listOf(PRIVACY_REVIEWED_QUALIFIER, ClassName.get("com.google", "MyQualifier"))
        ) {
        override val parameters: List<ParameterSpec> = emptyList()
        override fun provideBody(): CodeBlock =
          CodeBlock.of("throw UnsupportedOperationException();")
      }

    assertThat(providesMethod)
      .generatesTestModule(
        """
          class TestModule {
            @dagger.Provides
            @com.google.android.libraries.pcc.chronicle.api.qualifier.PrivacyReviewed
            @com.google.MyQualifier
            public static com.google.TestStuff provideTestStuff() {
              throw UnsupportedOperationException();
            }
          }
        """.trimIndent()
      )
  }

  @Test
  fun providesMethod_provideContentsInto_withArguments() {
    val providesMethod =
      object :
        DaggerModuleContentsProvider.ProvidesMethod(
          name = "provideTestStuff",
          providedType = ClassName.get("com.google", "TestStuff"),
          isSingleton = false
        ) {
        override val parameters: List<ParameterSpec> =
          listOf(
            ParameterSpec.builder(TypeName.BOOLEAN, "boolParam").build(),
            ParameterSpec.builder(TypeName.INT, "intParam").build(),
          )
        override fun provideBody(): CodeBlock =
          CodeBlock.of("throw UnsupportedOperationException();")
      }

    assertThat(providesMethod)
      .generatesTestModule(
        """
          class TestModule {
            @dagger.Provides
            public static com.google.TestStuff provideTestStuff(boolean boolParam, int intParam) {
              throw UnsupportedOperationException();
            }
          }
        """.trimIndent()
      )
  }

  fun assertThat(provider: DaggerModuleContentsProvider): StringSubject {
    val body =
      TypeSpec.classBuilder("TestModule")
        .apply { provider.provideContentsInto(this) }
        .build()
        .toString()
        .trim()
    return assertThat(body)
  }

  fun StringSubject.generatesTestModule(expected: String) = isEqualTo(expected)
}
