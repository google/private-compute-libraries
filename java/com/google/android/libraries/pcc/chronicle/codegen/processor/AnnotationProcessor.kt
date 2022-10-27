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

import javax.annotation.processing.AbstractProcessor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * Abstract annotation processor providing some helper methods for printing messages and
 * constructing package & file names. The [AbstractProcessor#process] method must still be
 * implemented by the subtype.
 */
abstract class AnnotationProcessor : AbstractProcessor() {

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

  /** Print an error - for problems that prevent normal completion. */
  protected fun printError(element: Element, message: String) {
    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message, element)
  }

  /** Print a note - for information from the processor. */
  protected fun printNote(element: Element, message: String) {
    processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message, element)
  }

  /** Print a warning, which does not prevent completion. */
  protected fun printWarning(element: Element, message: String) {
    processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message, element)
  }

  /** Get the package name of the receiving [Element]. */
  protected val Element.packageName: String
    get() = processingEnv.elementUtils.getPackageOf(this).qualifiedName.toString()
}
