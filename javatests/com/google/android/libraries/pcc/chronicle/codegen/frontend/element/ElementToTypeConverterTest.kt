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

package com.google.android.libraries.pcc.chronicle.codegen.frontend.element

import com.google.android.libraries.pcc.chronicle.codegen.AutoValueInDataClass
import com.google.android.libraries.pcc.chronicle.codegen.ExpectedTypes
import com.google.android.libraries.pcc.chronicle.codegen.NestedAutoValue
import com.google.android.libraries.pcc.chronicle.codegen.SimpleAutoValue
import com.google.android.libraries.pcc.chronicle.codegen.SimpleUnenclosedType
import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.testutil.TestAnnotationProcessor
import com.google.common.truth.Truth.assertThat
import kotlin.reflect.KClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ElementToTypeConverterTest {
  @Test
  fun thing() {
    testForType(SourceClasses.Thing::class, ExpectedTypes.thing)
  }

  @Test
  fun topLevelType() {
    testForType(SimpleUnenclosedType::class, ExpectedTypes.simpleUnenclosedType)
  }

  @Test
  fun nestedThing() {
    testForType(
      SourceClasses.NestedThing::class,
      ExpectedTypes.nestedThing,
      ExpectedTypes.simpleThing,
    )
  }

  @Test
  fun tripleNestedThing() {
    testForType(
      SourceClasses.Thing1::class,
      ExpectedTypes.thing1,
      ExpectedTypes.thing2,
      ExpectedTypes.thing3
    )
  }

  @Test
  fun listOfEntity() {
    testForType(
      SourceClasses.ListOfEntity::class,
      ExpectedTypes.listOfEntity,
      ExpectedTypes.simpleThing
    )
  }

  @Test
  fun mapOfEntity() {
    testForType(
      SourceClasses.MapOfEntity::class,
      ExpectedTypes.mapOfEntity,
      ExpectedTypes.simpleThing
    )
  }

  @Test
  fun mapWithEnumKey() {
    testForType(SourceClasses.MapWithEnumKey::class, ExpectedTypes.mapWithEnumKey)
  }

  @Test
  fun listOfListOfEntity() {
    testForType(
      SourceClasses.ListOfListOfEntity::class,
      ExpectedTypes.listOfListOfEntity,
      ExpectedTypes.simpleThing
    )
  }

  @Test
  fun mapOfListOfEntity() {
    testForType(
      SourceClasses.MapOfListOfEntity::class,
      ExpectedTypes.mapOfListOfEntity,
      ExpectedTypes.simpleThing
    )
  }

  @Test
  fun simpleAutoValue() {
    testForType(SimpleAutoValue::class, ExpectedTypes.simpleAutoValue)
  }

  @Test
  fun nestedAutoValue() {
    testForType(
      NestedAutoValue::class,
      ExpectedTypes.nestedAutoValue,
      ExpectedTypes.simpleAutoValue,
      ExpectedTypes.simpleThing
    )
  }

  @Test
  fun autoValueInDataClass() {
    testForType(
      AutoValueInDataClass::class,
      ExpectedTypes.autoValueInDataClass,
      ExpectedTypes.nestedAutoValue,
      ExpectedTypes.notAutoValue,
      ExpectedTypes.simpleAutoValue,
      ExpectedTypes.simpleThing,
    )
  }

  private fun testForType(cls: KClass<*>, vararg expect: Type) {
    val processor = TestAnnotationProcessor { processingEnv, element ->
      ElementToTypeConverter(processingEnv).convertElement(element)
    }
    val result = processor.runAnnotationTestForType(cls)
    assertThat(result).isEqualTo(TypeSet(*expect))
  }
}
