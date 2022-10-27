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

import com.google.android.libraries.pcc.chronicle.codegen.util.decapitalize
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

private val getterPattern = Regex("get([A-Z][A-Za-z0-9_]*)")
private val isPattern = Regex("is([A-Z][A-Za-z0-9_]*)")

/** Attempt to interpret an element as an abstract method (used to define values in @AutoValue). */
fun Element.asAbstractValueMethod(): FieldDecl? =
  abstractMethodFieldName()?.let { name ->
    FieldDecl(name = name, type = (this as ExecutableElement).returnType)
  }

private fun Element.isAbstractValueMethod() =
  kind == ElementKind.METHOD && Modifier.STATIC !in modifiers && Modifier.ABSTRACT in modifiers

private fun Element.abstractMethodFieldName(): String? =
  if (isAbstractValueMethod()) {
    simpleName.toString()
  } else {
    null
  }

fun Element.asIsMethod(): FieldDecl? =
  fieldNameForIsMethod()?.let {
    FieldDecl(name = simpleName.toString(), type = (this as ExecutableElement).returnType)
  }

fun Element.fieldNameForIsMethod(): String? =
  takeIf { isMemberMethod() && isPattern.matches(simpleName) }?.simpleName?.toString()

fun Element.asGetterMethod(): FieldDecl? =
  getterMethodFieldName()?.let { name ->
    FieldDecl(name = name, type = (this as ExecutableElement).returnType)
  }

private fun Element.getterMethodFieldName(): String? =
  if (isMemberMethod()) {
    getterPattern.find(simpleName)?.groupValues?.get(1)?.decapitalize()
  } else {
    null
  }

private fun Element.isMemberMethod(): Boolean =
  kind == ElementKind.METHOD && Modifier.STATIC !in modifiers
