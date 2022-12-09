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

package com.google.android.libraries.pcc.chronicle.codegen.processor

import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData
import com.google.android.libraries.pcc.chronicle.codegen.backend.DataTypeDescriptorPropertyProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.FileSpecContentsProvider
import com.google.android.libraries.pcc.chronicle.codegen.frontend.element.ElementToTypeConverter
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * This annotation processor will operate on any type annotated with the [ChronicleData] annotation.
 * For a type `TypeName`, it will create a file `TypeName_Generated.kt`, containing a top-level
 * property `TypeName_GENERATED_DTD` containing the generated DTD contents.
 */
@AutoService(ChronicleDataAnnotationProcessor::class)
class ChronicleDataAnnotationProcessor : AnnotationProcessor() {
  override fun getSupportedAnnotationTypes() = setOf(ChronicleData::class.java.canonicalName!!)

  override fun process(elements: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
    env.getElementsAnnotatedWith(ChronicleData::class.java).forEach { element ->
      val converter = ElementToTypeConverter(processingEnv)
      val types = converter.convertElement(element)
      printNote(element, "Generating DTD for $element")
      val dtd = DataTypeDescriptorPropertyProvider(element.simpleName, types)

      element.generateFile(dtd).writeTo(processingEnv.filer)
    }

    return true
  }

  /**
   * Generate the wrapper file that will hold the generated DataTypeDescriptor.
   *
   * Looks like:
   * ```
   * package <packageName>
   *
   * import <needed imports>
   *
   * val TypeName_GENERATED_DTD: DataTypeDescriptor = dataTypeDescriptor(
   *     name = <fully qualified class name>, cls = TypeName::class) {
   *       "key0" to FieldType.<fieldType0>
   *       "key1" to FieldType.<fieldType1>
   *       ...
   * }
   * ```
   */
  private fun Element.generateFile(dtd: FileSpecContentsProvider): FileSpec {
    return FileSpec.builder(packageName, outFileName).also { dtd.provideContentsInto(it) }.build()
  }

  /** Get the output filename of the receiving [Element]. */
  private val Element.outFileName: String
    get() = "${this.simpleName}_Generated"
}
