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

@ChronicleData data class TestPerson(val name: String, val age: Int, val pet: TestPet?)

val TestPerson_Name =
  Lens.create<TestPerson, String>(
    focusAccessPath = OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name"),
    getter = { it.name },
    setter = { entity, newValue -> entity.copy(name = newValue) },
  )

val TestPerson_Age =
  Lens.create<TestPerson, Int>(
    focusAccessPath = OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "age"),
    getter = { it.age },
    setter = { entity, newValue -> entity.copy(age = newValue) },
  )

val TestPerson_Pet =
  Lens.create<TestPerson, TestPet?>(
    focusAccessPath = OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "pet"),
    getter = { it.pet },
    setter = { entity, newValue -> entity.copy(pet = newValue) },
  )

val TestPerson_Lenses = setOf(TestPerson_Name, TestPerson_Age, TestPerson_Pet)
