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

import com.google.android.libraries.pcc.chronicle.analysis.ManagementStrategyComparator.compare
import com.google.android.libraries.pcc.chronicle.analysis.retentionsAsManagementStrategies
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyRetention
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyTarget
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheck
import com.google.android.libraries.pcc.chronicle.api.policy.builder.deletionTriggers

/**
 * Verifies that [ManagementStrategy] values declared by the [ConnectionProviders]
 * [ConnectionProvider] are valid given policy-requirements for the receiving [Policy] object.
 *
 * @return a list of [Checks][PolicyCheck]. If empty, the implication is that policy adherence is
 *   satisfied. If non-empty, the values will describe how policy is not being met.
 */
internal fun Policy.verifyManagementStrategies(
  connectionProviders: Collection<ConnectionProvider>
): List<PolicyCheck> {
  val targetsBySchemaName = targets.map(PolicyTarget::schemaName).toSet()
  val managedDataTypesBySchemaName =
    connectionProviders
      .asSequence()
      .map { it.dataType }
      // Ensure that this managed data type is mentioned by the policy.
      .filter { it.descriptor.name in targetsBySchemaName }
      .associateBy { it.descriptor.name }

  return targets.flatMap { target ->
    // If there is no managed data type for the target, it means that the policy contains an extra
    // target, which is no problem - skip it.
    val dataType = managedDataTypesBySchemaName[target.schemaName] ?: return@flatMap emptyList()

    val retentionViolations = target.verifyRetentionSatisfiedBy(dataType)
    val deletionViolations = target.verifyDeletionTriggersSatisfiedBy(dataType)

    retentionViolations + deletionViolations
  }
}

/**
 * Verifies that one of the receiving [PolicyTarget]'s [PolicyRetentions][PolicyRetention] and the
 * max age are satisfied by the given [strategy]. Returns a list of any failing [Checks]
 * [PolicyCheck] (one per unsatisfied [PolicyRetention]) if-and-only-if all retentions fail.
 */
internal fun PolicyTarget.verifyRetentionSatisfiedBy(dataType: DataType): List<PolicyCheck> {
  val strategy = dataType.managementStrategy
  val failedRetentions = retentionsAsManagementStrategies().filter { compare(strategy, it) > 0 }
  if (failedRetentions.size < retentions.size) return emptyList()

  return retentions.map {
    PolicyCheck("h:${dataType.descriptor.name} is $it with maxAgeMs = $maxAgeMs")
  }
}

/**
 * Verifies that any deletion triggers required by the receiving [PolicyTarget] are satisfied by the
 * given [ManagementStrategy]. Returns any violations as [Checks][PolicyCheck].
 */
internal fun PolicyTarget.verifyDeletionTriggersSatisfiedBy(dataType: DataType): List<PolicyCheck> {
  val strategy = dataType.managementStrategy
  val missingDeletionTriggers = this.deletionTriggers().filter { !strategy.satisfies(it) }

  return missingDeletionTriggers.map { PolicyCheck("h:${dataType.descriptor.name} is $it") }
}

/**
 * Returns whether or not the receiving [ManagementStrategy] satisfies the given [DeletionTrigger].
 */
internal fun ManagementStrategy.satisfies(policyDeletionTrigger: DeletionTrigger): Boolean {
  return when (this) {
    ManagementStrategy.PassThru -> true
    is ManagementStrategy.Stored -> this.deletionTriggers.contains(policyDeletionTrigger)
  }
}
