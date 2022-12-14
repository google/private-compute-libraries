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
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyTarget
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.AnnotationParam
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import com.google.android.libraries.pcc.chronicle.remote.RemoteContext
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * Utilities to read and construct [LedgerMap] and [LedgerInfo] instances, as well as write them to
 * files as JSON.
 */
object DataHubLedger {
  private val currentWorkingDir = System.getProperty("user.dir")

  internal val gson =
    GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(AnnotationParam::class.java, PolicyJson.AnnotationParamTypeAdapter)
      .registerTypeAdapter(DataTypeDescriptor::class.java, DtdJson.DataTypeDescriptorTypeAdapter)
      .registerTypeAdapter(FieldType::class.java, DtdJson.FieldTypeTypeAdapter)
      .registerTypeAdapter(LedgerDataType::class.java, LedgerDataType.TypeAdapterImpl)
      .registerTypeAdapter(LedgerInfo::class.java, LedgerInfo.TypeAdapterImpl)
      .registerTypeAdapter(LedgerMap::class.java, LedgerMap.TypeAdapterImpl)
      .registerTypeAdapter(Policy::class.java, PolicyJson.PolicyTypeAdapter)
      .registerTypeAdapter(PolicyField::class.java, PolicyJson.PolicyFieldTypeAdapter)
      .registerTypeAdapter(PolicyTarget::class.java, PolicyJson.PolicyTargetTypeAdapter)
      .registerTypeAdapter(ProcessEntry::class.java, ProcessEntry.TypeAdapterImpl)
      .registerTypeAdapter(RemoteServerInfo::class.java, RemoteServerInfo.TypeAdapterImpl)
      .registerTypeAdapter(
        PolicyContextRule::class.java,
        PolicyContextRuleJson.PolicyContextRuleTypeAdapter
      )
      .create()

  /** Reads a [LedgerMap] as JSON from a file at the provided [path]. */
  fun readLedgers(path: String, isAbsolute: Boolean = false): LedgerMap {
    val file =
      if (!isAbsolute) File(currentWorkingDir, path) else File(path)
    return BufferedReader(FileReader(file)).use { gson.fromJson(it, LedgerMap::class.java) }
  }

  /** Merges all [LedgerMap] files in the [directoryPath] into a single instance and returns it. */
  @JvmStatic
  fun mergeLedgers(directoryPath: String, isAbsolute: Boolean = false): LedgerMap {

    val dir =
      if (!isAbsolute) {
        File(currentWorkingDir, directoryPath)
      } else {
        File(directoryPath)
      }

    var result = LedgerMap()
    requireNotNull(dir.listFiles()) { "$directoryPath is not a directory" }
      .forEach { file -> result = result.merge(readLedgers(file.absolutePath, isAbsolute = true)) }
    return result
  }

  /**
   * Given a [ChronicleContext] and an application build variant [name], constructs a [LedgerInfo].
   */
  fun buildLedger(
    name: String,
    chronicleContext: ChronicleContext?,
    remoteContext: RemoteContext?,
  ): LedgerInfo {
    val dtdToPolicies =
      (chronicleContext?.policySet?.toSet() ?: emptySet())
        .flatMap { policy ->
          // Create <DTD, Policy> pairs.
          policy.targets.map {
            requireNotNull(chronicleContext?.dataTypeDescriptorSet?.get(it.schemaName)) to policy
          }
        }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    val dtdToConnections =
      (chronicleContext?.connectionProviders ?: emptySet())
        .flatMap { provider ->
          // Create <DTD, Connection> pairs.
          val descriptor = provider.dataType.descriptor
          provider.dataType.connectionTypes.map { connection -> descriptor to connection }
        }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    val dtdToServers =
      dtdToPolicies.keys.associateWith {
        remoteContext?.findServers(it)?.map { server -> RemoteServerInfo(server) }
      }

    val dataTypeInfos =
      dtdToPolicies.keys.union(dtdToConnections.keys).map { dtd ->
        val connections =
          dtdToConnections[dtd]?.mapNotNull { it.name.takeIf(String::isNotEmpty) } ?: emptyList()
        val policies = dtdToPolicies[dtd]?.map { it.name } ?: emptyList()
        LedgerDataType(dtd.name, connections, dtdToServers[dtd] ?: emptyList(), policies)
      }

    val dataTypeInfoNames = dataTypeInfos.map { it.name }.toSet()

    return LedgerInfo(
      name = name,
      dataTypes = dataTypeInfos,
      dataTypeDescriptors =
        chronicleContext
          ?.dataTypeDescriptorSet
          ?.toSet()
          ?.filter { it.name in dataTypeInfoNames }
          ?.toSet()
          ?: emptySet(),
      policies = chronicleContext?.policySet?.toSet() ?: emptySet()
    )
  }
}
