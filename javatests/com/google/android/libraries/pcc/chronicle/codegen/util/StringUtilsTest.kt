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

package com.google.android.libraries.pcc.chronicle.codegen.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class StringUtilsTest {
  @Test
  fun upperSnake_withAllLowers() {
    assertThat("foo".upperSnake()).isEqualTo("FOO")
  }

  @Test
  fun upperSnake_withAllUppers() {
    assertThat("FOO".upperSnake()).isEqualTo("FOO")
  }

  @Test
  fun upperSnake_empty() {
    assertThat("".upperSnake()).isEqualTo("")
  }

  @Test
  fun upperSnake_longName() {
    assertThat("AbstractFactoryImplTestDelegateManagerThing".upperSnake())
      .isEqualTo("ABSTRACT_FACTORY_IMPL_TEST_DELEGATE_MANAGER_THING")
  }

  @Test
  fun upperSnake_withNumbers() {
    assertThat("ThisClassAtV2IsBetterThanAtV1".upperSnake())
      .isEqualTo("THIS_CLASS_AT_V2_IS_BETTER_THAN_AT_V1")
  }

  @Test
  fun capitalize_empty() {
    assertThat("".capitalize()).isEqualTo("")
  }

  @Test
  fun capitalize_lowercase() {
    assertThat("thisismystring".capitalize()).isEqualTo("Thisismystring")
  }

  @Test
  fun capitalize_leadingWhitespace() {
    assertThat(" thisismystring".capitalize()).isEqualTo(" thisismystring")
  }

  @Test
  fun capitalize_uppercase() {
    assertThat("THISISMYSTRING".capitalize()).isEqualTo("THISISMYSTRING")
  }

  @Test
  fun capitalize_multipleWords_onlyCapitalizesFirstWord() {
    assertThat("this is my string".capitalize()).isEqualTo("This is my string")
  }

  @Test
  fun decapitalize_empty() {
    assertThat("".decapitalize()).isEqualTo("")
  }

  @Test
  fun decapitalize_lowercase() {
    assertThat("thisismystring".decapitalize()).isEqualTo("thisismystring")
  }

  @Test
  fun decapitalize_leadingWhitespace() {
    assertThat(" THISISMYSTRING".decapitalize()).isEqualTo(" THISISMYSTRING")
  }

  @Test
  fun decapitalize_uppercase() {
    assertThat("THISISMYSTRING".decapitalize()).isEqualTo("tHISISMYSTRING")
  }

  @Test
  fun decapitalize_multipleWords_onlyDecapitalizesFirstWord() {
    assertThat("This Is My String".decapitalize()).isEqualTo("this Is My String")
  }
}
