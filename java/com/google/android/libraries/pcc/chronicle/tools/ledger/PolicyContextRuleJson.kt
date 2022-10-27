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

import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * Collection of GSON [TypeAdapter] instances for converting [PolicyContextRule] objects to JSON.
 */
internal object PolicyContextRuleJson {

  /** TypeAdapter for [PolicyContextRule] */
  object PolicyContextRuleTypeAdapter : TypeAdapter<PolicyContextRule>() {
    /** Writes a given [PolicyContextRule] to the [JsonWriter]. */
    override fun write(out: JsonWriter, value: PolicyContextRule?) {
      if (value == null) {
        out.nullValue()
        return
      }

      // If `operands` is an empty list, then collapse the rule into a single line
      if (value.operands.isEmpty()) {
        out.makeSingleLineObject { writeNamedField(DataHubLedger.gson, "name", value.name) }
        return
      }

      out.makeObject {
        writeNamedField(DataHubLedger.gson, "name", value.name)
        writeNamedArray(DataHubLedger.gson, "operands", value.operands) { it.name }
      }
    }

    /** Read function to parse [PolicyContextRule]s and any nested operands. */
    override fun read(reader: JsonReader): PolicyContextRule? {
      var name = ""
      var operands: List<PolicyContextRule> = mutableListOf()

      reader.readObject { fieldName ->
        when (fieldName) {
          "name" -> name = reader.nextString()
          "operands" -> operands = reader.readList(DataHubLedger.gson)
        }
      }

      return ContextRule(name, operands)
    }
  }

  /**
   * Used as a container to simplify context rule deserialization.
   *
   * Note: This means that rules are not deserialized back into their true subclasses! The main
   * benefit is that this setup allows future [PolicyContextRule] implementations to happen
   * independently of the ledger.
   */
  class ContextRule(
    override val name: String = "",
    override val operands: List<PolicyContextRule> = emptyList()
  ) : PolicyContextRule {
    override fun invoke(context: TypedMap): Boolean = true
  }
}
