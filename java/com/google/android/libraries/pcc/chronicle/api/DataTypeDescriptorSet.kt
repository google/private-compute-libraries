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

package com.google.android.libraries.pcc.chronicle.api

import kotlin.reflect.KClass

/** Collection of [DataTypeDescriptors][DataTypeDescriptor]. */
interface DataTypeDescriptorSet {
  /**
   * Returns the [DataTypeDescriptor] with the given [name], throws [IllegalArgumentException] if
   * none is found.
   */
  operator fun get(name: String): DataTypeDescriptor =
    requireNotNull(getOrNull(name)) { "Could not find a DataTypeDescriptor for name: \"$name\"" }

  /** Returns the [DataTypeDescriptor] with the given [name], or `null` if none is found. */
  fun getOrNull(name: String): DataTypeDescriptor?

  /**
   * Returns the [FieldType] for a field within the [DataTypeDescriptor] with the given [dtdName],
   * accessed via the provided [accessPath].
   */
  fun findFieldTypeOrThrow(dtdName: String, accessPath: List<String>): FieldType =
    findFieldTypeOrThrow(get(dtdName), accessPath)

  /**
   * Returns the [FieldType] for a field within the given [DataTypeDescriptor] accessed via the
   * provided [accessPath].
   */
  fun findFieldTypeOrThrow(dtd: DataTypeDescriptor, accessPath: List<String>): FieldType

  /**
   * Returns the [DataTypeDescriptor] associated with the provided [FieldType].
   *
   * If the [FieldType] is a primitive or opaque value (or is yet otherwise unsupported), `null` is
   * returned.
   */
  fun findDataTypeDescriptor(fieldType: FieldType): DataTypeDescriptor?

  /** Returns the [DataTypeDescriptor] for the given [KClass], or null if one is not known. */
  fun findDataTypeDescriptor(cls: KClass<*>): DataTypeDescriptor?

  /**
   * Returns the [Class] associated with the provided [FieldType].
   *
   * If the [FieldType] represents a primitive, array, or list a constant type is returned.
   *
   * If it is a [FieldType.Reference] or [FieldType.Nested] type, its name is used to find the
   * [DataTypeDescriptor.cls] value of the [DataTypeDescriptor] with that name.
   *
   * If it's [FieldType.Opaque], its name is used in conjunction with [Class.forName]
   * - which has a possibility of throwing a [ClassNotFoundException] if the name is not present in
   * the classloader.
   *
   * If it's [FieldType.Nullable], the value of [findClass] for the non-nullable `itemFieldType`
   * value is returned.
   *
   * [IllegalArgumentException] is thrown for [FieldTypes][FieldType] not yet supported for
   * Cantrips.
   */
  fun fieldTypeAsClass(fieldType: FieldType): Class<*>

  /**
   * Returns the [Class] associated with the [FieldType] located within the given
   * [DataTypeDescriptor] accessed via the provided [accessPath].
   */
  fun findFieldTypeAsClass(dtd: DataTypeDescriptor, accessPath: List<String>): Class<*> =
    fieldTypeAsClass(findFieldTypeOrThrow(dtd, accessPath))

  /** Returns a representation as a regular Set. */
  fun toSet(): Set<DataTypeDescriptor>
}
