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

package com.google.android.libraries.pcc.chronicle.codegen.integration

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.codegen.AutoValueInDataClass
import com.google.android.libraries.pcc.chronicle.codegen.NestedAutoValue
import com.google.android.libraries.pcc.chronicle.codegen.SimpleAutoValue
import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses as Classes
import com.google.android.libraries.pcc.chronicle.codegen.backend.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.codegen.frontend.element.ElementToTypeConverter
import com.google.android.libraries.pcc.chronicle.codegen.readTestData
import com.google.android.libraries.pcc.chronicle.codegen.testutil.TestAnnotationProcessor
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import kotlin.reflect.KClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ElementToDataTypeDescriptorTest {

  @Test
  fun thing() {
    dataTypeDescriptorTest(Classes.Thing::class, "thing.txt")
  }

  @Test
  fun nested() {
    dataTypeDescriptorTest(Classes.NestedThing::class, "nested.txt")
  }

  @Test
  fun nestedType() {
    dataTypeDescriptorTest(Classes.NestedType::class, "nestedtype.txt")
  }

  @Test
  fun tripleNested() {
    dataTypeDescriptorTest(Classes.Thing1::class, "triplenested.txt")
  }

  @Test
  fun listOfEntity() {
    dataTypeDescriptorTest(Classes.ListOfEntity::class, "listofentity.txt")
  }

  @Test
  fun mapOfEntity() {
    dataTypeDescriptorTest(Classes.MapOfEntity::class, "mapofentity.txt")
  }

  @Test
  fun simpleAutoValue() {
    dataTypeDescriptorTest(SimpleAutoValue::class, "simpleautovalue.txt")
  }

  @Test
  fun nestedAutoValue() {
    dataTypeDescriptorTest(NestedAutoValue::class, "nestedautovalue.txt")
  }

  @Test
  fun autoValueInDataClass() {
    dataTypeDescriptorTest(AutoValueInDataClass::class, "autovalueindataclass.txt")
  }

  @Test
  fun subType() {
    dataTypeDescriptorTest(Classes.SubType::class, "subtype.txt")
  }

  /**
   * Run a test that converts the provided class in [klass] to a set of types, and then converts
   * those types to a dataTypeDescriptor, and then compares the results to the contents of the
   * provided test data file.
   */
  private fun dataTypeDescriptorTest(
    cls: KClass<*>,
    testDataFileName: String,
  ) {
    val processor = TestAnnotationProcessor { processingEnv, element ->
      ElementToTypeConverter(processingEnv).convertElement(element)
    }
    val types = processor.runAnnotationTestForType(cls)

    val block = types.dataTypeDescriptor()

    val testSpec = FileSpec.builder("chronicle.test", "test")

    testSpec.addProperty(
      PropertySpec.builder("dataTypeDescriptor", DataTypeDescriptor::class)
        .initializer(block)
        .build()
    )

    val output = "/* ktlint-disable */\n" + testSpec.build().toString()

    val expectedOutput =
      readTestData(
        "datatypedescriptor",
        testDataFileName,
      )
    assertThat(output).isEqualTo(expectedOutput)
  }
}
