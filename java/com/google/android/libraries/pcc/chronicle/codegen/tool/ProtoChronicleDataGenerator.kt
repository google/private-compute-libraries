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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.backend.DataTypeDescriptorDaggerProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.DataTypeDescriptorPropertyProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleContentsProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.lens.LensDaggerProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.lens.LensPropertyProvider
import com.google.android.libraries.pcc.chronicle.codegen.frontend.DescriptorToTypeConverter
import com.google.android.libraries.pcc.chronicle.codegen.frontend.JavaProtoTypeConverter
import com.google.common.annotations.VisibleForTesting
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors.FileDescriptor
import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.FileSpec
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.reflect.KClass

/**
 * Creates a kotlin file from a proto descriptor as if the generated proto class were annotated with
 * `@ChronicleData`. Similar to: [ChronicleDataAnnotationProcessor].
 */
class ProtoChronicleDataGenerator(private val protoClass: Class<*>) {
  constructor(protoClass: KClass<*>) : this(protoClass.java)

  private val javaProtoTypeConverter =
    JavaProtoTypeConverter(
      DescriptorToTypeConverter.Configuration(
        useJavaPackageInTypeLocations = false,
        enableNullableSupport = true,
        convertBytesFieldsToStrings = false
      )
    )

  private val types: TypeSet? by lazy { javaProtoTypeConverter.convertToTypes(protoClass) }

  private val dtd: DataTypeDescriptorPropertyProvider? by lazy {
    val types = this.types ?: return@lazy null
    DataTypeDescriptorPropertyProvider(protoClass.simpleName, types)
  }

  private val lenses: List<LensPropertyProvider> by lazy {
    val types = this.types ?: return@lazy emptyList()
    types.primary.fields.map { field -> LensPropertyProvider(types.primary, field) }
  }

  /** Contributes DTDs and lenses from the [protoClass] to the given kotlin [FileSpec.Builder]. */
  fun provideIntoKotlinFile(builder: FileSpec.Builder): FileSpec.Builder {
    dtd?.provideContentsInto(builder)
    lenses.forEach { lens -> lens.provideContentsInto(builder) }

    return builder
  }

  /**
   * Returns a list of [DaggerModuleContentsProvider] instances for use in generating a dagger
   * module based around the lenses gleaned from the [protoClass].
   */
  fun buildDaggerProviders(kotlinFileClassName: String): List<DaggerModuleContentsProvider> {
    val lensContents =
      lenses.map { LensDaggerProvider(it.type, it.field, lensClassName = kotlinFileClassName) }
    val dtdContents =
      listOf(DataTypeDescriptorDaggerProvider(protoClass.simpleName, kotlinFileClassName))
    return lensContents + dtdContents
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      object :
          CliktCommand(
            help =
              """
          |Generates Chronicle Data code from Protocol Buffer Descriptors. This is
          |intended to be run from blaze, not directly.
          |
          |Note: in order for this to be able to work correctly, the proto's generated java class
          |must be available on the classpath.
          |"""
                .trimMargin(),
          ) {
          val moduleForConstantsFile by option("--module_for").default("")
          val outputFilepath by argument().path()
          val descriptorFile by argument().path(mustBeReadable = true)

          override fun run() {
            val generatingModule = moduleForConstantsFile.isNotEmpty()
            val fileDescriptorSet = descriptorFile.readFileDescriptorSet()
            val fileDescriptorProto = fileDescriptorSet.fileList.first()
            val descriptor = FileDescriptor.buildFrom(fileDescriptorProto, emptyArray(), true)

            val outputFile = outputFilepath.toFile()
            val kotlinFileName =
              if (!generatingModule) {
                requireNotNull(outputFile.name.takeIf { it.endsWith(".kt") }?.dropLast(3)) {
                  "Kotlin output file must end in .kt"
                }
              } else {
                moduleForConstantsFile
              }
            val moduleFileName =
              if (generatingModule) {
                requireNotNull(outputFile.name.takeIf { it.endsWith(".java") }?.dropLast(5)) {
                  "Module output file must end in .java"
                }
              } else {
                ""
              }

            val randomSuffix = Random.Default.nextUInt()
            val kotlinFileBuilder =
              FileSpec.builder(descriptor.options.javaPackage, "${kotlinFileName}_$randomSuffix.kt")
            val daggerProviders = mutableListOf<DaggerModuleContentsProvider>()

            // For each class within the descriptor, accumulate contents for the kotlin and dagger
            // module output files.
            descriptor.javaClasses().forEach {
              val generator = ProtoChronicleDataGenerator(it)

              generator.provideIntoKotlinFile(kotlinFileBuilder)
              daggerProviders += generator.buildDaggerProviders("${kotlinFileName}Kt")
            }

            if (!generatingModule) {
              // Flush the kotlin file to disk.
              BufferedWriter(FileWriter(outputFile)).use {
                kotlinFileBuilder.build().writeTo(it)
                it.flush()
              }
            } else {
              // Construct the dagger module and flush it to disk.
              val daggerModule =
                DaggerModuleProvider(name = moduleFileName, contents = daggerProviders)
              BufferedWriter(FileWriter(outputFile)).use {
                JavaFile.builder(descriptor.options.javaPackage, daggerModule.provideModule())
                  .build()
                  .writeTo(it)
                it.flush()
              }
            }
          }
        }
        .main(args)
    }

    /**
     * Given a proto [FileDescriptor], return an [Iterator] of the associated java [Classes][Class].
     */
    @VisibleForTesting
    fun FileDescriptor.javaClasses(): Iterator<Class<*>> {
      val multipleFiles = options.hasJavaMultipleFiles() && options.javaMultipleFiles
      val pkg =
        if (options.hasJavaOuterClassname() && !multipleFiles) {
          "${options.javaPackage}.${options.javaOuterClassname}$"
        } else {
          "${options.javaPackage}."
        }

      return iterator {
        messageTypes.forEach { message -> yield(Class.forName("$pkg${message.name}")) }
      }
    }

    private fun Path.readFileDescriptorSet(): DescriptorProtos.FileDescriptorSet =
      Files.newInputStream(this).buffered().use { DescriptorProtos.FileDescriptorSet.parseFrom(it) }
  }
}
