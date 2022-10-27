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

package com.google.android.libraries.pcc.chronicle.codegen.typeconversion

import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.time.Duration
import java.time.Instant

/** Return a string representation of the kotlin type for the [FieldCategory]. */
fun FieldCategory.kotlinType(): TypeName {
  return when (this) {
    is FieldCategory.ByteArrayValue -> ByteArray::class.asTypeName()
    is FieldCategory.ShortValue -> Short::class.asTypeName()
    is FieldCategory.CharValue -> Char::class.asTypeName()
    is FieldCategory.FloatValue -> Float::class.asTypeName()
    is FieldCategory.StringValue -> String::class.asTypeName()
    is FieldCategory.BooleanValue -> Boolean::class.asTypeName()
    is FieldCategory.ByteValue -> Byte::class.asTypeName()
    is FieldCategory.DoubleValue -> Double::class.asTypeName()
    is FieldCategory.IntValue -> Int::class.asTypeName()
    is FieldCategory.LongValue -> Long::class.asTypeName()
    is FieldCategory.InstantValue -> Instant::class.asTypeName()
    is FieldCategory.DurationValue -> Duration::class.asTypeName()
    is FieldCategory.EnumValue -> jvmLocation.asClassName()
    is FieldCategory.ListValue -> List::class.asTypeName().parameterizedBy(listType.kotlinType())
    is FieldCategory.SetValue -> Set::class.asTypeName().parameterizedBy(setType.kotlinType())
    is FieldCategory.MapValue ->
      Map::class.asTypeName().parameterizedBy(keyType.kotlinType(), valueType.kotlinType())
    is FieldCategory.NestedTypeValue -> jvmLocation.asClassName()
    is FieldCategory.NullableValue ->
      innerType.kotlinType().copy(nullable = true, annotations = emptyList())
    is FieldCategory.ForeignReference ->
      throw IllegalArgumentException("Foreign references do not have a Kotlin type.")
    is FieldCategory.OpaqueTypeValue -> location.asClassName()
    is FieldCategory.TupleValue ->
      when (types.size) {
        2 ->
          Pair::class
            .asTypeName()
            .parameterizedBy(types.get(0).kotlinType(), types.get(1).kotlinType())
        3 ->
          Triple::class
            .asTypeName()
            .parameterizedBy(
              types.get(0).kotlinType(),
              types.get(1).kotlinType(),
              types.get(2).kotlinType()
            )
        else ->
          throw IllegalArgumentException(
            "Generic tuples do not have a Kotlin type, only pairs & triples"
          )
      }
  }
}

/**
 * Generate an identifier-friendly name based on the Arcs type that the [FieldCategory] will
 * generate.
 *
 * Right now, this is used only for generating the map wrapper types.
 *
 * Collection types may result in somewhat unwieldly names like:
 *
 * Map<String, List<List<Int>> -> StringToListListIntMapEntry
 *
 * But these types should only be used internally.
 */
fun FieldCategory.identifierFriendlyArcsType(): String {
  return when (this) {
    is FieldCategory.StringValue -> "Text"
    is FieldCategory.CharValue -> "Char"
    is FieldCategory.ByteValue -> "Byte"
    is FieldCategory.ShortValue -> "Short"
    is FieldCategory.IntValue -> "Int"
    is FieldCategory.LongValue -> "Long"
    is FieldCategory.FloatValue -> "Float"
    is FieldCategory.DoubleValue -> "Double"
    is FieldCategory.BooleanValue -> "Boolean"
    is FieldCategory.InstantValue -> "Instant"
    is FieldCategory.DurationValue -> "Duration"
    is FieldCategory.EnumValue -> location.name
    is FieldCategory.NestedTypeValue -> location.name
    is FieldCategory.ForeignReference -> schemaName
    is FieldCategory.SetValue -> "Set${setType.identifierFriendlyArcsType()}"
    is FieldCategory.ListValue -> "List${listType.identifierFriendlyArcsType()}"
    is FieldCategory.MapValue ->
      "${keyType.identifierFriendlyArcsType()}To${valueType.identifierFriendlyArcsType()}MapEntry"
    is FieldCategory.NullableValue -> innerType.identifierFriendlyArcsType()
    is FieldCategory.OpaqueTypeValue -> location.name
    is FieldCategory.TupleValue ->
      "Tuple" + types.map { it.identifierFriendlyArcsType() }.joinToString("And")
    is FieldCategory.ByteArrayValue ->
      throw IllegalArgumentException("byte arrays are not supported by arcs")
  }
}

/** Generate the KotlinPoet [ClassName] for a [TypeLocation]. */
fun TypeLocation.asClassName() =
  if (enclosingNames.isEmpty()) {
    ClassName(pkg, name)
  } else {
    enclosingNames.reversed().let { ClassName(pkg, it[0], *(it.drop(1) + name).toTypedArray()) }
  }

/** Get all of the map types needed for the type tree descending from the provided [field]. */
fun FieldCategory.collectMapTypes(): Set<FieldCategory.MapValue> {
  return when (this) {
    is FieldCategory.MapValue -> {
      keyType.collectMapTypes() + valueType.collectMapTypes() + this
    }
    is FieldCategory.ListValue -> listType.collectMapTypes()
    is FieldCategory.SetValue -> setType.collectMapTypes()
    else -> emptySet()
  }
}

/** Get all map types needed for all fields in the provided [type] and their descendants. */
fun Type.collectMapTypes(): Set<FieldCategory.MapValue> =
  fields.flatMapTo(mutableSetOf()) { it.category.collectMapTypes() }

/** Returns the type name for the wrapper type of a map entry represented in Arcs. */
fun FieldCategory.MapValue.typeName(packageName: String, particleName: String) =
  ClassName(packageName, particleName, identifierFriendlyArcsType())
