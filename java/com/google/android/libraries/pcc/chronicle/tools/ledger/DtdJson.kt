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

package com.google.android.libraries.pcc.chronicle.tools.ledger

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

object DtdJson {
  /**
   * Serializes/deserializes [DataTypeDescriptor] objects. NOTE: the [DataTypeDescriptor.cls] value
   * is not serialized, and is always deserialized to [Any.javaClass]. This is because it is not
   * useful for ledger comparison and would be error-prone when using [Class.forName] for builds
   * where the type is not included.
   */
  object DataTypeDescriptorTypeAdapter : TypeAdapter<DataTypeDescriptor>() {
    private const val NAME_FIELD = "name"
    private const val FIELDS_FIELD = "fields"
    private const val INNER_TYPES_FIELD = "innerTypes"

    override fun write(out: JsonWriter, value: DataTypeDescriptor) {
      out.makeObject {
        writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
        writeNamedMap(DataHubLedger.gson, FIELDS_FIELD, value.fields)
        writeNamedArray(DataHubLedger.gson, INNER_TYPES_FIELD, value.innerTypes) { it.name }
      }
    }

    override fun read(reader: JsonReader): DataTypeDescriptor {
      var name = ""
      var fields = mapOf<String, FieldType>()
      var innerTypes = setOf<DataTypeDescriptor>()
      reader.readObject {
        when (it) {
          NAME_FIELD -> name = nextString()
          FIELDS_FIELD -> fields = readMap(DataHubLedger.gson)
          INNER_TYPES_FIELD -> innerTypes = readSet(DataHubLedger.gson)
          else ->
            throw IllegalArgumentException("Unexpected key in DataTypeDescriptor: \"$it\" at $path")
        }
      }
      return DataTypeDescriptor(name, fields, innerTypes, Any::class)
    }
  }

  /** Serializes/deserializes [FieldType] instances to JSON. */
  object FieldTypeTypeAdapter : TypeAdapter<FieldType>() {
    private const val ARRAY = "Array"
    private const val BOOLEAN = "Boolean"
    private const val BYTE = "Byte"
    private const val BYTE_ARRAY = "ByteArray"
    private const val CHAR = "Char"
    private const val DOUBLE = "Double"
    private const val DURATION = "Duration"
    private const val ENUM = "Enum"
    private const val FLOAT = "Float"
    private const val INSTANT = "Instant"
    private const val INTEGER = "Int"
    private const val LIST = "List"
    private const val LONG = "Long"
    private const val NESTED = "Nested"
    private const val NULLABLE = "Nullable"
    private const val OPAQUE = "Opaque"
    private const val REFERENCE = "Reference"
    private const val SHORT = "Short"
    private const val STRING = "String"
    private const val TUPLE = "Tuple"

    private const val NAME_FIELD = "name"
    private const val TYPE_FIELD = "type"
    private const val TYPE_PARAMETER_FIELD = "parameter"
    private const val TYPE_PARAMETERS_FIELD = "parameters"
    private const val VALUES_FIELD = "values"

    private val STRING_TO_FIELDTYPE =
      mapOf(
        BOOLEAN to FieldType.Boolean,
        BYTE to FieldType.Byte,
        BYTE_ARRAY to FieldType.ByteArray,
        CHAR to FieldType.Char,
        DOUBLE to FieldType.Double,
        DURATION to FieldType.Duration,
        FLOAT to FieldType.Float,
        INSTANT to FieldType.Instant,
        INTEGER to FieldType.Integer,
        LONG to FieldType.Long,
        SHORT to FieldType.Short,
        STRING to FieldType.String,
      )

    override fun write(out: JsonWriter, value: FieldType) {
      when (value) {
        FieldType.Boolean -> out.value(BOOLEAN)
        FieldType.Byte -> out.value(BYTE)
        FieldType.ByteArray -> out.value(BYTE_ARRAY)
        FieldType.Char -> out.value(CHAR)
        FieldType.Double -> out.value(DOUBLE)
        FieldType.Duration -> out.value(DURATION)
        FieldType.Float -> out.value(FLOAT)
        FieldType.Instant -> out.value(INSTANT)
        FieldType.Integer -> out.value(INTEGER)
        FieldType.Long -> out.value(LONG)
        FieldType.Short -> out.value(SHORT)
        FieldType.String -> out.value(STRING)
        is FieldType.Array -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, ARRAY)
            out.writeNamedField(DataHubLedger.gson, TYPE_PARAMETER_FIELD, value.itemFieldType)
          }
        }
        is FieldType.Enum -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, ENUM)
            out.writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
            out.writeNamedStringArray(DataHubLedger.gson, VALUES_FIELD, value.possibleValues)
          }
        }
        is FieldType.List -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, LIST)
            out.writeNamedField(DataHubLedger.gson, TYPE_PARAMETER_FIELD, value.itemFieldType)
          }
        }
        is FieldType.Nested -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, NESTED)
            out.writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
          }
        }
        is FieldType.Nullable -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, NULLABLE)
            out.writeNamedField(DataHubLedger.gson, TYPE_PARAMETER_FIELD, value.itemFieldType)
          }
        }
        is FieldType.Opaque -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, OPAQUE)
            out.writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
          }
        }
        is FieldType.Reference -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, REFERENCE)
            out.writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
          }
        }
        is FieldType.Tuple -> {
          out.makeObject {
            out.writeNamedField(DataHubLedger.gson, TYPE_FIELD, TUPLE)
            out.writeNamedArray(DataHubLedger.gson, TYPE_PARAMETERS_FIELD, value.itemFieldTypes) {
              it.javaClass.toGenericString()
            }
          }
        }
      }
    }

    override fun read(reader: JsonReader): FieldType {
      if (reader.peek() == JsonToken.STRING) {
        val typeName = reader.nextString()
        return requireNotNull(STRING_TO_FIELDTYPE[typeName]) {
          "Unrecognized field type: $typeName"
        }
      }

      var type = ""
      var name: String? = null
      var typeParameter: FieldType? = null
      var typeParameters = emptyList<FieldType>()
      var possibleValues = emptyList<String>()

      reader.readObject {
        when (it) {
          NAME_FIELD -> name = nextString()
          TYPE_FIELD -> type = nextString()
          TYPE_PARAMETER_FIELD -> typeParameter = read(reader)
          TYPE_PARAMETERS_FIELD -> typeParameters = readList(DataHubLedger.gson)
          VALUES_FIELD -> possibleValues = readStringList()
          else -> throw IllegalArgumentException("Unexpected key FieldType: \"$it\" at $path")
        }
      }

      return when (type) {
        ARRAY ->
          FieldType.Array(requireNotNull(typeParameter) { "Expected type parameter for Array" })
        ENUM -> FieldType.Enum(requireNotNull(name) { "Expected name for Enum" }, possibleValues)
        LIST -> FieldType.List(requireNotNull(typeParameter) { "Expected type parameter for List" })
        NESTED -> FieldType.Nested(requireNotNull(name) { "Expected name for Nested" })
        NULLABLE ->
          FieldType.Nullable(
            requireNotNull(typeParameter) { "Expected type parameter for Nullable" }
          )
        OPAQUE -> FieldType.Opaque(requireNotNull(name) { "Expected name for Opaque" })
        REFERENCE -> FieldType.Reference(requireNotNull(name) { "Expected name for Reference" })
        TUPLE -> FieldType.Tuple(typeParameters)
        else -> throw IllegalArgumentException("Unexpected FieldType type: \"$type\"")
      }
    }
  }
}
