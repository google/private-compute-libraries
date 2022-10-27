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

package com.google.android.libraries.pcc.chronicle.api.optics

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OpticalAccessPathTest {
  @Test
  fun compose() {
    val lhs = OpticalAccessPath("Person", "pet")
    val rhs = OpticalAccessPath("Pet", "name")

    val composed = lhs compose rhs

    assertThat(composed).isEqualTo(OpticalAccessPath("Person", "pet", "name"))
  }

  @Test
  fun compose_emptyAccessPaths() {
    val lhs = OpticalAccessPath("Person")
    val rhs = OpticalAccessPath("Pet")

    val composed = lhs compose rhs
    val composedReverse = rhs compose lhs

    assertThat(composed).isEqualTo(lhs)
    assertThat(composedReverse).isEqualTo(rhs)
  }

  @Test
  fun toString_noSelectors() {
    val path = OpticalAccessPath("Person")

    assertThat(path.toString()).isEqualTo("Person")
  }

  @Test
  fun toString_oneSelector() {
    val path = OpticalAccessPath("Person", "pet")

    assertThat(path.toString()).isEqualTo("Person::pet")
  }

  @Test
  fun toString_manySelectors() {
    val path = OpticalAccessPath("Person", "pet", "breed", "demeanor", "isGoodWithChildren")

    assertThat(path.toString()).isEqualTo("Person::pet.breed.demeanor.isGoodWithChildren")
  }
}
