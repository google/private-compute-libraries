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

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Represents an individual data type available to a build containing Chronicle, its connections,
 * and associated policies.
 */
data class LedgerDataType
internal constructor(
  val name: String,
  val connections: List<String>,
  val servers: List<RemoteServerInfo>,
  val policies: List<String>,
) {
  internal object TypeAdapterImpl : TypeAdapter<LedgerDataType>() {
    private const val NAME_FIELD = "name"
    private const val CONNECTIONS_FIELD = "connections"
    private const val SERVERS_FIELD = "servers"
    private const val POLICIES_FIELD = "policies"

    override fun write(out: JsonWriter, value: LedgerDataType?) {
      if (value == null) {
        out.nullValue()
        return
      }
      out.makeObject {
        writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
        writeNamedStringArray(DataHubLedger.gson, CONNECTIONS_FIELD, value.connections, true)
        writeNamedArray(DataHubLedger.gson, SERVERS_FIELD, value.servers) { it.name }
        writeNamedStringArray(DataHubLedger.gson, POLICIES_FIELD, value.policies, true)
      }
    }

    override fun read(reader: JsonReader): LedgerDataType? {
      val type = reader.peek()
      if (type == JsonToken.NULL) return null

      var name = ""
      var connections = emptyList<String>()
      var servers = emptyList<RemoteServerInfo>()
      var policies = emptyList<String>()

      reader.readObject { fieldName ->
        when (fieldName) {
          NAME_FIELD -> name = reader.nextString()
          CONNECTIONS_FIELD -> connections = reader.readStringList()
          SERVERS_FIELD -> servers = reader.readList(DataHubLedger.gson)
          POLICIES_FIELD -> policies = reader.readStringList()
        }
      }
      return LedgerDataType(name, connections, servers, policies)
    }
  }
}
