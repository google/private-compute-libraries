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

/** This class represents the set of values that fields can take inside a Type object. */
sealed class FieldCategory {
  object StringValue : FieldCategory()
  object ByteValue : FieldCategory()
  object ByteArrayValue : FieldCategory()
  object ShortValue : FieldCategory()
  object CharValue : FieldCategory()
  object IntValue : FieldCategory()
  object LongValue : FieldCategory()
  object FloatValue : FieldCategory()
  object DoubleValue : FieldCategory()
  object BooleanValue : FieldCategory()
  object InstantValue : FieldCategory()
  object DurationValue : FieldCategory()

  data class EnumValue(
    val location: TypeLocation,
    val possibleValues: List<String>,
    val jvmLocation: TypeLocation = location
  ) : FieldCategory()
  data class ListValue(val location: TypeLocation, val listType: FieldCategory) : FieldCategory()
  data class SetValue(val location: TypeLocation, val setType: FieldCategory) : FieldCategory()
  data class MapValue(
    val location: TypeLocation,
    val keyType: FieldCategory,
    val valueType: FieldCategory
  ) : FieldCategory()
  data class NullableValue(val innerType: FieldCategory) : FieldCategory()
  data class NestedTypeValue(val location: TypeLocation, val jvmLocation: TypeLocation = location) :
    FieldCategory()
  data class ForeignReference(val schemaName: String, val hard: Boolean) : FieldCategory()
  data class OpaqueTypeValue(val location: TypeLocation) : FieldCategory()
  data class TupleValue(val types: List<FieldCategory>) : FieldCategory()
}
