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
import com.google.android.libraries.pcc.chronicle.api.optics.Traversal
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.CITY_MOUNTAIN_VIEW
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Mayor
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Name
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Populace
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Name
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Pet
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet_Name
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MultiCantripTest {
  @Test
  fun invoke_callsMultipleCantrips() {
    val nameReverser = Operation.create<String>("Reversed") { Action.Update(it.reversed()) }

    val cantrip =
      MultiCantrip(
        OpticalCantrip(optic = TestCity_Name.asTraversal(), operation = nameReverser),
        OpticalCantrip(
          optic = TestCity_Mayor.asTraversal() compose TestPerson_Name.asTraversal(),
          operation = nameReverser,
        ),
        OpticalCantrip(
          optic =
            TestCity_Mayor.asTraversal() compose
              TestPerson_Pet.asTraversal() compose
              TestPet_Name.asTraversal(),
          operation = nameReverser,
        ),
        OpticalCantrip(
          optic =
            TestCity_Populace.asTraversal() compose
              Traversal.list() compose
              TestPerson_Name.asTraversal(),
          operation = nameReverser,
        ),
        OpticalCantrip(
          optic =
            TestCity_Populace.asTraversal() compose
              Traversal.list() compose
              TestPerson_Pet.asTraversal() compose
              TestPet_Name.asTraversal(),
          operation = nameReverser,
        ),
      )

    val updated = cantrip(CITY_MOUNTAIN_VIEW)

    assertThat(updated!!.name).isEqualTo(CITY_MOUNTAIN_VIEW.name.reversed())
    assertThat(updated.mayor.name).isEqualTo(CITY_MOUNTAIN_VIEW.mayor.name.reversed())
    assertThat(updated.mayor.pet?.name).isEqualTo(CITY_MOUNTAIN_VIEW.mayor.pet?.name?.reversed())
    updated.populace.forEachIndexed { index, testPerson ->
      assertThat(testPerson.name).isEqualTo(CITY_MOUNTAIN_VIEW.populace[index].name.reversed())
      assertThat(testPerson.pet?.name)
        .isEqualTo(CITY_MOUNTAIN_VIEW.populace[index].pet?.name?.reversed())
    }
  }
}
