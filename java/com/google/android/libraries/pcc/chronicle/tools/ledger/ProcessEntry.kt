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

/**
 * An alternative representation of the data from a [LedgerInfo], with policies replaced by their
 * canonical names across all known build variants for the sake of being renderable to more
 * lightweight JSON.
 */
data class ProcessEntry(
  val name: String,
  val dataTypes: List<LedgerDataType>,
  val policies: Set<String>,
) {
  /** Transforms this [LedgerInfo] instance to a JSON string. */
  fun toJsonString(): String = DataHubLedger.gson.toJson(this)

  /** Transforms the [ProcessEntry] into a [LedgerInfo], given a global policy lookup. */
  fun toLedgerInfo(policyLookup: Map<String, Policy>, dtds: Set<DataTypeDescriptor>): LedgerInfo =
    LedgerInfo(
      name = name,
      dataTypes = dataTypes,
      dataTypeDescriptors = dataTypes.map { dt -> dtds.find { it.name == dt.name }!! }.toSet(),
      policies = policies.map { policyLookup[it]!! }.toSet(),
    )

  internal object TypeAdapterImpl : TypeAdapter<ProcessEntry>() {
    private const val NAME_FIELD = "name"
    private const val DATATYPES_FIELD = "dataTypes"
    private const val POLICIES_FIELD = "policies"

    override fun write(out: JsonWriter, value: ProcessEntry?) {
      if (value == null) {
        out.nullValue()
        return
      }
      out.makeObject {
        writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
        writeNamedArray(DataHubLedger.gson, DATATYPES_FIELD, value.dataTypes) { it.name }
        writeNamedArray(DataHubLedger.gson, POLICIES_FIELD, value.policies) { it }
      }
    }

    override fun read(reader: JsonReader): ProcessEntry? {
      val type = reader.peek()
      if (type == JsonToken.NULL) return null
      var name = ""
      var dataTypes: List<LedgerDataType> = emptyList()
      var policies: Set<String> = emptySet()
      reader.readObject { fieldName ->
        when (fieldName) {
          NAME_FIELD -> name = reader.nextString()
          DATATYPES_FIELD -> dataTypes = readList(DataHubLedger.gson)
          POLICIES_FIELD -> policies = readSet(DataHubLedger.gson)
          else -> throw IllegalArgumentException("Unexpected field in ProcessEntry: $fieldName")
        }
      }
      return ProcessEntry(name, dataTypes, policies)
    }
  }
}

internal fun LedgerInfo.toProcessEntry(canonicalNames: Map<Policy, String>): ProcessEntry =
  ProcessEntry(name, dataTypes, policies.map { canonicalNames[it]!! }.toSet())
