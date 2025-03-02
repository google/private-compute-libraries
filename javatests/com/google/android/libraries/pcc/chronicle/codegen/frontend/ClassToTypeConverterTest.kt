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

package com.google.android.libraries.pcc.chronicle.codegen.frontend

import com.google.android.libraries.pcc.chronicle.codegen.ExpectedTypes
import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses
import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses.NestedThing
import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses.Thing
import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses.Thing1
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.common.truth.Truth.assertThat
import kotlin.reflect.KClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ClassToTypeConverterTest {
  private var converter = ClassToTypeConverter()

  @Test
  fun unnestedCase() {
    classToTypeTest(Thing::class, ExpectedTypes.thing)
  }

  @Test
  fun nestedCase() {
    classToTypeTest(NestedThing::class, ExpectedTypes.nestedThing, ExpectedTypes.simpleThing)
  }

  @Test
  fun nestedType() {
    classToTypeTest(
      SourceClasses.NestedType::class,
      ExpectedTypes.nestedType,
      ExpectedTypes.innerType,
      ExpectedTypes.innerInnerType,
    )
  }

  @Test
  fun tripleNestedCase() {
    classToTypeTest(Thing1::class, ExpectedTypes.thing1, ExpectedTypes.thing2, ExpectedTypes.thing3)
  }

  @Test
  fun listOfEntity() {
    classToTypeTest(
      SourceClasses.ListOfEntity::class,
      ExpectedTypes.listOfEntity,
      ExpectedTypes.simpleThing,
    )
  }

  @Test
  fun mapOfEntity() {
    classToTypeTest(
      SourceClasses.MapOfEntity::class,
      ExpectedTypes.mapOfEntity,
      ExpectedTypes.simpleThing,
    )
  }

  @Test
  fun mapWithEnumKey() {
    classToTypeTest(SourceClasses.MapWithEnumKey::class, ExpectedTypes.mapWithEnumKey)
  }

  @Test
  fun listOfListOfEntity() {
    classToTypeTest(
      SourceClasses.ListOfListOfEntity::class,
      ExpectedTypes.listOfListOfEntity,
      ExpectedTypes.simpleThing,
    )
  }

  @Test
  fun mapOfListOfEntity() {
    classToTypeTest(
      SourceClasses.MapOfListOfEntity::class,
      ExpectedTypes.mapOfListOfEntity,
      ExpectedTypes.simpleThing,
    )
  }

  @Test
  fun recursiveReference() {
    classToTypeTest(
      SourceClasses.RecursiveRefA::class,
      ExpectedTypes.recursiveRefA,
      ExpectedTypes.recursiveRefB,
    )
  }

  @Test
  fun recursiveListReference() {
    classToTypeTest(
      SourceClasses.RecursiveListRefA::class,
      ExpectedTypes.recursiveListRefA,
      ExpectedTypes.recursiveListRefB,
    )
  }

  @Test
  fun recursiveMapReference() {
    classToTypeTest(
      SourceClasses.RecursiveMapRefA::class,
      ExpectedTypes.recursiveMapRefA,
      ExpectedTypes.recursiveMapRefB,
    )
  }

  private fun classToTypeTest(cls: KClass<*>, vararg expect: Type) {
    assertThat(converter.convertToTypes(cls.java)).isEqualTo(TypeSet(*expect))
  }
}
