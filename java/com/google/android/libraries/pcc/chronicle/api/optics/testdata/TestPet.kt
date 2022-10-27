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
sealed class TestPet {
  abstract val name: String
  abstract val age: Int
  // TODO(b/202182073): Hack to mimic desired DTD behavior.
  open val favoriteToy: String = "toy"
  open val likesMilk: Boolean? = null

  data class Dog(
    override val name: String,
    override val age: Int,
    override val favoriteToy: String = "Ball"
  ) : TestPet()

  data class Cat(
    override val name: String,
    override val age: Int,
    override val likesMilk: Boolean = true
  ) : TestPet()
}

val TestPet_Name =
  Lens.create<TestPet, String>(
    focusAccessPath = OpticalAccessPath(TEST_PET_GENERATED_DTD, "name"),
    getter = { it.name },
    setter = { entity, newValue ->
      when (entity) {
        is TestPet.Dog -> entity.copy(name = newValue)
        is TestPet.Cat -> entity.copy(name = newValue)
      }
    }
  )

val TestPet_Age =
  Lens.create<TestPet, Int>(
    focusAccessPath = OpticalAccessPath(TEST_PET_GENERATED_DTD, "age"),
    getter = { it.age },
    setter = { entity, newValue ->
      when (entity) {
        is TestPet.Dog -> entity.copy(age = newValue)
        is TestPet.Cat -> entity.copy(age = newValue)
      }
    }
  )

val TestPet_FavoriteToy =
  Lens.create<TestPet, String>(
    focusAccessPath = OpticalAccessPath(TEST_PET_GENERATED_DTD, "favoriteToy"),
    getter = {
      if (it is TestPet.Dog) {
        it.favoriteToy
      } else {
        ""
      }
    },
    setter = { entity, newValue ->
      when (entity) {
        is TestPet.Dog -> entity.copy(favoriteToy = newValue)
        is TestPet.Cat -> entity
      }
    }
  )

val TestPet_LikesMilk =
  Lens.create<TestPet, Boolean?>(
    focusAccessPath = OpticalAccessPath(TEST_PET_GENERATED_DTD, "likesMilk"),
    getter = {
      if (it is TestPet.Cat) {
        it.likesMilk
      } else {
        null
      }
    },
    setter = { entity, newValue ->
      when (entity) {
        is TestPet.Dog -> entity
        is TestPet.Cat -> entity.copy(likesMilk = newValue ?: false)
      }
    }
  )

val TestPet_Lenses = setOf(TestPet_Name, TestPet_Age, TestPet_FavoriteToy, TestPet_LikesMilk)
