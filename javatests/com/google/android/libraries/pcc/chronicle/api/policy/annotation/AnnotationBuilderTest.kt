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

/*
 * Copyright 2021 Google LLC.
 *
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 *
 * Code distributed by Google as part of this project is also subject to an additional IP rights
 * grant found at
 * http://polymer.github.io/PATENTS.txt
 */
package com.google.android.libraries.pcc.chronicle.api.policy.annotation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AnnotationBuilderTest {
  @Test
  fun annotation_emptyBlock() {
    val actual = annotation("ttl")

    assertThat(actual.name).isEqualTo("ttl")
    assertThat(actual.params).isEmpty()
  }

  @Test
  fun annotation_withParamBlock() {
    val actual = annotation("ttl") {
      param("stringParam", "My String Value")
      param("intParam", 42)
      param("boolParam", true)
    }

    assertThat(actual.name).isEqualTo("ttl")
    assertThat(actual.params).hasSize(3)
    assertThat(actual.params).containsExactly(
      "stringParam", AnnotationParam.Str("My String Value"),
      "intParam", AnnotationParam.Num(42),
      "boolParam", AnnotationParam.Bool(true)
    )
  }

  @Test
  fun javaStyle() {
    val actual = AnnotationBuilder("ttl")
      .param("stringParam", "My String Value")
      .param("intParam", 42)
      .param("boolParam", true)
      .build()

    assertThat(actual.name).isEqualTo("ttl")
    assertThat(actual.params).hasSize(3)
    assertThat(actual.params).containsExactly(
      "stringParam", AnnotationParam.Str("My String Value"),
      "intParam", AnnotationParam.Num(42),
      "boolParam", AnnotationParam.Bool(true)
    )
  }
}
