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

package com.google.android.libraries.pcc.chronicle.api.integration

import com.google.android.libraries.pcc.chronicle.analysis.ChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.PolicyEngine
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ConnectionResult
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.error.ChronicleError
import com.google.android.libraries.pcc.chronicle.api.error.ConnectionNotDeclared
import com.google.android.libraries.pcc.chronicle.api.error.ConnectionProviderNotFound
import com.google.android.libraries.pcc.chronicle.api.error.Disabled
import com.google.android.libraries.pcc.chronicle.api.error.PolicyNotFound
import com.google.android.libraries.pcc.chronicle.api.error.PolicyViolation
import com.google.android.libraries.pcc.chronicle.api.flags.FlagsReader
import com.google.android.libraries.pcc.chronicle.api.isReadConnection
import com.google.android.libraries.pcc.chronicle.api.isWriteConnection
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyConformanceCheck
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheck
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheckResult
import com.google.android.libraries.pcc.chronicle.util.Logcat
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import kotlin.reflect.KClass
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/** Default implementation of [Chronicle]. */
class DefaultChronicle(
  chronicleContext: ChronicleContext,
  private val policyEngine: PolicyEngine,
  private val config: Config,
  private val flags: FlagsReader,
) : Chronicle {
  private val context = atomic(chronicleContext)

  init {
    config.policyConformanceCheck.checkPoliciesConform(chronicleContext.policySet.toSet())

    chronicleContext.connectionProviders.forEach {
      // Log if not null
      it::class.qualifiedName?.let { qualifiedName ->
        logger.d("ConnectionProvider: %s", qualifiedName)
      }
    }

    // Verify that all provided write connections match all associated policies, and that they all
    // are mentioned in at least one policy.
    val writeConnectionCheckResult = policyEngine.checkWriteConnections(context.value)
    if (writeConnectionCheckResult is PolicyCheckResult.Fail) {
      handlePolicyCheckFail(writeConnectionCheckResult)?.let { throw it }
    }
  }

  @Suppress("UNCHECKED_CAST") // Checks happen, just not in a way that is compiler-verifiable.
  override fun getAvailableConnectionTypes(dataTypeClass: KClass<*>): Chronicle.ConnectionTypes {
    val context = context.value

    val dtd =
      context.dataTypeDescriptorSet.findDataTypeDescriptor(dataTypeClass)
        ?: return Chronicle.ConnectionTypes.EMPTY

    return context.connectionProviders
      .filter { it.dataType.descriptor == dtd }
      .fold(Chronicle.ConnectionTypes.EMPTY) { acc, provider ->
        val readConnections = mutableSetOf<Class<out ReadConnection>>()
        val writeConnections = mutableSetOf<Class<out WriteConnection>>()
        provider.dataType.connectionTypes.forEach { connectionClass ->
          if (connectionClass.isReadConnection) {
            readConnections.add(connectionClass as Class<out ReadConnection>)
          }
          if (connectionClass.isWriteConnection) {
            writeConnections.add(connectionClass as Class<out WriteConnection>)
          }
        }

        Chronicle.ConnectionTypes(
          readConnections = acc.readConnections + readConnections,
          writeConnections = acc.writeConnections + writeConnections
        )
      }
  }

  override fun <T : Connection> getConnection(request: ConnectionRequest<T>): ConnectionResult<T> =
    logger.timeVerbose("ChronicleImpl.getConnection($request)") { getConnectionInner(request) }

  private fun <T : Connection> getConnectionInner(
    request: ConnectionRequest<T>
  ): ConnectionResult<T> {
    if (flags.config.value.failNewConnections) {
      return ConnectionResult.Failure(Disabled("Chronicle disabled via flags."))
    }

    if (!request.requester.requiredConnectionTypes.contains(request.connectionType)) {
      logger.d(
        "Connection is not declared as required in `ProcessorNode` of a request: %s",
        request
      )
      return ConnectionResult.Failure(ConnectionNotDeclared(request))
    }

    val currentContext = context.value
    val connectionProvider =
      currentContext.findConnectionProvider(request.connectionType)
        ?: return ConnectionResult.Failure(ConnectionProviderNotFound(request))

    val policy = request.policy
    if (policy != null && policy !in currentContext.policySet) {
      handlePolicyCheckException(PolicyNotFound(policy))?.let {
        return ConnectionResult.Failure(it)
      }
    }
    if (policy == null && request.connectionType.isReadConnection) {
      handlePolicyCheckFail(
          PolicyCheckResult.Fail(
            listOf(
              PolicyCheck("ConnectionRequest.policy must be non-null for ReadConnection requests")
            )
          )
        )
        ?.let {
          return ConnectionResult.Failure(it)
        }
    }

    // Add to the graph and check the policy within an atomic update to the graph.
    context.update { existing ->
      val updated = existing.withNode(request.requester)

      if (policy != null && request.connectionType.isReadConnection) {
        val checkResult = policyEngine.checkPolicy(policy, request, updated)
        if (checkResult is PolicyCheckResult.Fail) {
          handlePolicyCheckFail(checkResult)?.let {
            return ConnectionResult.Failure(it)
          }
        }
      }

      updated
    }

    return ConnectionResult.Success(connectionProvider.getTypedConnection(request))
  }

  private fun handlePolicyCheckFail(failure: PolicyCheckResult.Fail): ChronicleError? =
    handlePolicyCheckException(PolicyViolation(failure.message))

  private fun handlePolicyCheckException(error: ChronicleError): ChronicleError? {
    return when (config.policyMode) {
      Config.PolicyMode.STRICT -> error
      Config.PolicyMode.LOG -> {
        error.message?.let { logger.e("Policy violation detected: %s", it) }
        null
      }
    }
  }

  /**
   * Allows [Chronicle] to use a new `connectionContext` by updating the [ChronicleContext].
   * Subsequent [Policy] checking will use the updated context when checking the `allowedContext`.
   */
  fun updateConnectionContext(updatedContext: TypedMap) {
    context.update { existing -> existing.withConnectionContext(updatedContext) }
  }

  /** A set of configuration values that can be cased to [DefaultChronicle] at construction time. */
  data class Config(
    /** Configure the behavior for policy check failures. */
    val policyMode: PolicyMode,

    /**
     * Set of rules to apply to guarantee that all [Policies][Policy] provided to Chronicle meet a
     * standard of quality.
     */
    val policyConformanceCheck: PolicyConformanceCheck
  ) {
    enum class PolicyMode {
      /** Policy failure will be logged, but the connection request will succeed. */
      LOG,

      /** Policy failure will result in failure result being returned. */
      STRICT
    }
  }

  companion object {
    private val logger = Logcat.default
  }
}
