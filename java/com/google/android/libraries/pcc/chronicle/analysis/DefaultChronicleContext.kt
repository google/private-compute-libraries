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

package com.google.android.libraries.pcc.chronicle.analysis

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.error.ConnectionTypeAmbiguity
import com.google.android.libraries.pcc.chronicle.util.TypedMap

/**
 * Default implementation of [ChronicleContext] configured and intended to represent the data-flow
 * characteristics and requirements of Chronicle in the process.
 */
class DefaultChronicleContext(
  override val connectionProviders: Set<ConnectionProvider>,
  override val processorNodes: Set<ProcessorNode>,
  override val policySet: PolicySet,
  override val dataTypeDescriptorSet: DataTypeDescriptorSet,
  override val connectionContext: TypedMap = TypedMap()
) : ChronicleContext {
  private val connectionProviderByType: Map<Class<out Connection>, ConnectionProvider>
  private val dtdByType: Map<Class<out Connection>, DataTypeDescriptor>
  private val mgmtStrategyByType: Map<Class<out Connection>, ManagementStrategy>

  // Note: It would be nice to not have to do this much work each time we create a new
  // ChronicleContextImpl when adding a node.
  init {
    val tempConnectionProviders = mutableMapOf<Class<out Connection>, ConnectionProvider>()
    val tempDtds = mutableMapOf<Class<out Connection>, DataTypeDescriptor>()
    val tempMgmtProperties = mutableMapOf<Class<out Connection>, ManagementStrategy>()

    connectionProviders.forEach { connectionProvider ->
      val dataType = connectionProvider.dataType
      dataType.connectionTypes.forEach { connectionType ->
        // Make sure we do not have connection ambiguity.
        val existingConnectionProvider = tempConnectionProviders[connectionType]
        if (existingConnectionProvider != null) {
          throw ConnectionTypeAmbiguity(
            connectionType,
            setOf(existingConnectionProvider, connectionProvider)
          )
        }
        tempConnectionProviders[connectionType] = connectionProvider
        tempDtds[connectionType] = dataType.descriptor
        tempMgmtProperties[connectionType] = dataType.managementStrategy
      }
    }

    connectionProviderByType = tempConnectionProviders
    dtdByType = tempDtds
    mgmtStrategyByType = tempMgmtProperties
  }

  override fun <T : Connection> findConnectionProvider(
    connectionType: Class<T>
  ): ConnectionProvider? {
    return connectionProviderByType[connectionType]
  }

  override fun <T : Connection> findDataType(connectionType: Class<T>): DataTypeDescriptor? {
    return dtdByType[connectionType]
  }

  override fun withNode(node: ProcessorNode): ChronicleContext {
    return DefaultChronicleContext(
      connectionProviders = connectionProviders,
      processorNodes = processorNodes + node,
      policySet = policySet,
      dataTypeDescriptorSet = dataTypeDescriptorSet,
      connectionContext = connectionContext
    )
  }

  /**
   * Returns a copy of the current [ChronicleContext] containing the provided [connectionContext].
   */
  override fun withConnectionContext(connectionContext: TypedMap): ChronicleContext {
    return DefaultChronicleContext(
      connectionProviders = connectionProviders,
      processorNodes = processorNodes,
      policySet = policySet,
      dataTypeDescriptorSet = dataTypeDescriptorSet,
      connectionContext = connectionContext
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DefaultChronicleContext

    if (connectionProviders != other.connectionProviders) return false
    if (processorNodes != other.processorNodes) return false
    if (policySet != other.policySet) return false
    if (connectionContext != other.connectionContext) return false

    return true
  }

  override fun hashCode(): Int {
    var result = connectionProviders.hashCode()
    result = 31 * result + processorNodes.hashCode()
    result = 31 * result + policySet.hashCode()
    result = 31 * result + connectionContext.hashCode()
    return result
  }
}
