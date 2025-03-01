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

package com.google.android.libraries.pcc.chronicle.util

/**
 * A [MutableTypedMap] is a mutable, type-safe map that allows for heterogeneously typed values.
 *
 * This map is keyed using implementations of the [Key] interface. A [Key] implementation will
 * specify the class type of a corresponding value to be stored in the map.
 *
 * Example:
 * ```kotlin
 * object NameKey : Key<String>
 * object IdKey : Key<Int>
 *
 * map[NameKey] = "First Last"
 * map[IdKey] = 123
 * ```
 */
class MutableTypedMap(val map: MutableMap<Key<Any>, Any> = mutableMapOf()) {

  /** Set a value of type T for the given key */
  operator fun <T : Any> set(key: Key<T>, value: T) {
    @Suppress("UNCHECKED_CAST")
    map[key as Key<Any>] = value
  }

  /** Get object of type T or null if not present */
  operator fun <T> get(key: Key<T>): T? {
    @Suppress("UNCHECKED_CAST")
    return map[key as Key<Any>] as T
  }

  /** Get immutable version of this [MutableTypedMap] */
  fun toTypedMap() = TypedMap(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MutableTypedMap) return false
    if (map != other.map) return false
    return true
  }

  override fun hashCode(): Int {
    return map.hashCode()
  }
}

/**
 * Keying structure for the [TypedMap] that can be associated with a value of type T, where T is a
 * basic type (https://kotlinlang.org/docs/basic-types.html)
 *
 * Non-basic types are not recommended, since deep copies are currently not supported.
 *
 * Example usage of a [Key] for a String value:
 * ```kotlin
 * object Name : Key<String>
 *
 * // Set Name
 * mutableTypedMap[Name] = "First Last"
 * typedMap = TypedMap(mutableTypedMap)
 *
 * // Get Name
 * val myName = typedMap[Name]
 * ```
 */
interface Key<T>

/**
 * A [TypedMap] is an immutable, type-safe map that allows for heterogeneously typed values. It
 * copies in the current state of a provided [MutableTypedMap].
 */
class TypedMap(mutableTypedMap: MutableTypedMap = MutableTypedMap()) {
  private val map: MutableTypedMap = MutableTypedMap(mutableTypedMap.map.toMutableMap())

  /** Get object of type T or null if not present */
  operator fun <T> get(key: Key<T>): T? = map[key]

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TypedMap) return false
    if (map != other.map) return false
    return true
  }

  override fun hashCode(): Int {
    return map.hashCode()
  }
}
