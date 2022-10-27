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
import com.google.gson.stream.JsonWriter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/** Represents a collection of [LedgerInfo] instances, grouped by their [names][LedgerInfo.name]. */
data class LedgerMap(val ledgers: MutableMap<String, LedgerInfo> = mutableMapOf()) {
  /** Appends a [LedgerInfo] to the map. */
  fun add(info: LedgerInfo) {
    ledgers[info.name] = info
  }

  /** Serializes to JSON and writes the full [LedgerMap] to the file at the provided [path]. */
  fun writeTo(path: String) {
    val file = File(path)
    BufferedWriter(FileWriter(file, false)).use { writer ->
      writer.write(DataHubLedger.gson.toJson(this))
      writer.newLine()
      writer.flush()
    }
  }

  /** Merges this [LedgerMap] with the [other] and returns the result, without modifying either. */
  fun merge(other: LedgerMap): LedgerMap {
    val newLedgers = mutableMapOf<String, LedgerInfo>()
    newLedgers.putAll(ledgers)
    newLedgers.putAll(other.ledgers)
    return LedgerMap(ledgers = newLedgers)
  }

  internal object TypeAdapterImpl : TypeAdapter<LedgerMap>() {
    private const val DTDS_FIELD = "dtds"
    private const val POLICIES_FIELD = "policies"
    private const val PROCESSES_FIELD = "processes"

    override fun write(out: JsonWriter, value: LedgerMap?) {
      if (value == null) {
        out.nullValue()
        return
      }

      // Deduplicate policies, but allow for _different_ policies with the _same_ name (by creating
      // canonical policy names based on the order the policy was seen in the ledger's sorted
      // contents).
      val sameNamePolicyJsonValues = mutableMapOf<String, List<String>>()
      val canonicalPolicyNames = mutableMapOf<Policy, String>()
      value.ledgers.values
        .sortedBy { it.name }
        .flatMap { it.policies }
        .forEach { policy ->
          // Find the canonical name for the policy. Do bookkeeping to keep track of whether it's
          // been seen yet.
          val policyJson = DataHubLedger.gson.toJson(policy)
          var othersWithSameName = sameNamePolicyJsonValues[policy.name] ?: emptyList()
          // We use JSON values here because Policy deep-equals checking doesn't work as well as
          // comparing JSON representations when looking for duplicates.
          var index = othersWithSameName.withIndex().find { policyJson == it.value }?.index
          if (index == null) {
            // It's new to the set.
            index = othersWithSameName.size
            othersWithSameName = othersWithSameName + policyJson
            sameNamePolicyJsonValues[policy.name] = othersWithSameName
          }

          canonicalPolicyNames += policy to "${policy.name}${if (index == 0) "" else "-$index"}"
        }

      // Flip the canonical name map so the keys are the names.
      val policies = canonicalPolicyNames.entries.associate { it.value to it.key }
      // Use the canonical names to turn LedgerInfos into ProcessEntries
      val processEntries = value.ledgers.values.map { it.toProcessEntry(canonicalPolicyNames) }
      // Extract the collection of DTDs
      val dtds = value.ledgers.values.flatMap { it.dataTypeDescriptors }.toSet()

      out.makeObject {
        writeNamedArray(DataHubLedger.gson, DTDS_FIELD, dtds) { it.name }
        writeNamedMap(DataHubLedger.gson, POLICIES_FIELD, policies)
        writeNamedMap(DataHubLedger.gson, PROCESSES_FIELD, processEntries.associateBy { it.name })
      }
    }

    override fun read(reader: JsonReader): LedgerMap {
      var processEntries = emptyMap<String, ProcessEntry>()
      var dtds = emptySet<DataTypeDescriptor>()
      var policiesByCanonicalNames = emptyMap<String, Policy>()
      reader.readObject { key ->
        when (key) {
          DTDS_FIELD -> dtds = readSet(DataHubLedger.gson)
          POLICIES_FIELD -> policiesByCanonicalNames = readMap(DataHubLedger.gson)
          PROCESSES_FIELD -> processEntries = readMap(DataHubLedger.gson)
          else -> throw IllegalArgumentException("Unexpected field in LedgerMap: $key")
        }
      }
      return LedgerMap(
        processEntries
          .mapValues { (_, entry) -> entry.toLedgerInfo(policiesByCanonicalNames, dtds) }
          .toMutableMap()
      )
    }
  }
}
