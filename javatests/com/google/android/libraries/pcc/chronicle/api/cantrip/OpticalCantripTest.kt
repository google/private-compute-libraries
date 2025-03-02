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

package com.google.android.libraries.pcc.chronicle.api.cantrip

import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.operation.Operation
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.CITY_MOUNTAIN_VIEW
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Name
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OpticalCantripTest {
  @Test
  fun invoke_omitFromParent_returnsNull() {
    val cantrip =
      OpticalCantrip(
        optic = TestCity_Name.asTraversal(),
        operation = Operation.create("OmitFromParent") { Action.OmitFromParent },
      )

    val result = cantrip(CITY_MOUNTAIN_VIEW)

    assertThat(result).isNull()
  }

  @Test
  fun invoke_omitFromRoot_returnsNull() {
    val cantrip =
      OpticalCantrip(
        optic = TestCity_Name.asTraversal(),
        operation = Operation.create("OmitFromRoot") { Action.OmitFromRoot },
      )

    val result = cantrip(CITY_MOUNTAIN_VIEW)

    assertThat(result).isNull()
  }

  @Test
  fun invoke_throw_throws() {
    val exception = IllegalArgumentException("Oops")
    val cantrip =
      OpticalCantrip(
        optic = TestCity_Name.asTraversal(),
        operation = Operation.create("Throw") { Action.Throw(exception) },
      )

    val thrown = assertFailsWith<IllegalArgumentException> { cantrip(CITY_MOUNTAIN_VIEW) }

    assertThat(thrown).isSameInstanceAs(exception)
  }

  @Test
  fun invoke_update() {
    val cantrip =
      OpticalCantrip(
        optic = TestCity_Name.asTraversal(),
        operation = Operation.create("Update") { Action.Update("Seattle") },
      )

    val result = cantrip(CITY_MOUNTAIN_VIEW)

    assertThat(result!!.name).isEqualTo("Seattle")
  }
}
