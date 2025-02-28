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

import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteComputeServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/** Represents a [RemoteServer] instance for the purpose of tracking via the Chronicle ledger. */
data class RemoteServerInfo(
  val name: String,
  val dtdName: String,
  val isStore: Boolean,
  val isStream: Boolean,
  val isCompute: Boolean,
) {
  constructor(
    server: RemoteServer<*>
  ) : this(
    name = server.javaClass.name,
    dtdName = server.dataTypeDescriptor.name,
    isStore = server is RemoteStoreServer<*>,
    isStream = server is RemoteStreamServer<*>,
    isCompute = server is RemoteComputeServer<*, *>,
  )

  internal object TypeAdapterImpl : TypeAdapter<RemoteServerInfo>() {
    private const val NAME_FIELD = "name"
    private const val DTD_NAME_FIELD = "dataTypeName"
    private const val SERVER_TYPE_FIELD = "serverType"
    private const val SERVER_TYPE_STORE = "store"
    private const val SERVER_TYPE_STREAM = "stream"
    private const val SERVER_TYPE_COMPUTE = "compute"

    override fun write(out: JsonWriter, value: RemoteServerInfo?) {
      if (value == null) {
        out.nullValue()
        return
      }

      val types = mutableListOf<String>()
      if (value.isStore) types.add(SERVER_TYPE_STORE)
      if (value.isStream) types.add(SERVER_TYPE_STREAM)
      if (value.isCompute) types.add(SERVER_TYPE_COMPUTE)

      out.makeObject {
        writeNamedField(DataHubLedger.gson, NAME_FIELD, value.name)
        writeNamedField(DataHubLedger.gson, DTD_NAME_FIELD, value.dtdName)
        writeNamedStringArray(DataHubLedger.gson, SERVER_TYPE_FIELD, types, true)
      }
    }

    override fun read(reader: JsonReader): RemoteServerInfo? {
      if (reader.peek() == JsonToken.NULL) return null

      var name = ""
      var dtdName = ""
      var isStore = false
      var isStream = false
      var isCompute = false

      reader.readObject { fieldName ->
        when (fieldName) {
          NAME_FIELD -> name = nextString()
          DTD_NAME_FIELD -> dtdName = nextString()
          SERVER_TYPE_FIELD -> {
            readStringList().forEach { type ->
              when (type) {
                SERVER_TYPE_STORE -> isStore = true
                SERVER_TYPE_STREAM -> isStream = true
                SERVER_TYPE_COMPUTE -> isCompute = true
                else -> throw IllegalArgumentException("Invalid server type: $type")
              }
            }
          }
          else ->
            throw IllegalArgumentException("Unexpected field for RemoteServerInfo: $fieldName")
        }
      }
      return RemoteServerInfo(name, dtdName, isStore, isStream, isCompute)
    }
  }
}
