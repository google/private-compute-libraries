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
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LensDaggerProviderTest {
  @Test
  fun provideContentsInto_defaults() {
    val provider = LensDaggerProvider(TYPE, FIELD)

    assertThat(provider)
      .generatesMethod(
        """
        |  @Provides
        |  @Singleton
        |  @IntoSet
        |  public static Lens<?, ?, ?, ?> provideMyTypeMyFieldLens() {
        |    return MyType_GeneratedKt.MY_TYPE_MY_FIELD_GENERATED_LENS;
        |  }
        """.trimMargin()
      )
  }

  @Test
  fun provideContentsInto_withCustomMethodName() {
    val provider = LensDaggerProvider(TYPE, FIELD, methodName = "provideIt")

    assertThat(provider)
      .generatesMethod(
        """
        |  @Provides
        |  @Singleton
        |  @IntoSet
        |  public static Lens<?, ?, ?, ?> provideIt() {
        |    return MyType_GeneratedKt.MY_TYPE_MY_FIELD_GENERATED_LENS;
        |  }
        """.trimMargin()
      )
  }

  @Test
  fun provideContentsInto_withCustomLensPropertyName() {
    val provider = LensDaggerProvider(TYPE, FIELD, lensPropertyName = "THE_LENS")

    assertThat(provider)
      .generatesMethod(
        """
        |  @Provides
        |  @Singleton
        |  @IntoSet
        |  public static Lens<?, ?, ?, ?> provideMyTypeMyFieldLens() {
        |    return MyType_GeneratedKt.THE_LENS;
        |  }
        """.trimMargin()
      )
  }

  @Test
  fun provideContentsInto_withCustomLensClassName() {
    val provider = LensDaggerProvider(TYPE, FIELD, lensClassName = "MyTypeLenses")

    assertThat(provider)
      .generatesMethod(
        """
        |  @Provides
        |  @Singleton
        |  @IntoSet
        |  public static Lens<?, ?, ?, ?> provideMyTypeMyFieldLens() {
        |    return MyTypeLenses.MY_TYPE_MY_FIELD_GENERATED_LENS;
        |  }
        """.trimMargin()
      )
  }

  private fun assertThat(provider: LensDaggerProvider): StringSubject {
    val typeSpec =
      TypeSpec.classBuilder("MyClass").apply { provider.provideContentsInto(this) }.build()
    val contents = JavaFile.builder("com.google", typeSpec).build().toString().trim()
    return assertThat(contents)
  }

  private fun StringSubject.generatesMethod(method: String) {
    isEqualTo(
      """
        |package com.google;
        |
        |import com.google.android.libraries.pcc.chronicle.api.optics.Lens;
        |import dagger.Provides;
        |import dagger.multibindings.IntoSet;
        |import javax.inject.Singleton;
        |
        |class MyClass {
        |$method
        |}
      """.trimMargin()
    )
  }

  companion object {
    private val FIELD = FieldEntry("myField", FieldCategory.StringValue)

    private val TYPE =
      Type(location = TypeLocation("MyType", pkg = "com.google"), fields = listOf(FIELD))
  }
}
