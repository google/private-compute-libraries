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

package com.google.android.libraries.pcc.chronicle.api.optics

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor

/**
 * A representation of the path walked by a composed set of optics such as [Lens].
 *
 * When composing optics, the result should also contain a composition of their [OpticalAccessPaths]
 * [OpticalAccessPath], representing the full traversal.
 */
data class OpticalAccessPath(
  /**
   * Name of the root object type.
   *
   * Typically, this is a [DataTypeDescriptor.name].
   */
  val dataTypeName: String,
  /** List of field names walked by the composition of optics. */
  val selectors: List<String>
) {
  constructor(
    rootTypeName: String,
    vararg selectors: String
  ) : this(rootTypeName, selectors.asList())

  constructor(dtd: DataTypeDescriptor, vararg selectors: String) : this(dtd, selectors.asList())

  constructor(dtd: DataTypeDescriptor, selectors: List<String>) : this(dtd.name, selectors)

  /**
   * Composes two [OpticalAccessPath] objects into one.
   *
   * The end result is an [OpticalAccessPath] with the receiver/LHS's [dataTypeName] and a
   * concatenation of the [other]'s [selectors] onto the receiver/LHS's.
   *
   * Example:
   *
   * ```kotlin
   * val cityMayor = OpticalAccessPath("City", "mayor")
   * val personPetName = OpticalAccessPath("Person", "pet", "name")
   *
   * val cityMayorPetName = cityMayor compose personPetName
   *
   * cityMayorPetName.toString() // "City::mayor.pet.name"
   * ```
   */
  infix fun compose(other: OpticalAccessPath): OpticalAccessPath =
    OpticalAccessPath(dataTypeName, selectors + other.selectors)

  override fun toString(): String {
    if (selectors.isEmpty()) return dataTypeName
    return "$dataTypeName::${selectors.joinToString(".")}"
  }
}
