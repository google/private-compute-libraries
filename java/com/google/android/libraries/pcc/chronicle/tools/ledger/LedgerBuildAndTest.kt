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

import com.google.android.libraries.pcc.chronicle.analysis.ChronicleContext
import com.google.android.libraries.pcc.chronicle.remote.RemoteContext
import com.google.common.truth.Truth.assertWithMessage

/**
 * Utility which, given a [ChronicleContext] constructs a ledger and either stores it to disk or
 * tests it against a previously-stored version.
 */
class LedgerBuildAndTest(
  private val namedContexts: Map<String, Pair<ChronicleContext?, RemoteContext?>>
) {
  private val mapFileName: String = requireNotNull(System.getProperty("map_name"))
  private val name: String = requireNotNull(System.getProperty("name"))
  private val isTest: Boolean = requireNotNull(System.getProperty("is_test")).toInt() == 1
  private val outputDir: String? = System.getenv("OUTPUT_DIR")

  /** Runs the build/test of the ledger for the current app variant. */
  fun run() {
    val map =
      namedContexts.entries.fold(LedgerMap()) { acc, (contextName, contexts) ->
        // Create a suffix with the contextName, if one is provided.
        val suffix = contextName.takeIf { it.isNotEmpty() }?.let { "-$it" } ?: ""
        acc.add(DataHubLedger.buildLedger("$name$suffix", contexts.first, contexts.second))
        acc
      }
    if (isTest) test(map) else build(map)
  }

  private fun build(ledgerMap: LedgerMap) {
    val folderName = outputDir ?: return
    val fileName = "$folderName/$name.json"
    ledgerMap.writeTo(fileName)
  }

  private fun test(ledgerMap: LedgerMap) {
    val previouslyGenerated = DataHubLedger.readLedgers(mapFileName)
    ledgerMap.ledgers.forEach { (name, infoFromCurrentBuild) ->
      val previouslyGeneratedForName =
        requireNotNull(previouslyGenerated.ledgers[name]) {
          "It looks like the ledger record for build/process: $name does not yet exist. " +
            "Please run the appropriate script to rebuild the ledger."
        }
      assertWithMessage("Existing ledger record for $name must match what is in the build.")
        .that(infoFromCurrentBuild.toJsonString())
        .isEqualTo(previouslyGeneratedForName.toJsonString())
    }
  }
}
