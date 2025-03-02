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

package com.google.android.libraries.pcc.chronicle.codegen.backend

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.codegen.ExpectedTypes
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.readTestData
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeToDataTypeDescriptorTest {
  @Test
  fun thing() {
    dataTypeDescriptorTest("thing.txt", ExpectedTypes.thing)
  }

  @Test
  fun nested() {
    dataTypeDescriptorTest("nested.txt", ExpectedTypes.nestedThing, ExpectedTypes.simpleThing)
  }

  @Test
  fun nestedType() {
    dataTypeDescriptorTest(
      "nestedtype.txt",
      ExpectedTypes.nestedType,
      ExpectedTypes.innerType,
      ExpectedTypes.innerInnerType,
    )
  }

  @Test
  fun tripleNested() {
    dataTypeDescriptorTest(
      "triplenested.txt",
      ExpectedTypes.thing1,
      ExpectedTypes.thing2,
      ExpectedTypes.thing3,
    )
  }

  @Test
  fun listOfEntity() {
    dataTypeDescriptorTest(
      "listofentity.txt",
      ExpectedTypes.listOfEntity,
      ExpectedTypes.simpleThing,
    )
  }

  @Test
  fun mapOfEntity() {
    dataTypeDescriptorTest("mapofentity.txt", ExpectedTypes.mapOfEntity, ExpectedTypes.simpleThing)
  }

  @Test
  fun subType() {
    dataTypeDescriptorTest("subtype.txt", ExpectedTypes.subType)
  }

  private fun dataTypeDescriptorTest(
    name: String,
    primaryType: Type,
    vararg additionalTypes: Type,
  ) {
    val testSpec = FileSpec.builder("chronicle.test", "test")

    val types = TypeSet(primaryType, setOf(*additionalTypes))
    val block = types.dataTypeDescriptor()

    testSpec.addProperty(
      PropertySpec.builder("dataTypeDescriptor", DataTypeDescriptor::class)
        .initializer(block)
        .build()
    )

    val output = "/* ktlint-disable */\n" + testSpec.build().toString()

    val expectedOutput = readTestData("datatypedescriptor", name)
    assertThat(output).isEqualTo(expectedOutput)
  }
}
