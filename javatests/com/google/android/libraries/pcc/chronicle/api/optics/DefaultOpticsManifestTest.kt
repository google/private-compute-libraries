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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultDataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.operation.Operation
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.CITY_MOUNTAIN_VIEW
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PERSON_BARACK
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PERSON_LARRY
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PERSON_MICHELLE
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PERSON_SERGEY
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.PERSON_SUNDAR
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_CITY_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_LOCATION_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_PERSON_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_PET_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Lenses
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestLocation_Lenses
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Lenses
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet_Lenses
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultOpticsManifestTest {
  private val optics: OpticsManifest =
    DefaultOpticsManifest(
      TestPerson_Lenses + TestCity_Lenses + TestLocation_Lenses + TestPet_Lenses,
      DefaultDataTypeDescriptorSet(
        setOf(
          TEST_PERSON_GENERATED_DTD,
          TEST_CITY_GENERATED_DTD,
          TEST_LOCATION_GENERATED_DTD,
          TEST_PET_GENERATED_DTD
        ),
      )
    )

  @Test
  fun composeTraversal_simple() {
    val nameTraversal =
      optics.composeMono<TestPerson, String>(OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name"))

    val people = listOf(PERSON_LARRY, PERSON_SERGEY, PERSON_SUNDAR, PERSON_MICHELLE, PERSON_BARACK)
    val peopleNamesTraversal = Traversal.list<TestPerson>() compose nameTraversal

    assertThat(peopleNamesTraversal.every(people).toList())
      .containsExactly("Larry", "Sergey", "Sundar", "Michelle", "Barack")
      .inOrder()
  }

  @Test
  fun composeTraversal_simple_actionUpdate() {
    val nameTraversal =
      optics.composeMono<TestPerson, String>(OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name"))

    val people = listOf(PERSON_LARRY, PERSON_SERGEY, PERSON_SUNDAR, PERSON_MICHELLE, PERSON_BARACK)
    val peopleNamesTraversal = Traversal.list<TestPerson>() compose nameTraversal

    val brinnifier: (String) -> Action<out String> = {
      if (it == "Sergey") Action.Update("Brin") else Action.Update(it)
    }

    val result = peopleNamesTraversal.modifyWithAction(people, brinnifier)
    val updated = (result as Action.Update).newValue
    assertThat(peopleNamesTraversal.every(updated).toList())
      .containsExactly("Larry", "Brin", "Sundar", "Michelle", "Barack")
      .inOrder()
  }

  @Test
  fun composeTraversal_simple_actionOmitFromParent() {
    val nameTraversal =
      optics.composeMono<TestPerson, String>(OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name"))

    val people = listOf(PERSON_LARRY, PERSON_SERGEY, PERSON_SUNDAR, PERSON_MICHELLE, PERSON_BARACK)
    val peopleNamesTraversal = Traversal.list<TestPerson>() compose nameTraversal

    val removeSergey: (String) -> Action<out String> = {
      if (it == "Sergey") Action.OmitFromParent else Action.Update(it)
    }

    val result = peopleNamesTraversal.modifyWithAction(people, removeSergey)
    val updated = (result as Action.Update).newValue
    assertThat(peopleNamesTraversal.every(updated).toList())
      .containsExactly("Larry", "Sundar", "Michelle", "Barack")
      .inOrder()
  }

  @Test
  fun composeTraversal_nestedNoList() {
    val cityMayorNameTraversal =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "mayor", "name")
      )

    assertThat(cityMayorNameTraversal.every(CITY_MOUNTAIN_VIEW).toList()).containsExactly("Sundar")
  }

  @Test
  fun composeTraversal_nestedWithList() {
    val cityPopulationNameTraversal =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "name")
      )

    assertThat(cityPopulationNameTraversal.every(CITY_MOUNTAIN_VIEW).toList())
      .containsExactly(
        "Sundar",
        "Larry",
        "Sergey",
      )
  }

  @Test
  fun composeTraversal_nestedWithList_actionUpdate() {
    val cityPopulationNameTraversal =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "name")
      )

    assertThat(cityPopulationNameTraversal.every(CITY_MOUNTAIN_VIEW).toList())
      .containsExactly("Sundar", "Larry", "Sergey")

    val doubler = Operation.create<String>("Doubler") { Action.Update("$it$it") }

    val opResult = cityPopulationNameTraversal.modifyWithAction(CITY_MOUNTAIN_VIEW, doubler)
    val modified = (opResult as Action.Update).newValue

    assertThat(cityPopulationNameTraversal.every(modified).toList())
      .containsExactly("SundarSundar", "LarryLarry", "SergeySergey")
  }

  @Test
  fun composeTraversal_nestedWithList_actionOmitFromParent() {
    val cityPopulationNameTraversal =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "name")
      )

    assertThat(cityPopulationNameTraversal.every(CITY_MOUNTAIN_VIEW).toList())
      .containsExactly("Sundar", "Larry", "Sergey")

    val deleteLarry =
      Operation.create<String>("LarryDeleter") {
        if (it == "Larry") Action.OmitFromParent else Action.Update(it)
      }

    val opResult = cityPopulationNameTraversal.modifyWithAction(CITY_MOUNTAIN_VIEW, deleteLarry)
    val modified = (opResult as Action.Update).newValue

    assertThat(cityPopulationNameTraversal.every(modified).toList())
      .containsExactly("Sundar", "Sergey")
  }

  @Test
  fun composeTraversal_nestedWithList_actionOmitFromRoot() {
    val cityPopulationNameTraversal =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "name")
      )

    assertThat(cityPopulationNameTraversal.every(CITY_MOUNTAIN_VIEW).toList())
      .containsExactly("Sundar", "Larry", "Sergey")

    val omitter = Operation.create<String>("Omitter") { Action.OmitFromRoot }

    val opResult = cityPopulationNameTraversal.modifyWithAction(CITY_MOUNTAIN_VIEW, omitter)
    assertThat(opResult).isSameInstanceAs(Action.OmitFromRoot)
  }

  @Test
  fun composeTraversal_nestedWithList_actionThrow() {
    val cityPopulationNameTraversal =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "name")
      )

    assertThat(cityPopulationNameTraversal.every(CITY_MOUNTAIN_VIEW).toList())
      .containsExactly("Sundar", "Larry", "Sergey")

    val exception = IllegalArgumentException("Foo")
    val thrower = Operation.create<String>("Thrower") { Action.Throw(exception) }

    val opResult = cityPopulationNameTraversal.modifyWithAction(CITY_MOUNTAIN_VIEW, thrower)

    opResult as Action.Throw

    assertThat(opResult.exception).isSameInstanceAs(exception)
  }

  @Test
  fun composeTraversal_withNullableNoList() {
    // TestPerson.pet is a nullable field.
    val mayoralPetNameTraversal =
      optics.composeMono<TestCity, String?>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "mayor", "pet", "name")
      )

    assertThat(mayoralPetNameTraversal.every(CITY_MOUNTAIN_VIEW).toList()).containsExactly("Geralt")

    val modified = mayoralPetNameTraversal.modify(CITY_MOUNTAIN_VIEW) { "$it$it" }

    assertThat(mayoralPetNameTraversal.every(modified).toList()).containsExactly("GeraltGeralt")
  }

  @Test
  fun composeTraversal_withNullableNoList_actionOmitFromRoot() {
    val mayoralPetNameTraversal =
      optics.composeMono<TestCity, String?>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "mayor", "pet", "name")
      )

    val omitter = Operation.create<String?>("Omitter") { Action.OmitFromRoot }

    val opResult = mayoralPetNameTraversal.modifyWithAction(CITY_MOUNTAIN_VIEW, omitter)

    assertThat(opResult).isSameInstanceAs(Action.OmitFromRoot)
  }

  @Test
  fun composeTraversal_withNullableNoList_actionUpdate() {
    val mayoralPetNameTraversal =
      optics.composeMono<TestCity, String?>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "mayor", "pet", "name")
      )

    val doubler =
      Operation.create<String?>("Doubler") {
        it?.let { Action.Update("$it$it") } ?: Action.Update(it)
      }

    val opResult = mayoralPetNameTraversal.modifyWithAction(CITY_MOUNTAIN_VIEW, doubler)
    val modified = (opResult as Action.Update).newValue

    assertThat(mayoralPetNameTraversal.every(modified).toList()).containsExactly("GeraltGeralt")
  }

  @Test
  fun composeTraversal_withNullableNoList_actionThrow() {
    val mayoralPetNameTraversal =
      optics.composeMono<TestCity, String?>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "mayor", "pet", "name")
      )

    val exception = IllegalArgumentException("Foo")
    val thrower = Operation.create<String?>("Thrower") { Action.Throw(exception) }

    val opResult = mayoralPetNameTraversal.modifyWithAction(CITY_MOUNTAIN_VIEW, thrower)

    opResult as Action.Throw

    assertThat(opResult.exception).isSameInstanceAs(exception)
  }

  @Test
  fun composeTraversal_withNullableList() {
    // TestPerson.pet is a nullable field.
    val populacePetNamesTraversal =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "pet", "name")
      )

    assertThat(populacePetNamesTraversal.every(CITY_MOUNTAIN_VIEW).toList())
      .containsExactly("Geralt", "Arya")

    val modified = populacePetNamesTraversal.modify(CITY_MOUNTAIN_VIEW) { "$it$it" }
    assertThat(populacePetNamesTraversal.every(modified).toList())
      .containsExactly("GeraltGeralt", "AryaArya")
  }

  @Test
  fun composeTraversal_sourceEntityTypeMismatch() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composeMono<TestPerson, String>(OpticalAccessPath(TEST_CITY_GENERATED_DTD, "name"))
      }

    assertThat(error)
      .hasMessageThat()
      .contains(
        "Could not compose a traversal for ${TestCity::class.java.name}::name with an input " +
          "entity type of ${TestPerson::class.java}"
      )
  }

  @Test
  fun composeTraversal_targetEntityTypeMismatch() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composePoly<TestPerson, TestCity, String, String>(
          OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name")
        )
      }

    assertThat(error)
      .hasMessageThat()
      .contains(
        "Could not compose a traversal for ${TestPerson::class.java.name}::name with an output " +
          "entity type of ${TestCity::class.java}"
      )
  }

  @Test
  fun composeTraversal_sourceFieldTypeMismatch() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composeMono<TestPerson, Int>(OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name"))
      }

    assertThat(error)
      .hasMessageThat()
      .contains(
        "Could not compose a traversal for ${TestPerson::class.java.name}::name with an input " +
          "field type of ${Integer::class.java}"
      )
  }

  @Test
  fun composeTraversal_targetFieldTypeMismatch() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composePoly<TestPerson, TestPerson, String, Int>(
          OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name")
        )
      }

    assertThat(error)
      .hasMessageThat()
      .contains(
        "Could not compose a traversal for ${TestPerson::class.java.name}::name with an output " +
          "field type of ${Integer::class.java}"
      )
  }

  @Test
  fun composeTraversal_noAssociatedOptic_notNested() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composeMono<TestPerson, String>(
          OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "lastName")
        )
      }

    assertThat(error)
      .hasMessageThat()
      .contains("${TestPerson::class.java.name}::lastName has no associated optic.")
  }

  @Test
  fun composeTraversal_noAssociatedOptic_nested() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composeMono<TestPerson, String>(
          OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "pet", "lastName")
        )
      }

    assertThat(error)
      .hasMessageThat()
      .contains("testdata.TestPet::lastName has no associated optic.")
  }

  @Test
  fun composeTraversal_noDtdFound() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composeMono<Foo, String>(OpticalAccessPath("Foo", "inner", "name"))
      }

    assertThat(error)
      .hasMessageThat()
      .contains("Could not find a DataTypeDescriptor for name: \"Foo\"")
  }

  @Test
  fun composeTraversal_fieldNotFoundForDtd() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composeMono<TestPerson, String>(
          OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "cousin", "name")
        )
      }

    assertThat(error)
      .hasMessageThat()
      .contains("Field \"cousin\" not found in ${TestPerson::class.java.name}")
  }

  @Test
  fun composeTraversal_nestedFieldInsidePrimitive() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        optics.composeMono<TestPerson, String>(
          OpticalAccessPath(TEST_PERSON_GENERATED_DTD, "name", "lastName")
        )
      }

    assertThat(error)
      .hasMessageThat()
      .contains(
        "Cannot find associated DataTypeDescriptor for field [name] of " +
          "${TestPerson::class.java.name}::name.lastName."
      )
  }

  @Test
  fun composeTraversal_modifyWithAction_update_nestedListInList() {
    val optic =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "pet", "name")
      )

    val redactingOp: (String) -> Action<out String> = { Action.Update("${it[0]}*****") }

    val result = optic.modifyWithAction(CITY_MOUNTAIN_VIEW, redactingOp)
    result as Action.Update

    assertThat(optic.every(result.newValue).toList()).containsExactly("G*****", "A*****")
  }

  @Test
  fun composeTraversal_modifyWithAction_omitFromParent_nestedListInList() {
    val optic =
      optics.composeMono<TestCity, String>(
        OpticalAccessPath(TEST_CITY_GENERATED_DTD, "populace", "pet", "name")
      )

    val omittingOp: (String) -> Action<out String> = {
      if (it == "Arya") Action.OmitFromParent else Action.Update(it)
    }

    val result = optic.modifyWithAction(CITY_MOUNTAIN_VIEW, omittingOp)
    result as Action.Update

    // Arya's owner should've been removed from the populace.
    assertThat(optic.every(result.newValue).toList()).containsExactly("Geralt")
    assertThat(result.newValue.populace).hasSize(2)
  }

  data class Foo(val name: String, val inner: Foo?)
}
