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

package com.google.android.libraries.pcc.chronicle.remote.impl

import com.google.android.libraries.pcc.chronicle.analysis.PolicySet
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionName
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.SandboxProcessorNode
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.dataTypeName
import com.google.android.libraries.pcc.chronicle.api.remote.isReadRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails.IsolationType.ISOLATED_PROCESS
import com.google.android.libraries.pcc.chronicle.remote.RemotePolicyChecker

/** Implementation of [RemotePolicyChecker]. */
class RemotePolicyCheckerImpl(private val chronicle: Chronicle, private val policySet: PolicySet) :
  RemotePolicyChecker {
  private val processorNodes = mutableMapOf<ClientDetails, RemoteProcessorNode>()

  override fun checkAndGetPolicyOrThrow(
    metadata: RemoteRequestMetadata,
    server: RemoteServer<*>,
    clientDetails: ClientDetails,
  ): Policy? {
    // TODO(b/210998515): Use usage type to find policy instead of id.
    val policy = policySet.findByName(metadata.usageType)
    if (metadata.usageType.isNotBlank() && policy == null) {
      throw RemoteError(
        type = RemoteErrorMetadata.Type.POLICY_NOT_FOUND,
        message = "No policy found with id/usageType [${metadata.usageType}]",
      )
    }

    val connectionName: ConnectionName<Connection> = connectionNameFrom(metadata)

    val processorNode =
      synchronized(this) {
        val node =
          processorNodes[clientDetails]?.also { it.connectionNames.add(connectionName) }
            ?: RemoteProcessorNode(mutableSetOf(connectionName))
        processorNodes[clientDetails] = node
        node
      }

    chronicle
      .checkPolicy(
        metadata.dataTypeName,
        policy,
        metadata.isReadRequest,
        processorNode.let {
          if (clientDetails.isolationType == ISOLATED_PROCESS) SandboxProcessorNode(it) else it
        },
      )
      .getOrThrow()

    return policy
  }

  private fun connectionNameFrom(metadata: RemoteRequestMetadata): ConnectionName<Connection> =
    if (metadata.isReadRequest) {
      ReadConnection.connectionName(metadata.dataTypeName)
    } else {
      WriteConnection.connectionName(metadata.dataTypeName)
    }

  /** Simple [ProcessorNode] implementation used to represent a remote store request. */
  private class RemoteProcessorNode(
    val connectionNames: MutableSet<ConnectionName<out Connection>>
  ) : ProcessorNode {
    override val requiredConnectionTypes: Set<Class<out Connection>>
      get() = emptySet() // not used since [ProcessorNode.requiredConnectionNames] is in use.

    override val requiredConnectionNames: Set<ConnectionName<out Connection>> = connectionNames
  }
}
