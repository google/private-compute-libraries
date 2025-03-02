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
import com.google.android.libraries.pcc.chronicle.codegen.testutil.withForeignReference
import com.google.android.libraries.pcc.chronicle.codegen.testutil.withoutFields
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Descriptors
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DescriptorToTypeConverterConfigTest {
  @Test
  fun ignoreByFieldName() {
    descriptorToTypeConfigTest(
      DescriptorToTypeConverter.Configuration(ignoredFieldNames = setOf("field1", "field5")),
      CodegenTestProto.Thing.getDescriptor(),
      ExpectedTypes.protoThing.forProtos().withoutFields("field1", "field5"),
      ExpectedTypes.protoTimestamp.forProtos(),
    )
  }

  @Test
  fun ignoreNestedFieldName() {
    descriptorToTypeConfigTest(
      DescriptorToTypeConverter.Configuration(ignoredFieldNames = setOf("field1")),
      CodegenTestProto.NestedThing.getDescriptor(),
      ExpectedTypes.nestedThing.forProtos().withoutFields("field1"),
      ExpectedTypes.simpleThing.forProtos().withoutFields("field1"),
    )
  }

  @Test
  fun ignoreByMessageType() {
    descriptorToTypeConfigTest(
      DescriptorToTypeConverter.Configuration(
        ignoredMessageType =
          setOf(
            Descriptors.FieldDescriptor.Type.FLOAT.name,
            Descriptors.FieldDescriptor.Type.STRING.name,
          )
      ),
      CodegenTestProto.Thing.getDescriptor(),
      ExpectedTypes.protoThing.forProtos().withoutFields("field1", "field5", "field9"),
      ExpectedTypes.protoTimestamp.forProtos(),
    )
  }

  @Test
  fun ignoreByNestedMessageType() {
    descriptorToTypeConfigTest(
      DescriptorToTypeConverter.Configuration(ignoredMessageType = setOf("SimpleThing")),
      CodegenTestProto.NestedThing.getDescriptor(),
      ExpectedTypes.nestedThing.forProtos().withoutFields("field2"),
    )
  }

  @Test
  fun foreignReference() {
    descriptorToTypeConfigTest(
      DescriptorToTypeConverter.Configuration(
        foreignReferences = setOf(ForeignReference("field1", "PackageName", true, ""))
      ),
      CodegenTestProto.Thing.getDescriptor(),
      ExpectedTypes.protoThing.forProtos().withForeignReference("field1", "PackageName", true),
      ExpectedTypes.protoTimestamp.forProtos(),
    )
  }

  @Test
  fun foreignReference_additional() {
    descriptorToTypeConfigTest(
      DescriptorToTypeConverter.Configuration(
        foreignReferences = setOf(ForeignReference("field1", "PackageName", true, "field1_hardref"))
      ),
      CodegenTestProto.Thing.getDescriptor(),
      ExpectedTypes.protoThing
        .forProtos()
        .withForeignReference("field1", "PackageName", true, "field1Hardref"),
      ExpectedTypes.protoTimestamp.forProtos(),
    )
  }

  private fun descriptorToTypeConfigTest(
    config: DescriptorToTypeConverter.Configuration,
    desc: Descriptors.Descriptor,
    vararg expect: Type,
  ) {
    val types: Set<Type> = DescriptorToTypeConverter(config = config).convertToTypes(desc)
    assertThat(types).isEqualTo(TypeSet(*expect))
  }
}
