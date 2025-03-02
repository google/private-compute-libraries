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

import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataTypeDescriptorDaggerProviderTest {
  @Test
  fun provideContentsInto_defaultDtdContainingClassName() {
    val provider = DataTypeDescriptorDaggerProvider("MyDataClass")

    assertThat(provider)
      .generatesMethod(
        """
        |  @Provides
        |  @Singleton
        |  @IntoSet
        |  public static DataTypeDescriptor provideMyDataClassDataTypeDescriptor() {
        |    return MyDataClass_GeneratedKt.MY_DATA_CLASS_GENERATED_DTD;
        |  }
        """
          .trimMargin()
      )
  }

  @Test
  fun provideContentsInto_customDtdContainingClassName() {
    val provider =
      DataTypeDescriptorDaggerProvider(
        elementName = "MyDataClass",
        dtdContainingClassName = "MyDtds",
      )

    assertThat(provider)
      .generatesMethod(
        """
        |  @Provides
        |  @Singleton
        |  @IntoSet
        |  public static DataTypeDescriptor provideMyDataClassDataTypeDescriptor() {
        |    return MyDtds.MY_DATA_CLASS_GENERATED_DTD;
        |  }
        """
          .trimMargin()
      )
  }

  private fun assertThat(provider: DataTypeDescriptorDaggerProvider): StringSubject {
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
        |import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor;
        |import dagger.Provides;
        |import dagger.multibindings.IntoSet;
        |import javax.inject.Singleton;
        |
        |class MyClass {
        |$method
        |}
      """
        .trimMargin()
    )
  }
}
