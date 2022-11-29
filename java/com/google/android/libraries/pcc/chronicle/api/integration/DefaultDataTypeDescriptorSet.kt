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

package com.google.android.libraries.pcc.chronicle.api.integration

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.FieldType
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

/** Default implementation of [DataTypeDescriptorSet]. */
class DefaultDataTypeDescriptorSet : DataTypeDescriptorSet {

  private val dtds: Set<DataTypeDescriptor>
  private val dtdsByName: Map<String, DataTypeDescriptor>
  private val dtdsByKClass: Map<KClass<*>, DataTypeDescriptor>

  constructor(dtds: Set<DataTypeDescriptor>) {
    val dtdsByName: MutableMap<String, DataTypeDescriptor> = mutableMapOf()

    dtds
      .flatMap { collectNestedDataTypeDescriptors(it) }
      .map {
        // Ensure that DTDs of the same name are not overwriting each other.
        // For `equal` DTDs, duplicate names are allowed, since no information will be changed and
        // this is needed for nested DTDs.
        require(!dtdsByName.containsKey(it.name) || dtdsByName[it.name]?.equals(it) == true) {
          "DataTypeDescriptor ${it.name} must be unique."
        }
        dtdsByName[it.name] = it
      }

    this.dtds = dtds
    this.dtdsByName = dtdsByName
    this.dtdsByKClass = dtdsByName.values.associateBy { it.cls }
  }

  private fun collectNestedDataTypeDescriptors(dtd: DataTypeDescriptor): List<DataTypeDescriptor> {
    return dtd.innerTypes.fold(listOf(dtd)) { acc, t -> acc + collectNestedDataTypeDescriptors(t) }
  }

  override fun getOrNull(name: String): DataTypeDescriptor? = dtdsByName[name]

  override tailrec fun findFieldTypeOrThrow(
    dtd: DataTypeDescriptor,
    accessPath: List<String>,
  ): FieldType {
    require(accessPath.isNotEmpty()) { "Cannot find field type for empty access path." }

    val field = accessPath[0]
    val fieldType =
      requireNotNull(dtd.fields[field]) { "Field \"$field\" not found in ${dtd.name}" }

    if (accessPath.size == 1) return fieldType

    val nextDtd = requireNotNull(findDataTypeDescriptor(fieldType))
    return findFieldTypeOrThrow(nextDtd, accessPath.drop(1))
  }

  override fun findDataTypeDescriptor(fieldType: FieldType): DataTypeDescriptor? {
    return when (fieldType) {
      is FieldType.Array -> findDataTypeDescriptor(fieldType.itemFieldType)
      is FieldType.List -> findDataTypeDescriptor(fieldType.itemFieldType)
      is FieldType.Nullable -> findDataTypeDescriptor(fieldType.itemFieldType)
      is FieldType.Nested -> getOrNull(fieldType.name)
      is FieldType.Reference -> getOrNull(fieldType.name)
      FieldType.Boolean,
      FieldType.Byte,
      FieldType.ByteArray,
      FieldType.Char,
      FieldType.Double,
      FieldType.Duration,
      FieldType.Float,
      FieldType.Instant,
      FieldType.Integer,
      FieldType.Long,
      FieldType.Short,
      FieldType.String,
      is FieldType.Enum,
      is FieldType.Opaque,
      // We would need to know which item within the tuple we are interested in.
      is FieldType.Tuple -> null
    }
  }

  override fun findDataTypeDescriptor(cls: KClass<*>): DataTypeDescriptor? = dtdsByKClass[cls]

  override fun fieldTypeAsClass(fieldType: FieldType): Class<*> {
    return when (fieldType) {
      FieldType.Boolean -> Boolean::class.javaObjectType
      FieldType.Byte -> Byte::class.javaObjectType
      FieldType.ByteArray -> ByteArray::class.java
      FieldType.Char -> Char::class.javaObjectType
      FieldType.Double -> Double::class.javaObjectType
      FieldType.Duration -> Duration::class.java
      FieldType.Float -> Float::class.javaObjectType
      FieldType.Instant -> Instant::class.java
      FieldType.Integer -> Int::class.javaObjectType
      FieldType.Long -> Long::class.javaObjectType
      FieldType.Short -> Short::class.javaObjectType
      FieldType.String -> String::class.java
      is FieldType.Enum -> Enum::class.java
      is FieldType.Array -> Array::class.java
      is FieldType.List -> List::class.java
      is FieldType.Reference -> this[fieldType.name].cls.java
      is FieldType.Nested -> this[fieldType.name].cls.java
      is FieldType.Opaque -> Class.forName(fieldType.name)
      is FieldType.Nullable -> fieldTypeAsClass(fieldType.itemFieldType)
      is FieldType.Tuple ->
        // TODO(b/208662121): Tuples could be android.util.Pair or kotlin.Pair (or similar)
        throw IllegalArgumentException("Tuple is too ambiguous to return a field type.")
    }
  }

  override fun toSet(): Set<DataTypeDescriptor> = dtds
}
