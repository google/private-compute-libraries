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

package com.google.android.libraries.pcc.chronicle.codegen.tool

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.codegen.CodegenTestProto
import com.google.android.libraries.pcc.chronicle.codegen.SimpleTestProto1
import com.google.android.libraries.pcc.chronicle.codegen.SimpleTestProto2
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleProvider
import com.google.android.libraries.pcc.chronicle.codegen.tool.ProtoChronicleDataGenerator.Companion.javaClasses
import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProtoChronicleDataGeneratorTest {
  @Test
  fun fileDescriptorJavaClasses_outerClassName() {
    // Get the full file descriptor from the Thing descriptor.
    val fileDescriptor = CodegenTestProto.Thing.getDescriptor().file

    // Use the extension method to fetch all of the defined classes within.
    val testProtoClasses = fileDescriptor.javaClasses()

    assertThat(testProtoClasses.asSequence().toList())
      .containsExactly(
        CodegenTestProto.ListOfEntity::class.java,
        CodegenTestProto.MapOfEntity::class.java,
        CodegenTestProto.NestedThing::class.java,
        CodegenTestProto.NestedType::class.java,
        CodegenTestProto.OneOfThing::class.java,
        CodegenTestProto.RecursiveListRefA::class.java,
        CodegenTestProto.RecursiveListRefB::class.java,
        CodegenTestProto.RecursiveMapRefA::class.java,
        CodegenTestProto.RecursiveMapRefB::class.java,
        CodegenTestProto.RecursiveRefA::class.java,
        CodegenTestProto.RecursiveRefB::class.java,
        CodegenTestProto.RepeatedGroup::class.java,
        CodegenTestProto.Thing::class.java,
        CodegenTestProto.Thing1::class.java,
        CodegenTestProto.Thing2::class.java,
        CodegenTestProto.Thing3::class.java,
      )
  }

  @Test
  fun fileDescriptorJavaClasses_noOuterClassName() {
    // Get the full file descriptor from the SimpleTestProto1 descriptor.
    val fileDescriptor = SimpleTestProto1.getDescriptor().file

    // Use the extension method to fetch all of the defined classes within.
    val testProtoClasses = fileDescriptor.javaClasses()

    assertThat(testProtoClasses.asSequence().toList())
      .containsExactly(SimpleTestProto1::class.java, SimpleTestProto2::class.java)
  }

  @Test
  fun provideIntoKotlinFile_containsDtd() {
    val generator = ProtoChronicleDataGenerator(SimpleTestProto1::class)
    val spec =
      generator.provideIntoKotlinFile(
        FileSpec.builder("com.google", "SimpleTestProto_Generated.kt")
      )

    val dtdProperty = spec.build().members[0] as PropertySpec
    assertThat(dtdProperty.name).isEqualTo("SIMPLE_TEST_PROTO1_GENERATED_DTD")
    assertThat(dtdProperty.type).isEqualTo(DataTypeDescriptor::class.asTypeName())
  }

  @Test
  fun provideIntoKotlinFile_thing() {
    val generator = ProtoChronicleDataGenerator(CodegenTestProto.Thing::class)
    val spec =
      generator
        .provideIntoKotlinFile(
          FileSpec.builder(
            packageName = "com.google.android.libraries.pcc.chronicle.codegen",
            fileName = "Thing_Generated.kt",
          )
        )
        .build()

    val expectedContents = loadResourceAsString("goldens/ThingGolden.txt")
    val actualContents = StringBuilder().also { spec.writeTo(it) }.toString().trim()
    assertThat(actualContents).isEqualTo(expectedContents)
  }

  @Test
  fun buildDaggerProviders_thing() {
    val generator = ProtoChronicleDataGenerator(CodegenTestProto.Thing::class)
    val providers = generator.buildDaggerProviders("Thing_GeneratedKt")

    val daggerModule = DaggerModuleProvider("Thing_GeneratedModule", contents = providers)
    val daggerFile =
      JavaFile.builder(
          "com.google.android.libraries.pcc.chronicle.codegen",
          daggerModule.provideModule(),
        )
        .build()

    val expectedContents = loadResourceAsString("goldens/ThingModuleGolden.txt")
    val actualContents = StringBuilder().also { daggerFile.writeTo(it) }.toString().trim()
    assertThat(actualContents).isEqualTo(expectedContents)
  }

  private fun loadResourceAsString(relativePath: String): String {
    return this::class
      .java
      .getResourceAsStream(relativePath)
      ?.bufferedReader()
      ?.lineSequence()
      ?.joinToString("\n")
      ?.trim() ?: ""
  }
}
