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
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.SandboxProcessorNode
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.isReadRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails.IsolationType
import com.google.android.libraries.pcc.chronicle.remote.RemotePolicyChecker

/** Implementation of [RemotePolicyChecker]. */
class RemotePolicyCheckerImpl(
  private val chronicle: Chronicle,
  private val policySet: PolicySet,
) : RemotePolicyChecker {
  private val processorNodes = mutableMapOf<ClientDetails, RemoteProcessorNode>()

  override fun checkAndGetPolicyOrThrow(
    metadata: RemoteRequestMetadata,
    server: RemoteServer<*>,
    clientDetails: ClientDetails
  ): Policy? {
    val connectionClass =
      if (metadata.isReadRequest) server.readConnection.javaClass
      else server.writeConnection.javaClass

    // TODO(b/210998515): Use usage type to find policy instead of id.
    val policy = policySet.findByName(metadata.usageType)
    if (metadata.usageType.isNotBlank() && policy == null) {
      throw RemoteError(
        type = RemoteErrorMetadata.Type.POLICY_NOT_FOUND,
        message = "No policy found with id/usageType [${metadata.usageType}]"
      )
    }
    val processorNode =
      synchronized(this) {
        val node =
          processorNodes[clientDetails]?.also { it.connectionClasses.add(connectionClass) }
            ?: RemoteProcessorNode(mutableSetOf(connectionClass))
        processorNodes[clientDetails] = node
        node
      }

    // Create a ConnectionRequest and try to use it. If it succeeds then we can proceed.
    // If it throws, the exception will be picked up by the CoroutineExceptionHandler.
    val connectionRequest =
      ConnectionRequest(
        connectionClass,
        if (clientDetails.isolationType == IsolationType.ISOLATED_PROCESS) {
          SandboxProcessorNode(processorNode)
        } else processorNode,
        policy
      )
    chronicle.getConnectionOrThrow(connectionRequest)

    return policy
  }

  /** Simple [ProcessorNode] implementation used to represent a remote store request. */
  private class RemoteProcessorNode(val connectionClasses: MutableSet<Class<out Connection>>) :
    ProcessorNode {
    override val requiredConnectionTypes: Set<Class<out Connection>> = connectionClasses
  }
}
