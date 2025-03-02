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
import com.google.protobuf.Descriptors
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DescriptorToTypeConverterTest {
  @Test
  fun protoThing() {
    descriptorToTypeTest(
      CodegenTestProto.Thing.getDescriptor(),
      ExpectedTypes.protoThing.forProtos(),
      ExpectedTypes.protoTimestamp.forProtos(),
    )
  }

  @Test
  fun protoThing_bytesAsByteArrays() {
    descriptorToTypeTestWithConfig(
      CodegenTestProto.Thing.getDescriptor(),
      DescriptorToTypeConverter.Configuration(convertBytesFieldsToStrings = false),
      ExpectedTypes.protoThing.forProtos(convertBytesToStrings = false),
      ExpectedTypes.protoTimestamp.forProtos(convertBytesToStrings = false),
    )
  }

  @Test
  fun nestedThing() {
    descriptorToTypeTest(
      CodegenTestProto.NestedThing.getDescriptor(),
      ExpectedTypes.nestedThing.forProtos(),
      ExpectedTypes.simpleThing.forProtos(),
    )
  }

  @Test
  fun nestedType() {
    descriptorToTypeTest(
      CodegenTestProto.NestedType.getDescriptor(),
      ExpectedTypes.nestedType.forProtos(),
      ExpectedTypes.innerType.forProtos(),
      ExpectedTypes.innerInnerType.forProtos(),
    )
  }

  @Test
  fun tripleNestedThing() {
    descriptorToTypeTest(
      CodegenTestProto.Thing1.getDescriptor(),
      ExpectedTypes.thing1.forProtos(),
      ExpectedTypes.thing2.forProtos(),
      ExpectedTypes.thing3.forProtos(),
    )
  }

  @Test
  fun oneOfThing() {
    descriptorToTypeTest(
      CodegenTestProto.OneOfThing.getDescriptor(),
      ExpectedTypes.oneOfThing(enableNullable = false).forProtos(),
      ExpectedTypes.stringThing.forProtos(),
      ExpectedTypes.intThing.forProtos(),
      ExpectedTypes.otherThing.forProtos(),
    )
  }

  @Test
  fun oneOfThingNullable() {
    descriptorToTypeTestWithConfig(
      CodegenTestProto.OneOfThing.getDescriptor(),
      DescriptorToTypeConverter.Configuration(enableNullableSupport = true),
      ExpectedTypes.oneOfThing(enableNullable = true).forProtos(),
      ExpectedTypes.stringThing.forProtos(),
      ExpectedTypes.intThing.forProtos(),
      ExpectedTypes.otherThing.forProtos(),
    )
  }

  @Test
  fun recursiveRef() {
    descriptorToTypeTest(
      CodegenTestProto.RecursiveRefA.getDescriptor(),
      ExpectedTypes.recursiveRefA.forProtos(),
      ExpectedTypes.recursiveRefB.forProtos(),
    )
  }

  @Test
  fun recursiveRepeatedRef() {
    descriptorToTypeTest(
      CodegenTestProto.RecursiveListRefA.getDescriptor(),
      ExpectedTypes.recursiveListRefA.forProtos(),
      ExpectedTypes.recursiveListRefB.forProtos(),
    )
  }

  @Test
  fun recursiveMapRef() {
    descriptorToTypeTest(
      CodegenTestProto.RecursiveMapRefA.getDescriptor(),
      ExpectedTypes.recursiveMapRefA.forProtos(),
      ExpectedTypes.recursiveMapRefB.forProtos(),
    )
  }

  @Test
  fun repeatedGroupTest() {
    descriptorToTypeTest(
      CodegenTestProto.RepeatedGroup.getDescriptor(),
      ExpectedTypes.repeatedGroup.forProtos(),
      ExpectedTypes.repeatedGroupInner.forProtos(),
      ExpectedTypes.nestedThing.forProtos(),
      ExpectedTypes.simpleThing.forProtos(),
    )
  }

  @Test
  fun typeLocationPackage_fromJavaPackage_whenUseJavaPackageInTypeNameDefault() {
    val expected =
      ExpectedTypes.protoThing.forProtos(
        overridePkg = CodegenTestProto.Thing.getDescriptor().file.options.javaPackage
      )

    descriptorToTypeTestWithConfig(
      CodegenTestProto.Thing.getDescriptor(),
      DescriptorToTypeConverter.Configuration(),
      expected,
      ExpectedTypes.protoTimestamp.forProtos(),
    )
  }

  @Test
  fun typeLocationPackage_fromJavaPackage_whenUseJavaPackageInTypeNameTrue() {
    val expected =
      ExpectedTypes.protoThing.forProtos(
        overridePkg = CodegenTestProto.Thing.getDescriptor().file.options.javaPackage
      )

    descriptorToTypeTestWithConfig(
      CodegenTestProto.Thing.getDescriptor(),
      DescriptorToTypeConverter.Configuration(useJavaPackageInTypeLocations = true),
      expected,
      ExpectedTypes.protoTimestamp.forProtos(),
    )
  }

  @Test
  fun typeLocationPackage_fromProtoPackage_whenUseJavaPackageInTypeNameFalse() {
    val expected =
      ExpectedTypes.protoThing.forProtos(
        overridePkg = CodegenTestProto.Thing.getDescriptor().file.`package`,
        timestampOverridePkg = "google.protobuf",
      )

    descriptorToTypeTestWithConfig(
      CodegenTestProto.Thing.getDescriptor(),
      DescriptorToTypeConverter.Configuration(useJavaPackageInTypeLocations = false),
      expected,
      ExpectedTypes.protoTimestamp.forProtos(timestampOverridePkg = "google.protobuf"),
    )
  }

  private fun descriptorToTypeTest(desc: Descriptors.Descriptor, vararg expect: Type) {
    descriptorToTypeTestWithConfig(desc, DescriptorToTypeConverter.Configuration(), *expect)
  }

  private fun descriptorToTypeTestWithConfig(
    desc: Descriptors.Descriptor,
    config: DescriptorToTypeConverter.Configuration,
    vararg expect: Type,
  ) {
    val types: Set<Type> = DescriptorToTypeConverter(config).convertToTypes(desc)
    assertThat(types).isEqualTo(TypeSet(*expect))
  }
}
