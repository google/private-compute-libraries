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

package com.google.android.libraries.pcc.chronicle.codegen

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeLocationTest {
  @Test
  fun toString_withEmptyEnclosingName() {
    val location = TypeLocation(name = "name", enclosingNames = listOf(""), pkg = "pkg")

    assertThat(location.toString()).isEqualTo("pkg.name")
  }

  @Test
  fun toString_withoutEnclosingNames() {
    val location = TypeLocation(name = "name", enclosingNames = emptyList(), pkg = "pkg")

    assertThat(location.toString()).isEqualTo("pkg.name")
  }

  @Test
  fun toString_withOneEnclosingName() {
    val location = TypeLocation(name = "name", enclosingNames = listOf("enclosing"), pkg = "pkg")

    assertThat(location.toString()).isEqualTo("pkg.enclosing.name")
  }

  @Test
  fun toString_withThreeEnclosingName() {
    val location =
      TypeLocation(
        name = "name",
        enclosingNames = listOf("innermost", "middle", "outermost"),
        pkg = "pkg"
      )

    assertThat(location.toString()).isEqualTo("pkg.outermost.middle.innermost.name")
  }
}
