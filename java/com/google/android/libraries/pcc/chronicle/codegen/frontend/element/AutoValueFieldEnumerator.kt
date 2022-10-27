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
import com.google.auto.value.AutoValue
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

/**
 * An implementation of [FieldEnumerator] that supports the structure of a Java [AutoValue] type. It
 * works by assuming that all methods on the class represent a field in the data type. If the class
 * implements concrete field getters using the `getName` naming pattern, they'll be converted into a
 * field called `name`.
 */
class AutoValueFieldEnumerator(override val processingEnv: ProcessingEnvironment) :
  FieldEnumerator, ProcessingEnvHelpers by processingEnvHelpers(processingEnv) {
  override fun fieldsInType(element: Element): List<FieldDecl>? {
    val supported = element.isAnnotatedWith(AutoValue::class.java)

    if (!supported) {
      return null
    }

    // Assume that all methods returning a type are considered fields of this datatype.
    return element
      .asTypeElement()
      .enclosedElements
      .asSequence()
      .mapNotNull { it.asAbstractValueMethod() ?: it.asGetterMethod() ?: it.asIsMethod() }
      .toList()
  }
}
