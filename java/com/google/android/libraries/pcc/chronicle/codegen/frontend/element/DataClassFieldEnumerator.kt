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

import com.google.android.libraries.pcc.chronicle.codegen.util.ProcessingEnvHelpers
import com.google.android.libraries.pcc.chronicle.codegen.util.processingEnvHelpers
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

/**
 * An implementation of [FieldEnumerator] that supports simple property-only data structures, this
 * is the converter to use with Kotlin data classes, or any other class that's structured in a
 * similar fashion. This enumerates all visible "getter" methods on the type.
 *
 * For example, a method named `getValue` results in a [FieldDecl] with a field name of `value`.
 */
class DataClassFieldEnumerator(override val processingEnv: ProcessingEnvironment) :
  ProcessingEnvHelpers by processingEnvHelpers(processingEnv), FieldEnumerator {

  override fun fieldsInType(element: Element): List<FieldDecl> {
    return element
      .asTypeElement()
      .enclosedElements
      .asSequence()
      .mapNotNull { it.asGetterMethod() ?: it.asIsMethod() }
      .toList()
  }
}
