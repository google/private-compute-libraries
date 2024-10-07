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

package com.google.android.libraries.pcc.chronicle.codegen.util

import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

interface ProcessingEnvHelpers {
  val processingEnv: ProcessingEnvironment

  /**
   * Returns a list of strings where each string represents the name of the enum constants in an
   * enum. This helper doesn't check that the provided type is actually an enum.
   */
  fun Element.enumValues(): List<String> =
    enclosedElements
      .filter { it.kind == ElementKind.ENUM_CONSTANT }
      .map { it.simpleName.toString() }

  /**
   * Returns the provided [TypeMirror] wrapped in a [ComparableType] comparison helper class to make
   * comparisons via `equals` work.
   */
  fun TypeMirror.comparableType(): ComparableType = ComparableType(this, processingEnv)

  /** Removes any type parameter information from a type. Useful for comparisons. */
  fun TypeMirror.rawType(): TypeMirror = processingEnv.typeUtils.erasure(this)

  /** Converts a string literal class name into a [TypeMirror], ignoring type params. */
  fun String.asNamedRawType(): TypeMirror = requireNotNull(asNamedRawTypeOrNull())

  /** Converts a string literal class name into a [TypeMirror], or null if not found. */
  fun String.asNamedRawTypeOrNull(): TypeMirror? =
    processingEnv.elementUtils.getTypeElement(this)?.asType()?.rawType()

  /** Returns the [Element] that represents the type of the receiving [Element]. */
  // TODO: go/nullness-caller-updates-lsc - Avoid dereferencing possibly null value?
  fun Element.asTypeElement(): Element = processingEnv.typeUtils.asElement(this.asType())!!

  /** Returns the [idx]th type parameter for the [TypeMirror]. Does not do any validity checks. */
  fun TypeMirror.typeParameter(idx: Int): TypeMirror = (this as DeclaredType).typeArguments[idx]

  /** Returns the [PackageElement] associated with this [Element]. */
  fun Element.packageName(): PackageElement = processingEnv.elementUtils.getPackageOf(this)

  /**
   * Returns a list of strings representing an enclosing types for a class.
   *
   * The enclosing types will be returned in list ordered inner-most first.
   *
   * For example, `java.util.Map.Entry` will return ["Map"].
   * `some.package.OuterMost.Middle.Inner.ClassName` will return ["Inner", "Middle", "OuterMost"]
   */
  fun Element.enclosingTypeNames(): List<String> =
    generateSequence(this) { element ->
        element.enclosingElement?.takeIf { it.kind != ElementKind.PACKAGE }
      }
      .drop(1) // Skip the top level
      .map { it.simpleName.toString() }
      .toList()

  /** Creates a [TypeLocation] from the information present int he [Element]. */
  fun Element.asTypeLocation() =
    TypeLocation(
      asTypeElement().simpleName.toString(),
      asTypeElement().enclosingTypeNames(),
      asTypeElement().packageName().toString(),
    )

  /** Converts the receiving [TypeMirror] into a [TypeLocation]. */
  fun TypeMirror.asTypeLocation(): TypeLocation =
    // TODO: go/nullness-caller-updates-lsc - Avoid dereferencing possibly null value?
    processingEnv.typeUtils.asElement(this)!!.asTypeLocation()

  /**
   * Returns true if one of the [AnnotationMirror] items on this element has the same
   * fully-qualified name as the provided [Class].
   */
  fun Element.isAnnotatedWith(cls: Class<*>): Boolean {
    return asTypeElement().annotationMirrors.any {
      it.annotationType.asElement().asTypeLocation().toString() == cls.name
    }
  }
}

/**
 * It's a bit annoying to compare two [TypeMirrors][TypeMirror]. This is a simple wrapper class that
 * makes them comparable by equals.
 */
class ComparableType(val type: TypeMirror, val processingEnv: ProcessingEnvironment) :
  TypeMirror by type {
  override fun hashCode() = type.hashCode()

  override fun equals(other: Any?) =
    when (other) {
      is ComparableType -> processingEnv.typeUtils.isSameType(this.type, other.type)
      is TypeMirror -> processingEnv.typeUtils.isSameType(this.type, other)
      else -> false
    }
}

/**
 * Create a new instance of the [ProcessingEnvHelpers] interface for the provided
 * [ProcessingEnvironment].
 *
 * To make these helpers available directly in a class that's being constructed with a
 * [ProcessingEnvironment] as a constructor parameter, you can delegate to this implementation:
 * ```
 * class MyClass(
 *   processingEnv: ProcessingEnvironment
 * ) : ProcessingEnvHelpers by processingEnvHelpers(processingEnv) {
 *   // Extension methods in ProcessingEnvironment will be available here.
 * }
 * ```
 */
fun processingEnvHelpers(processingEnv: ProcessingEnvironment): ProcessingEnvHelpers =
  object : ProcessingEnvHelpers {
    override val processingEnv = processingEnv
  }
