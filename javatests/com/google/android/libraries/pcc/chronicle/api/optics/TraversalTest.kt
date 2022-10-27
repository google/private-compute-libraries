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
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.CITY_MOUNTAIN_VIEW
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Mayor
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Populace
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestLocation
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Name
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Pet
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet_Name
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TraversalTest {
  @Test
  fun lift_nonComposedMonomorphic() {
    val traversal = Traversal.list<String>()

    val repeatFn: (String) -> String = { "$it$it" }
    val lifted = traversal.lift(repeatFn)

    assertThat(lifted(listOf("a", "b", "c"))).containsExactly("aa", "bb", "cc").inOrder()
  }

  @Test
  fun lift_nonComposedPolymorphic() {
    val traversal = Traversal.listMap<String, Int>()

    val lengthCalculation: (String) -> Int = String::length
    val lifted = traversal.lift(lengthCalculation)

    assertThat(lifted(listOf("this", "is", "my", "testing", "value", "")))
      .containsExactly(4, 2, 2, 7, 5, 0)
      .inOrder()
  }

  @Test
  fun lift_composedMonomorphic() {
    val outer = Traversal.list<List<String>>()
    val inner = Traversal.list<String>()

    val composed = outer compose inner

    val repeatFn: (String) -> String = { "$it$it" }
    val lifted = composed.lift(repeatFn)

    val data = listOf(listOf("a", "b", "c"), listOf("d"), listOf("e", "f"), listOf())

    assertThat(lifted(data))
      .containsExactly(listOf("aa", "bb", "cc"), listOf("dd"), listOf("ee", "ff"), listOf<String>())
      .inOrder()
  }

  @Test
  fun lift_composedPolymorphic() {
    val outer = Traversal.listMap<List<String>, List<Int>>()
    val inner = Traversal.listMap<String, Int>()

    val composed = outer compose inner

    val lengthCalculation: (String) -> Int = String::length
    val lifted = composed.lift(lengthCalculation)

    val data =
      listOf(
        listOf("alice", "bob", "claire"),
        listOf("diego"),
        listOf("eliza", "francisco"),
        listOf()
      )

    assertThat(lifted(data))
      .containsExactly(listOf(5, 3, 6), listOf(5), listOf(5, 9), listOf<Int>())
      .inOrder()
  }

  @Test
  fun modifyWithAction_monomorphic_update() {
    val traversal = Traversal.list<String>()
    val input = listOf("a", "b", "c", "d")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.Update("ayooo") else Action.Update(it)
      }

    result as Action.Update

    assertThat(result.newValue).containsExactly("ayooo", "b", "c", "d")
  }

  @Test
  fun modifyWithAction_monomorphic_omitFromParent() {
    val traversal = Traversal.list<String>()
    val input = listOf("a", "b", "c", "d")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.OmitFromParent else Action.Update(it)
      }

    result as Action.Update

    assertThat(result.newValue).containsExactly("b", "c", "d")
  }

  @Test
  fun modifyWithAction_monomorphic_omitFromRoot() {
    val traversal = Traversal.list<String>()
    val input = listOf("a", "b", "c", "d")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.OmitFromRoot else Action.Update(it)
      }

    assertThat(result).isSameInstanceAs(Action.OmitFromRoot)
  }

  @Test
  fun modifyWithAction_monomorphic_throw() {
    val traversal = Traversal.list<String>()
    val input = listOf("a", "b", "c", "d")
    val exception = IllegalStateException("oops")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.Throw(exception) else Action.Update(it)
      }

    result as Action.Throw

    assertThat(result.exception).isSameInstanceAs(exception)
  }

  @Test
  fun modifyWithAction_polymorphic_update() {
    val traversal = Traversal.listMap<String, Int>()
    val input = listOf("a", "b", "c", "d")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.Update(42) else Action.Update(it.length)
      }

    result as Action.Update

    assertThat(result.newValue).containsExactly(42, 1, 1, 1)
  }

  @Test
  fun modifyWithAction_polymorphic_omitFromParent() {
    val traversal = Traversal.listMap<String, Int>()
    val input = listOf("a", "b", "c", "d")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.OmitFromParent else Action.Update(it.length)
      }

    result as Action.Update

    assertThat(result.newValue).containsExactly(1, 1, 1)
  }

  @Test
  fun modifyWithAction_polymorphic_omitFromRoot() {
    val traversal = Traversal.listMap<String, Int>()
    val input = listOf("a", "b", "c", "d")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.OmitFromRoot else Action.Update(it.length)
      }

    assertThat(result).isSameInstanceAs(Action.OmitFromRoot)
  }

  @Test
  fun modifyWithAction_polymorphic_throw() {
    val traversal = Traversal.listMap<String, Int>()
    val input = listOf("a", "b", "c", "d")
    val exception = IllegalStateException("oops")

    val result =
      traversal.modifyWithAction(input) {
        if (it == "a") Action.Throw(exception) else Action.Update(it.length)
      }

    result as Action.Throw

    assertThat(result.exception).isSameInstanceAs(exception)
  }

  @Test
  fun isMonomorphic_true() {
    val traversal = Traversal.list<String>()
    assertThat(traversal.isMonomorphic).isTrue()
  }

  @Test
  fun isMonomorphic_false() {
    val traversal = Traversal.listMap<String, Int>()
    assertThat(traversal.isMonomorphic).isFalse()
  }

  @Test
  fun compose_throwsWhen_canComposeFalse() {
    val outer = Traversal.list<String>()
    val inner = Traversal.list<String>()

    val e =
      assertFailsWith<IllegalArgumentException> {
        @Suppress("UNCHECKED_CAST") // that's what we're asserting!
        (outer as ListTraversal<List<String>>) compose inner
      }
    assertThat(e).hasMessageThat().contains("cannot compose with")
  }

  @Test
  fun canCompose_false() {
    val outer = Traversal.list<String>()
    val inner = Traversal.list<String>()

    assertThat(outer canCompose inner).isFalse()
  }

  @Test
  fun toString_nonComposedMonomorphic() {
    assertThat(Traversal.list<String>().toString()).isEqualTo("Traversal(List<String>::forEach)")
  }

  @Test
  fun toString_nonComposedPolymorphic() {
    assertThat(Traversal.listMap<String, Int>().toString())
      .isEqualTo("Traversal(List<String>::forEach -> List<Integer>::forEach)")
  }

  @Test
  fun toString_composedMonomorphic() {
    val outer = Traversal.list<List<String>>()
    val inner = Traversal.list<String>()

    val composed = outer compose inner

    assertThat(composed.toString()).isEqualTo("Traversal(List<List>::forEach.forEach)")
  }

  @Test
  fun toString_composedPolymorphic() {
    val outer = Traversal.listMap<List<String>, List<Int>>()
    val inner = Traversal.listMap<String, Int>()

    val composed = outer compose inner

    assertThat(composed.toString())
      .isEqualTo("Traversal(List<List>::forEach.forEach -> List<List>::forEach.forEach)")
  }

  @Test
  fun composedList_every() {
    val outer = Traversal.list<List<String>>()
    val inner = Traversal.list<String>()

    val composed = outer compose inner

    val listOfLists =
      listOf(
        listOf("alice", "bob", "claire"),
        listOf("diego"),
        listOf("eliza", "francisco"),
        listOf()
      )

    assertThat(composed.every(listOfLists).toList()).isEqualTo(listOfLists.flatten())
  }

  @Test
  fun composedLensAndTraversal() {
    val cities = Traversal.list<TestCity>()
    val populace = TestCity_Populace.asTraversal()
    val allPeople = Traversal.list<TestPerson>()
    val personName = TestPerson_Name.asTraversal()

    val namesOfAllPeopleAcrossAllCities =
      cities compose populace compose allPeople compose personName

    val zach = TestPerson("Zach", 32, null)
    val data =
      listOf(
        CITY_MOUNTAIN_VIEW,
        TestCity(
          name = "Dunedin",
          mayor = zach,
          location = TestLocation(1.0f, 2.0f),
          populace = listOf(zach)
        )
      )

    assertThat(namesOfAllPeopleAcrossAllCities.every(data).toList())
      .containsExactly(
        "Sundar",
        "Larry",
        "Sergey",
        "Zach",
      )
  }

  @Test
  fun composingNullableWithNonNullable() {
    val pet = TestPerson_Pet.asTraversal()
    val petName = TestPet_Name.asTraversal()

    val personPetName = pet compose petName

    val personWithNoPet = TestPerson(name = "Angelica", age = 28, pet = null)
    val personWithPet =
      TestPerson(name = "Brian", age = 31, pet = TestPet.Dog(name = "Spoon", age = 3))

    assertThat(personPetName.every(personWithNoPet).toList()).isEmpty()
    assertThat(personPetName.modify(personWithNoPet) { "Bob" }).isEqualTo(personWithNoPet)

    assertThat(personPetName.every(personWithPet).toList()).containsExactly("Spoon")
    assertThat(personPetName.modify(personWithPet) { "Bob" })
      .isEqualTo(personWithPet.copy(pet = TestPet.Dog(name = "Bob", age = 3)))

    val peoplePetNames = Traversal.list<TestPerson>() compose personPetName

    assertThat(peoplePetNames.every(listOf(personWithNoPet, personWithPet)).toList())
      .containsExactly("Spoon")
    assertThat(peoplePetNames.modify(listOf(personWithNoPet, personWithPet)) { "Bob" })
      .containsExactly(
        personWithNoPet,
        personWithPet.copy(pet = TestPet.Dog(name = "Bob", age = 3))
      )
  }

  @Test
  fun composing_updatingNullableField() {
    val mayor = TestCity_Mayor.asTraversal()
    val pet = TestPerson_Pet.asTraversal()

    val mayorPet = mayor compose pet

    val nulledPet = mayorPet.modify(CITY_MOUNTAIN_VIEW) { null }
    assertThat(nulledPet.mayor.pet).isNull()

    val updatedPet = mayorPet.modify(nulledPet) { TestPet.Cat("Garfield", age = 12) }
    assertThat(updatedPet.mayor.pet).isEqualTo(TestPet.Cat("Garfield", age = 12))
  }

  @Test
  fun composing_operatingOnNullableField() {
    val mayor = TestCity_Mayor.asTraversal()
    val pet = TestPerson_Pet.asTraversal()

    val mayorPet = mayor compose pet

    val nulledPet = mayorPet.modify(CITY_MOUNTAIN_VIEW) { null }
    assertThat(nulledPet.mayor.pet).isNull()

    val opResult =
      mayorPet.modifyWithAction(nulledPet) { Action.Update(TestPet.Cat("Garfield", age = 12)) }
    val updatedPet = (opResult as Action.Update).newValue
    assertThat(updatedPet.mayor.pet).isEqualTo(TestPet.Cat("Garfield", age = 12))
  }
}
