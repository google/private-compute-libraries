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

package com.google.android.libraries.pcc.chronicle.api.optics.testdata

import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData
import com.google.android.libraries.pcc.chronicle.api.optics.Lens
import com.google.android.libraries.pcc.chronicle.api.optics.OpticalAccessPath

@ChronicleData
data class TestCity(
  val name: String,
  val mayor: TestPerson,
  val location: TestLocation,
  val populace: List<TestPerson>
)

val TestCity_Name =
  Lens.create<TestCity, String>(
    focusAccessPath = OpticalAccessPath(TEST_CITY_GENERATED_DTD, "name"),
    getter = { it.name },
    setter = { entity, newValue -> entity.copy(name = newValue) }
  )

val TestCity_Mayor =
  Lens.create<TestCity, TestPerson>(
    focusAccessPath = OpticalAccessPath(TEST_CITY_GENERATED_DTD, "mayor"),
    getter = { it.mayor },
    setter = { entity, newValue -> entity.copy(mayor = newValue) }
  )

val TestCity_Location =
  Lens.create<TestCity, TestLocation>(
    focusAccessPath = OpticalAccessPath(TEST_CITY_GENERATED_DTD, "location"),
    getter = { it.location },
    setter = { entity, newValue -> entity.copy(location = newValue) }
  )

val TestCity_Populace =
  Lens.create<TestCity, List<TestPerson>>(
    focusAccessPath = OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace"),
    getter = { it.populace },
    setter = { entity, newValue -> entity.copy(populace = newValue) }
  )

val TestCity_Lenses = setOf(TestCity_Name, TestCity_Mayor, TestCity_Location, TestCity_Populace)
