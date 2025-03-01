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

package com.google.android.libraries.pcc.chronicle.analysis.impl

import com.google.android.libraries.pcc.chronicle.analysis.CantripFactory
import com.google.android.libraries.pcc.chronicle.analysis.ChronicleContext
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.cantrip.Cantrip
import com.google.android.libraries.pcc.chronicle.api.cantrip.MultiCantrip
import com.google.android.libraries.pcc.chronicle.api.cantrip.OpticalCantrip
import com.google.android.libraries.pcc.chronicle.api.operation.Operation
import com.google.android.libraries.pcc.chronicle.api.operation.OperationLibrary
import com.google.android.libraries.pcc.chronicle.api.optics.OpticalAccessPath
import com.google.android.libraries.pcc.chronicle.api.optics.OpticsManifest
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.builder.UsageType

/**
 * Implementation of [CantripFactory] which composes its [Cantrips][Cantrip] using the provided
 * [OpticsManifest] and [OperationLibrary].
 *
 * The [ChronicleContext] provides a means to look up the [DataTypeDescriptor] associated with the
 * [ConnectionRequest.connectionType] of the [ConnectionRequest] passed to [buildCantrip].
 */
class CantripFactoryImpl(
  private val optics: OpticsManifest,
  private val operations: OperationLibrary,
  private val dtds: DataTypeDescriptorSet,
) : CantripFactory {

  @Suppress("UNCHECKED_CAST") // Types are checked via alternate mechanisms.
  override fun <Data> buildCantrip(
    dtd: DataTypeDescriptor,
    requester: ProcessorNode,
    policy: Policy?,
    usageType: UsageType,
  ): Cantrip<Data> {
    val noOp = MultiCantrip<Data>()
    val policyTarget = policy?.targets?.find { it.schemaName == dtd.name } ?: return noOp

    val conditionalUsages =
      policyTarget.fields.flatMap { collectConditionalUsages(dtd, it, usageType) }

    val innerCantrips =
      conditionalUsages.map {
        val dtdClass = dtd.cls.java
        val traversal =
          optics.composeTraversal(it.accessPath, dtdClass, dtdClass, it.fieldType, it.fieldType)
        val op =
          requireNotNull(operations.findOperation(it.tag, it.fieldType, it.fieldType)) {
            "No Operation found with name: ${it.tag} for type: ${it.fieldType}"
          }
        OpticalCantrip(traversal, op as Operation<Any, Any>) as Cantrip<Data>
      }

    return MultiCantrip(innerCantrips)
  }

  /**
   * Does a traversal to collect [ConditionalUsage] from the [PolicyField], composing a pre-order
   * list as a result.
   */
  private fun collectConditionalUsages(
    baseDtd: DataTypeDescriptor,
    field: PolicyField,
    usageType: UsageType,
  ): List<ConditionalUsage> {
    val innerUsages = field.subfields.flatMap { collectConditionalUsages(baseDtd, it, usageType) }

    val pathToField = OpticalAccessPath(baseDtd, field.fieldPath)
    val localUsages =
      field.redactedUsages.entries
        .filter { (_, types) -> usageType in types || UsageType.ANY in types }
        .map {
          ConditionalUsage(
            accessPath = pathToField,
            tag = it.key,
            fieldType = dtds.findFieldTypeAsClass(baseDtd, field.fieldPath),
          )
        }

    return localUsages + innerUsages
  }

  private data class ConditionalUsage(
    val accessPath: OpticalAccessPath,
    val tag: String,
    val fieldType: Class<*>,
  )
}
