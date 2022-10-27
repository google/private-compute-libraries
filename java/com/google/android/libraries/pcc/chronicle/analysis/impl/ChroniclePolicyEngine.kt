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

import com.google.android.libraries.pcc.chronicle.analysis.ChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.ManagementStrategyComparator.compare
import com.google.android.libraries.pcc.chronicle.analysis.PolicyEngine
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.SandboxProcessorNode
import com.google.android.libraries.pcc.chronicle.api.isWriteConnection
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheck
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheckResult
import com.google.android.libraries.pcc.chronicle.api.policy.canEgress

/**
 * Uses the policy engine from Arcs to check policy adherence.
 *
 * When [checkPolicy] is called, this implementation will perform the following:
 *
 * 1. Verify all the fields requested by the connection is marked as allowed for egress.
 * * Return [PolicyCheckResult.Pass] if the [ManagementStrategies] for the [ConnectionProviders]
 * being used by the `connectionRequester` are valid for the [Policy], and all of the checks
 * performed by data flow analysis pass.
 * * Return [PolicyCheckResult.Fail] otherwise.
 */
class ChroniclePolicyEngine : PolicyEngine {
  override fun checkPolicy(
    policy: Policy,
    request: ConnectionRequest<*>,
    context: ChronicleContext,
  ): PolicyCheckResult {
    // Verify the ManagementStrategies involved against the Policy, and verify the checks using
    // the results of the data-flow analysis.
    val violations =
      policy.verifyManagementStrategies(context.connectionProviders) +
        checkAllFieldsAreAllowedForEgress(context, policy, request) +
        policy.verifyContext(context.connectionContext)

    return PolicyCheckResult.make(violations)
  }

  override fun checkWriteConnections(context: ChronicleContext): PolicyCheckResult {
    val violations =
      context.connectionProviders.flatMap { checkWriteConnectionManagement(it.dataType, context) }

    return PolicyCheckResult.make(violations)
  }

  private fun checkWriteConnectionManagement(
    dataType: DataType,
    context: ChronicleContext
  ): List<PolicyCheck> {
    // If the managed data type doesn't declare any write connections, there's nothing to do here.
    if (dataType.connectionTypes.none { it.isWriteConnection }) return emptyList()

    // Find the canonical strategy from the policy set for the data type. If we had no strategy
    // laid out for the data type by any known policy; this is a problem.
    val managementStrategies = context.policySet.findManagementStrategies(dataType.descriptor)
    if (managementStrategies.isEmpty()) {
      return listOf(PolicyCheck("h:${dataType.descriptor.name} is $MUST_HAVE_POLICY_PREDICATE"))
    }

    // If the provider's management strategy is less restrictive than all of management strategies,
    // associated with a data type, that's a problem.
    if (managementStrategies.all { compare(dataType.managementStrategy, it) > 0 }) {
      return listOf(
        PolicyCheck(
          "h:${dataType.descriptor.name} is $MANAGEMENT_STRATEGY_NOT_STRICT_ENOUGH_PREDICATE"
        )
      )
    }
    return emptyList()
  }

  private fun <T : Connection> checkAllFieldsAreAllowedForEgress(
    context: ChronicleContext,
    policy: Policy,
    request: ConnectionRequest<T>,
  ): List<PolicyCheck> {
    val dtd =
      context.findDataType(request.connectionType)
        ?: return listOf(
          PolicyCheck("s:${request.connectionType} is $CONNECTON_NOT_FOUND_PREDICATE")
        )

    val policyTarget =
      policy.targets.find { it.schemaName == dtd.name }
        ?: return listOf(PolicyCheck("s:${dtd.name} is $DTD_NOT_FOUND_PREDICATE"))

    val violatingFields = mutableListOf<String>()
    dtd.fields.forEach { (fieldName, type) ->
      policyTarget.fields
        .find { it.fieldPath.last() == fieldName }
        ?.let { policyField ->
          violatingFields.addAll(
            type.findPolicyViolations(
              "${dtd.name}.$fieldName",
              policyField,
              if (request.requester is SandboxProcessorNode) {
                { !(it.rawUsages.canEgress() || it.rawUsages.contains(UsageType.SANDBOX)) }
              } else {
                { !it.rawUsages.canEgress() }
              },
              context
            )
          )
        }
        ?: violatingFields.add("${dtd.name}.$fieldName")
    }

    return violatingFields.map { PolicyCheck("s:$it is $FIELD_CANNOT_BE_EGRESSED_PREDICATE") }
  }

  private fun FieldType.findPolicyViolations(
    prefix: String,
    policyField: PolicyField,
    hasPolicyViolation: (PolicyField) -> Boolean,
    context: ChronicleContext
  ): List<String> {
    return when (this) {
      FieldType.Boolean,
      FieldType.Byte,
      FieldType.ByteArray,
      FieldType.Char,
      FieldType.Double,
      FieldType.Duration,
      FieldType.Float,
      FieldType.Instant,
      FieldType.Integer,
      FieldType.Long,
      FieldType.Short,
      FieldType.String,
      is FieldType.Enum,
      is FieldType.Opaque -> if (hasPolicyViolation(policyField)) listOf(prefix) else emptyList()
      is FieldType.Array ->
        this.itemFieldType.findPolicyViolations(prefix, policyField, hasPolicyViolation, context)
      is FieldType.List ->
        this.itemFieldType.findPolicyViolations(prefix, policyField, hasPolicyViolation, context)
      is FieldType.Nullable ->
        this.itemFieldType.findPolicyViolations(prefix, policyField, hasPolicyViolation, context)
      is FieldType.Nested -> {
        context.dataTypeDescriptorSet[this.name].findPolicyViolations(
          prefix,
          policyField,
          hasPolicyViolation,
          context
        )
      }
      is FieldType.Reference ->
        throw IllegalArgumentException("References are not supported in policy yet.")
      is FieldType.Tuple ->
        throw IllegalArgumentException("Tuples are not supported in policy yet.")
    }
  }

  private fun DataTypeDescriptor.findPolicyViolations(
    prefix: String,
    policyField: PolicyField,
    hasPolicyViolation: (PolicyField) -> Boolean,
    context: ChronicleContext
  ): List<String> {
    val violatingFields = mutableListOf<String>()
    this.fields.forEach { (fieldName, type) ->
      policyField.subfields
        .find { it.fieldPath.last() == fieldName }
        ?.let {
          violatingFields.addAll(
            type.findPolicyViolations("$prefix.$fieldName", it, hasPolicyViolation, context)
          )
        }
        ?: violatingFields.add("$prefix.$fieldName")
    }
    return violatingFields.toList()
  }

  companion object {
    /**
     * Predicate with special label used in the [Checks][Check] rendered as errors for a
     * [PolicyCheckResult.Fail] to let the user know that a given [DataType] does not have policies
     * matching its [ManagementStrategy].
     *
     * Not intended to be propagated with label propagation, it's for error-messaging only.
     */
    private const val MUST_HAVE_POLICY_PREDICATE = "\"must have a corresponding policy\""

    /**
     * Predicate with special label used in the [Checks][Check] rendered as errors for a
     * [PolicyCheckResult.Fail] to let the user know that a given [DataType] is insufficiently
     * restrained according to policy.
     *
     * Not intended to be propagated with label propagation, it's for error-messaging only.
     */
    private const val MANAGEMENT_STRATEGY_NOT_STRICT_ENOUGH_PREDICATE =
      "\"management is at least as restrained as the most conservative policy\""

    /**
     * Predicate with special label used in the [Checks][Check] rendered as errors for a
     * [PolicyCheckResult.Fail] to let the user know that a given [Connection], the corresponding
     * [DataTypeDescriptor] is not registered with [Chronicle].
     *
     * Not intended to be propagated with label propagation, it's for error-messaging only.
     */
    private const val CONNECTON_NOT_FOUND_PREDICATE = "not a registered connection"

    /**
     * Predicate with special label used in the [Checks][Check] rendered as errors for a
     * [PolicyCheckResult.Fail] to let the user know that a given [Connection], the corresponding
     * [DataTypeDescriptor] is not found specified in the given [Policy].
     *
     * Not intended to be propagated with label propagation, it's for error-messaging only.
     */
    private const val DTD_NOT_FOUND_PREDICATE = "not found in the given policy"

    /**
     * Predicate with special label used in the [Checks][Check] rendered as errors for a
     * [PolicyCheckResult.Fail] to let the user know that a given [Connection], the corresponding
     * field in the [DataTypeDescriptor] is not allowed for [Usage.ANY] in the given [Policy].
     *
     * Not intended to be propagated with label propagation, it's for error-messaging only.
     */
    private const val FIELD_CANNOT_BE_EGRESSED_PREDICATE = "not allowed for egress"
  }
}
