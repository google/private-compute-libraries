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

val PET_DOG_GERALT = TestPet.Dog(name = "Geralt", age = 12, favoriteToy = "ball")

val PET_DOG_MORAINE = TestPet.Dog(name = "Moraine", age = 15, favoriteToy = "tugrope")

val PET_CAT_ARYA = TestPet.Cat(name = "Arya", age = 6, likesMilk = true)

val PET_DOG_BO = TestPet.Dog(name = "Bo", age = 6, favoriteToy = "sebastian")

val PERSON_SUNDAR = TestPerson(name = "Sundar", age = 25, pet = PET_DOG_GERALT)

val PERSON_LARRY = TestPerson(name = "Larry", age = 41, pet = null)

val PERSON_SERGEY = TestPerson(name = "Sergey", age = 42, pet = PET_CAT_ARYA)

val PERSON_BARACK = TestPerson(name = "Barack", age = 55, pet = PET_DOG_BO)

val PERSON_MICHELLE = TestPerson(name = "Michelle", age = 52, pet = PET_DOG_BO)

val CITY_MOUNTAIN_VIEW =
  TestCity(
    name = "Mountain View",
    mayor = PERSON_SUNDAR,
    location = TestLocation(37.3861f, -122.0839f),
    populace = listOf(PERSON_SUNDAR, PERSON_LARRY, PERSON_SERGEY)
  )

val CITY_CHICAGO =
  TestCity(
    name = "Chicago",
    mayor = PERSON_MICHELLE,
    location = TestLocation(41.8781f, -87.6298f),
    populace = listOf(PERSON_BARACK, PERSON_MICHELLE)
  )
