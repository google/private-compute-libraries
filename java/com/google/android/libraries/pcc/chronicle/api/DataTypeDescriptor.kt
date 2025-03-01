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

/**
 * Builds a [DataTypeDescriptor] from the given [name] and using the supplied [block].
 *
 * Example:
 * ```kotlin
 *  val myType = dataTypeDescriptor("Person", Person::class) {
 *   "name" to FieldType.String
 *   "age" to FieldType.Short
 *   "father" to FieldType.Reference("Person")
 *   "pet" to dataTypeDescriptor("Dog", Dog::class) { }
 * }
 * ```
 */
@ChronicleDsl
fun dataTypeDescriptor(
  name: String,
  cls: KClass<*>,
  block: DataTypeDescriptor.Builder.() -> Unit = {},
): DataTypeDescriptor {
  return DataTypeDescriptor.Builder(name, cls).apply(block).build()
}

/**
 * Defines a Chronicle-managed data type. A [DataTypeDescriptor] allows Chronicle to perform
 * field-by-field data flow analysis for policy enforcement.
 */
data class DataTypeDescriptor(
  val name: String,
  val fields: Map<String, FieldType>,
  val innerTypes: Set<DataTypeDescriptor> = emptySet(),
  val cls: KClass<*>,
) {
  class Builder(val name: String, val cls: KClass<*>) {
    private val fields = mutableMapOf<String, FieldType>()
    private val innerTypes = mutableSetOf<DataTypeDescriptor>()

    /** Adds a field to the [DataTypeDescriptor] being built. */
    infix fun String.to(type: FieldType) {
      fields[this] = type
    }

    /**
     * Constructs a nested [DataTypeDescriptor] and returns it as a [FieldType.Nested] which can be
     * used along with [to] to map it to a field name.
     */
    @ChronicleDsl
    fun dataTypeDescriptor(
      name: String,
      cls: KClass<*>,
      block: Builder.() -> Unit = {},
    ): FieldType {
      val dtd = Builder(name, cls).apply(block).build()
      require(innerTypes.none { it.name == dtd.name && it != dtd }) {
        "Duplicate inner type declared with name: ${dtd.name} and different contents"
      }
      innerTypes.add(dtd)
      return FieldType.Nested(dtd.name)
    }

    /** Builds a [DataTypeDescriptor] from the current state of the [Builder]. */
    fun build(): DataTypeDescriptor = DataTypeDescriptor(name, fields, innerTypes, cls)
  }
}

/** Sealed class of options for field values in [DataTypeDescriptor]s. */
sealed class FieldType {
  /** Represents a field value as a [Boolean] primitive. */
  object Boolean : FieldType()

  /** Represents a field value as a [Byte] primitive. */
  object Byte : FieldType()

  /** Represents a field value as a [ByteArray] primitive. */
  object ByteArray : FieldType()

  /** Represents a field value as a [Short] primitive. */
  object Short : FieldType()

  /** Represents a field value as a [Integer] primitive. */
  object Integer : FieldType()

  /** Represents a field value as a [Long] primitive. */
  object Long : FieldType()

  /** Represents a field value as a [Float] primitive. */
  object Float : FieldType()

  /** Represents a field value as a [Double] primitive. */
  object Double : FieldType()

  /** Represents a field value as a [String] primitive. */
  object String : FieldType()

  /** Represents a field value as a [Char] primitive. */
  object Char : FieldType()

  /** Represents a field value as a [java.time.Instant]. */
  object Instant : FieldType()

  /** Represents a field value as a [java.time.Duration]. */
  object Duration : FieldType()

  /** Represents a field value as an [Enum] object. */
  data class Enum(
    val name: kotlin.String,
    val possibleValues: kotlin.collections.List<kotlin.String>,
  ) : FieldType()

  /** Represents a field value as an array of [itemFieldType] -typed objects. */
  data class Array(val itemFieldType: FieldType) : FieldType()

  /** Represents a field value as a list of [itemFieldType] -typed objects. */
  data class List(val itemFieldType: FieldType) : FieldType()

  /**
   * Represents a field value as a non-primitive value, another [DataTypeDescriptor]-defined type.
   */
  data class Nested(val name: kotlin.String) : FieldType()

  /**
   * Represents a field value as a nullable potentially containing a [itemFieldType]-typed object.
   */
  data class Nullable(val itemFieldType: FieldType) : FieldType()

  /**
   * Represents a field value as a reference to a non-primitive value, another [DataTypeDescriptor]
   * -defined type.
   */
  data class Reference(val name: kotlin.String) : FieldType()

  /**
   * Represents a field value as a given opaque type, belonging to a small, predetermined set of
   * possible opaque types. Only the fully-qualified name is used to define the type, with no
   * [DataTypeDescriptor] definition.
   */
  data class Opaque(val name: kotlin.String) : FieldType()

  /** Represents a field value as a tuple with [itemFieldTypes] -typed objects. */
  data class Tuple(val itemFieldTypes: kotlin.collections.List<FieldType>) : FieldType()
}
