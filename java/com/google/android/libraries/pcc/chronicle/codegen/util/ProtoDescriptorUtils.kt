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

import com.google.android.libraries.pcc.chronicle.codegen.OneOfs
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.google.protobuf.Descriptors

/**
 * A helper to get a list of strings representing an enclosing types for a class.
 *
 * The enclosing types will be returned in list ordered innermost-first.
 *
 * For example, `java.util.Map.Entry` will return ["Map"].
 * `some.package.OuterMost.Middle.Inner.ClassName` will return ["Inner", "Middle", "OuterMost"]
 */
fun Descriptors.Descriptor.enclosingNames(): List<String> =
  generateSequence(containingType) { it.containingType }.map { it.name }.toList()

/**
 * A helper to get a list of strings representing an enclosing types for an enum.
 *
 * The enclosing types will be returned in list ordered innermost-first.
 *
 * For example, `java.util.Map.Entry` will return ["Map"].
 * `some.package.OuterMost.Middle.Inner.ClassName` will return ["Inner", "Middle", "OuterMost"]
 */
fun Descriptors.EnumDescriptor.enclosingNames(): List<String> =
  generateSequence(containingType) { it.containingType }.map { it.name }.toList()

/** Generate the [TypeLocation] for the Java class that will be generated for this proto. */
fun Descriptors.Descriptor.typeLocation(useJavaPackage: Boolean): TypeLocation =
  TypeLocation(
    name = name,
    enclosingNames =
      enclosingNames() +
        if (!file.options.javaMultipleFiles) listOf(file.options.javaOuterClassname)
        else emptyList(),
    pkg = if (useJavaPackage) file.options.javaPackage else file.`package`,
  )

/** Generate the [TypeLocation] for the Java enum class that will be generated for this proto. */
fun Descriptors.EnumDescriptor.typeLocation(useJavaPackage: Boolean): TypeLocation =
  TypeLocation(
    name = name,
    enclosingNames =
      enclosingNames() +
        if (!file.options.javaMultipleFiles) listOf(file.options.javaOuterClassname)
        else emptyList(),
    pkg = if (useJavaPackage) file.options.javaPackage else file.`package`,
  )

/** Return the field name to use when accessing it from generated code. */
fun Descriptors.FieldDescriptor.toProtoFieldName(bytesAsStrings: Boolean = true) =
  when {
    isMapField -> "${name.toChronicleFieldName()}Map"
    isRepeated && !isMapField -> "${name.toChronicleFieldName()}List"
    type == Descriptors.FieldDescriptor.Type.BYTES ->
      "${name.toChronicleFieldName()}${if (bytesAsStrings) ".toStringUtf8()" else ""}"
    else -> name.toChronicleFieldName()
  }

/** Return the name of the presenceConditional for one-of fields. */
fun Descriptors.FieldDescriptor.presenceConditional(): String {
  val capitalizedName = name.toChronicleFieldName().capitalize()
  return if (containingOneof != null) "has$capitalizedName" else ""
}

/**
 * Given a message's [OneofDescriptor], extract details necessary to represent that one-of for code
 * generation backends.
 */
fun List<Descriptors.OneofDescriptor>.toTypeOneOfs(): OneOfs {
  // Make a map of the oneof field name -> list of fields it contains
  val fieldsForOneOf =
    associateBy(
      keySelector = { it.name.toChronicleFieldName() },
      valueTransform = { it.fields.map { field -> field.name.toChronicleFieldName() } },
    )

  // Map a map of all of the oneof contained fields to their containing oneof name.
  val oneOfForField =
    flatMap { oneof ->
        // Turn [f1, f2, f3] into [f1: n, f2: n, f3: n]
        // Where f# is field# name, and n is the oneof field name
        oneof.fields.map { it.name.toChronicleFieldName() to oneof.name.toChronicleFieldName() }
      }
      .toMap()

  return OneOfs(oneOfForField, fieldsForOneOf)
}
