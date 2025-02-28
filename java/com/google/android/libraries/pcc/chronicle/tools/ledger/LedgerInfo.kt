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
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** Represents a collection of available data types and policies in an application/build variant. */
data class LedgerInfo(
  val name: String,
  val dataTypes: List<LedgerDataType>,
  val dataTypeDescriptors: Set<DataTypeDescriptor>,
  val policies: Set<Policy>,
) {
  /** Transforms this [LedgerInfo] instance to a JSON string. */
  fun toJsonString(): String = DataHubLedger.gson.toJson(this)

  internal object TypeAdapterImpl : TypeAdapter<LedgerInfo>() {
    private const val NAME_FIELD = "name"
    private const val DATATYPES_FIELD = "dataTypes"
    private const val DTDS_FIELD = "dtds"
    private const val POLICIES_FIELD = "policies"

    override fun write(out: JsonWriter, value: LedgerInfo?) {
      if (value == null) {
        out.nullValue()
        return
      }
      out.makeObject {
        writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
        writeNamedArray(DataHubLedger.gson, DATATYPES_FIELD, value.dataTypes) { it.name }
        writeNamedArray(DataHubLedger.gson, DTDS_FIELD, value.dataTypeDescriptors) { it.name }
        writeNamedArray(DataHubLedger.gson, POLICIES_FIELD, value.policies) { it.name }
      }
    }

    override fun read(reader: JsonReader): LedgerInfo? {
      val type = reader.peek()
      if (type == JsonToken.NULL) return null
      var name = ""
      var dataTypes = emptyList<LedgerDataType>()
      var dtds = emptySet<DataTypeDescriptor>()
      var policies = emptySet<Policy>()
      reader.readObject { fieldName ->
        when (fieldName) {
          NAME_FIELD -> name = reader.nextString()
          DATATYPES_FIELD -> dataTypes = readList(DataHubLedger.gson)
          DTDS_FIELD -> dtds = readSet(DataHubLedger.gson)
          POLICIES_FIELD -> policies = readSet(DataHubLedger.gson)
          else -> throw IllegalArgumentException("Unexpected key in LedgerInfo: $fieldName")
        }
      }
      return LedgerInfo(name, dataTypes, dtds, policies)
    }
  }
}
