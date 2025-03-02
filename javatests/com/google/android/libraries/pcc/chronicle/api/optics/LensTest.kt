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

import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.CITY_CHICAGO
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.CITY_MOUNTAIN_VIEW
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PERSON_SUNDAR
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PET_CAT_ARYA
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PET_DOG_GERALT
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PET_DOG_MORAINE
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Mayor
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Name
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Age
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Name
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Pet
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet_Age
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet_Name
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LensTest {
  @Test
  fun asTraversal() {
    val nameTraversal = TestPerson_Name.asTraversal()

    assertThat(nameTraversal.every(PERSON_SUNDAR).toList()).containsExactly("Sundar")
    assertThat(nameTraversal.modify(PERSON_SUNDAR) { "Jason" })
      .isEqualTo(PERSON_SUNDAR.copy(name = "Jason"))
    // Should still be called a Lens when toString is called.
    assertThat(nameTraversal.toString()).isEqualTo("Lens(${TestPerson::class.java.name}::name)")
  }

  @Test
  fun lift_nonComposedMonomorphic() {
    val modifyName = TestPerson_Name.lift { "Jason" }
    val modifyAge = TestPerson_Age.lift { it * 2 }
    val modifyPet = TestPerson_Pet.lift { PET_CAT_ARYA }

    assertThat(modifyName(PERSON_SUNDAR)).isEqualTo(PERSON_SUNDAR.copy(name = "Jason"))
    assertThat(modifyAge(PERSON_SUNDAR)).isEqualTo(PERSON_SUNDAR.copy(age = PERSON_SUNDAR.age * 2))
    assertThat(modifyPet(PERSON_SUNDAR)).isEqualTo(PERSON_SUNDAR.copy(pet = PET_CAT_ARYA))
  }

  @Test
  fun lift_nonComposedPolymorphic() {
    // Create a dogToCat modifier that makes the converted result to a cat who likes milk if and
    // only if the dog's favorite toy has an even number of characters.
    val dogToCat = DogToCat_FavoriteToyToLikesMilk.lift { it.length % 2 == 0 }

    assertThat(dogToCat(PET_DOG_GERALT))
      .isEqualTo(
        TestPet.Cat(
          name = PET_DOG_GERALT.name,
          age = PET_DOG_GERALT.age,
          likesMilk = PET_DOG_GERALT.favoriteToy.length % 2 == 0,
        )
      )
    assertThat(dogToCat(PET_DOG_MORAINE))
      .isEqualTo(
        TestPet.Cat(
          name = PET_DOG_MORAINE.name,
          age = PET_DOG_MORAINE.age,
          likesMilk = PET_DOG_MORAINE.favoriteToy.length % 2 == 0,
        )
      )
  }

  @Test
  fun lift_composedMonomorphic() {
    val modifyPetName = (TestPerson_Pet compose TestPet_Name).lift { "Rivia" }
    val modifyPetAge = (TestPerson_Pet compose TestPet_Age).lift { it / 2 }

    assertThat(modifyPetName(PERSON_SUNDAR))
      .isEqualTo(PERSON_SUNDAR.copy(pet = PET_DOG_GERALT.copy(name = "Rivia")))
    assertThat(modifyPetAge(PERSON_SUNDAR))
      .isEqualTo(PERSON_SUNDAR.copy(pet = PET_DOG_GERALT.copy(age = PET_DOG_GERALT.age / 2)))
  }

  @Test
  fun lift_composedPolymorphic() {
    // Create a dogToCat modifier that makes the converted result to a cat who likes milk if and
    // only if the dog's favorite toy has an even number of characters.
    val cityMayorDogToCat =
      (TestCity_Mayor compose TestPerson_Pet compose DogToCat_FavoriteToyToLikesMilk).lift {
        it.length % 2 == 0
      }

    assertThat(cityMayorDogToCat(CITY_MOUNTAIN_VIEW))
      .isEqualTo(
        CITY_MOUNTAIN_VIEW.copy(
          mayor =
            CITY_MOUNTAIN_VIEW.mayor.copy(
              pet =
                TestPet.Cat(
                  name = CITY_MOUNTAIN_VIEW.mayor.pet!!.name,
                  age = CITY_MOUNTAIN_VIEW.mayor.pet!!.age,
                  likesMilk =
                    (CITY_MOUNTAIN_VIEW.mayor.pet as TestPet.Dog).favoriteToy.length % 2 == 0,
                )
            )
        )
      )
  }

  @Test
  fun lift_composed_changeToCompatibleType() {
    val modifyMayorPet = (TestCity_Mayor compose TestPerson_Pet).lift { PET_CAT_ARYA }

    assertThat(modifyMayorPet(CITY_MOUNTAIN_VIEW))
      .isEqualTo(CITY_MOUNTAIN_VIEW.copy(mayor = CITY_MOUNTAIN_VIEW.mayor.copy(pet = PET_CAT_ARYA)))
  }

  @Test
  fun isMonomorphic_true() {
    assertThat(TestPerson_Name.isMonomorphic).isTrue()
    assertThat((TestPerson_Pet compose TestPet_Name).isMonomorphic).isTrue()
    assertThat((TestCity_Mayor compose TestPerson_Pet compose TestPet_Name).isMonomorphic).isTrue()
  }

  @Test
  fun isMonomorphic_false() {
    assertThat(DogToCat_FavoriteToyToLikesMilk.isMonomorphic).isFalse()
    assertThat((TestPerson_Pet compose DogToCat_FavoriteToyToLikesMilk).isMonomorphic).isFalse()
  }

  @Test
  fun toString_nonComposedMonomorphic() {
    assertThat(TestPerson_Name.toString()).isEqualTo("Lens(${TestPerson::class.java.name}::name)")
  }

  @Test
  fun toString_nonComposedPolymorphic() {
    assertThat(DogToCat_FavoriteToyToLikesMilk.toString())
      .isEqualTo("Lens(TestPet.Dog::favoriteToy -> TestPet.Cat::likesMilk)")
  }

  @Test
  fun toString_composedMonomorphic() {
    val composedLens = TestCity_Mayor compose TestPerson_Pet compose TestPet_Age

    assertThat(composedLens.toString())
      .isEqualTo("Lens(${TestCity::class.java.name}::mayor.pet.age)")
  }

  @Test
  fun toString_composedPolymorphic() {
    val composedLens = TestCity_Mayor compose TestPerson_Pet compose DogToCat_FavoriteToyToLikesMilk

    assertThat(composedLens.toString())
      .isEqualTo(
        "Lens(${TestCity::class.java.name}::mayor.pet.favoriteToy -> " +
          "${TestCity::class.java.name}::mayor.pet.likesMilk)"
      )
  }

  @Test
  fun modifyWithAction_update() {
    val result = TestCity_Name.modifyWithAction(CITY_CHICAGO) { Action.Update("Not Chicago") }

    result as Action.Update

    assertThat(TestCity_Name.get(result.newValue)).isEqualTo("Not Chicago")
  }

  @Test
  fun modifyWithAction_omitFromParent() {
    val result = TestCity_Name.modifyWithAction(CITY_CHICAGO) { Action.OmitFromParent }
    assertThat(result).isSameInstanceAs(Action.OmitFromParent)
  }

  @Test
  fun modifyWithAction_omitFromRoot() {
    val result = TestCity_Name.modifyWithAction(CITY_CHICAGO) { Action.OmitFromRoot }
    assertThat(result).isSameInstanceAs(Action.OmitFromRoot)
  }

  @Test
  fun modifyWithAction_throw() {
    val exception = IllegalArgumentException("Oops")
    val result = TestCity_Name.modifyWithAction(CITY_CHICAGO) { Action.Throw(exception) }

    result as Action.Throw

    assertThat(result.exception).isSameInstanceAs(exception)
  }

  companion object {
    private val DogToCat_FavoriteToyToLikesMilk =
      Lens.create<TestPet.Dog, TestPet.Cat, String, Boolean>(
        sourceAccessPath = OpticalAccessPath("TestPet.Dog", "favoriteToy"),
        targetAccessPath = OpticalAccessPath("TestPet.Cat", "likesMilk"),
        getter = { it.favoriteToy },
        setter = { entity, newValue -> TestPet.Cat(entity.name, entity.age, newValue) },
      )
  }
}
