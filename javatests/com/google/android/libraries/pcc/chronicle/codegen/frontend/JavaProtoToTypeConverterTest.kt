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

import com.google.android.libraries.pcc.chronicle.codegen.CodegenTestProto
import com.google.android.libraries.pcc.chronicle.codegen.ExpectedTypes
import com.google.android.libraries.pcc.chronicle.codegen.ExpectedTypes.forProtos
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.common.truth.Truth.assertThat
import kotlin.reflect.KClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JavaProtoToTypeConverterTest {
  @Test
  fun protoThing() {
    descriptorToTypeTest(
      CodegenTestProto.Thing::class,
      ExpectedTypes.protoThing.forProtos(),
      ExpectedTypes.protoTimestamp.forProtos()
    )
  }

  @Test
  fun nestedThing() {
    descriptorToTypeTest(
      CodegenTestProto.NestedThing::class,
      ExpectedTypes.nestedThing.forProtos(),
      ExpectedTypes.simpleThing.forProtos(),
    )
  }

  @Test
  fun nestedType() {
    descriptorToTypeTest(
      CodegenTestProto.NestedType::class,
      ExpectedTypes.nestedType.forProtos(),
      ExpectedTypes.innerType.forProtos(),
      ExpectedTypes.innerInnerType.forProtos()
    )
  }

  @Test
  fun tripleNestedThing() {
    descriptorToTypeTest(
      CodegenTestProto.Thing1::class,
      ExpectedTypes.thing1.forProtos(),
      ExpectedTypes.thing2.forProtos(),
      ExpectedTypes.thing3.forProtos()
    )
  }

  @Test
  fun oneOfThing() {
    descriptorToTypeTest(
      CodegenTestProto.OneOfThing::class,
      ExpectedTypes.oneOfThing(enableNullable = false).forProtos(),
      ExpectedTypes.stringThing.forProtos(),
      ExpectedTypes.intThing.forProtos(),
      ExpectedTypes.otherThing.forProtos()
    )
  }

  @Test
  fun recursiveRef() {
    descriptorToTypeTest(
      CodegenTestProto.RecursiveRefA::class,
      ExpectedTypes.recursiveRefA.forProtos(),
      ExpectedTypes.recursiveRefB.forProtos(),
    )
  }

  @Test
  fun recursiveRepeatedRef() {
    descriptorToTypeTest(
      CodegenTestProto.RecursiveListRefA::class,
      ExpectedTypes.recursiveListRefA.forProtos(),
      ExpectedTypes.recursiveListRefB.forProtos(),
    )
  }

  @Test
  fun recursiveMapRef() {
    descriptorToTypeTest(
      CodegenTestProto.RecursiveMapRefA::class,
      ExpectedTypes.recursiveMapRefA.forProtos(),
      ExpectedTypes.recursiveMapRefB.forProtos(),
    )
  }

  @Test
  fun repeatedGroupTest() {
    descriptorToTypeTest(
      CodegenTestProto.RepeatedGroup::class,
      ExpectedTypes.repeatedGroup.forProtos(),
      ExpectedTypes.repeatedGroupInner.forProtos(),
      ExpectedTypes.nestedThing.forProtos(),
      ExpectedTypes.simpleThing.forProtos()
    )
  }

  private fun descriptorToTypeTest(cls: KClass<*>, vararg expect: Type) {
    val types = JavaProtoTypeConverter().convertToTypes(cls.java)
    assertThat(types).isEqualTo(TypeSet(*expect))
  }
}
