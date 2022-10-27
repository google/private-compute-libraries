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

package com.google.android.libraries.pcc.chronicle.codegen

/**
 * A [TypeSet] contains the result of the run of a converter frontend. It contains a single
 * top-level type, along with any supporting types needed to fully represent the fields of the type,
 * including any recursive dependencies required by nested types.
 */
data class TypeSet(val primary: Type, val additional: Set<Type> = emptySet()) : Set<Type> {

  constructor(vararg allTypes: Type) : this(allTypes.first(), allTypes.drop(1).toSet())

  override val size = additional.size + 1
  override fun contains(element: Type) = primary == element || additional.contains(element)
  override fun containsAll(elements: Collection<Type>) = elements.all { contains(it) }
  override fun isEmpty() = false
  override fun iterator(): Iterator<Type> {
    return iterator {
      yield(primary)
      additional.forEach { yield(it) }
    }
  }
}

/**
 * Intermediary representation of a POJO/Proto for use converting to a Chronicle schema.
 *
 * @param location The [TypeLocation] of the underlying Java/Kotlin type
 * @param fields The fields the type contains
 * @param oneOfs Information about mutually-exclusive field groups (from protos) conversion. The
 * `Set<Type>` returned from a frontend should contain exactly one item with this flag set.
 */
data class Type(
  val location: TypeLocation,
  val fields: List<FieldEntry>,
  val oneOfs: OneOfs = OneOfs(),
  val jvmLocation: TypeLocation = location,
  val tooling: Tooling = Tooling.UNKNOWN
) {
  val name = location.name
  val enclosingNames = location.enclosingNames
  val pkg = location.pkg

  /**
   * The library or framework within which the type was defined.
   *
   * This can be useful when generating code when it comes to knowing how to approach doing things
   * like creating a new instance of a type or returning a copy with a changed field.
   */
  enum class Tooling {
    /** The [Type] was defined by a protocol buffer descriptor. */
    PROTO,
    /** The [Type] was defined as an AutoValue abstract class. */
    AUTOVALUE,
    /** The [Type] was defined as a kotlin data class. */
    DATA_CLASS,
    /** It is either unknown or unsupported which system the [Type] was defined within. */
    UNKNOWN,
  }
}

/**
 * Information needed to locate a Java/Kotlin type.
 *
 * @param name The simple name of the type
 * @param enclosingNames a list of names of enclosing types for the type, ordered innermost-first.
 * @param pkg The package containing the type
 */
data class TypeLocation(
  val name: String,
  val enclosingNames: List<String> = emptyList(),
  val pkg: String
) {
  override fun toString(): String {
    val fullName = (enclosingNames.reversed().filter { it.isNotEmpty() } + name).joinToString(".")
    return "$pkg.$fullName"
  }
}

/**
 * Describes one field in a structure that will become a Chronicle schema.
 *
 * @property name the name to use in the generated schema
 * @property category the FieldCategory of the field
 * @property sourceName the name in the source structure, can be an arbitrary code snippet
 * @property presenceCondition the code to emit to detect the presence of this field in the object
 */
data class FieldEntry(
  val name: String,
  val category: FieldCategory,
  val sourceName: String = name,
  val presenceCondition: String = ""
)

/**
 * A structure to hold information of "oneof" protobuf fields.
 *
 * @property oneOfForField a map from a field name to the field name of its containing oneof field.
 * @property fieldsForOneOf a map from a oneof field name to a list of the field names it contains.
 */
data class OneOfs(
  val oneOfForField: Map<String, String> = emptyMap(),
  val fieldsForOneOf: Map<String, List<String>> = emptyMap()
)
