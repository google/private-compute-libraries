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
import com.google.android.libraries.pcc.chronicle.api.ConnectionName
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.util.TypedMap

/**
 * Immutable data structure which maintains the universe of [ProcessorNodes][ProcessorNode] and
 * [ConnectionProviders][ConnectionProvider] known by Chronicle.
 *
 * Note:
 *
 * [connectionProviders] and [processorNodes] are not necessarily mutually exclusive sets. It's
 * possible that a single class could implement both interfaces.
 */
interface ChronicleContext {
  /** All [ConnectionProvider]s known to Chronicle. */
  val connectionProviders: Set<ConnectionProvider>

  /** All [ProcessorNode]s known to Chronicle. */
  val processorNodes: Set<ProcessorNode>

  /** Collection of all known [Policies][Policy]. */
  val policySet: PolicySet

  /** All [DataTypeDescriptor]s known to Chronicle. */
  val dataTypeDescriptorSet: DataTypeDescriptorSet

  /** Contextual variables used in Policy evaluations */
  val connectionContext: TypedMap

  /**
   * Returns a [ConnectionProvider] capable of providing [Connection]s of the specified
   * [connectionType].
   */
  // TODO(b/251295492) This function can disappear.
  fun <T : Connection> findConnectionProvider(connectionType: Class<T>?): ConnectionProvider?

  /**
   * Returns a [ConnectionProvider] capable of providing [Connection]s of the specified
   * [ConnectionName].
   */
  fun <T : Connection> findConnectionProvider(
    connectionName: ConnectionName<T>
  ): ConnectionProvider?

  /** Returns the [DataTypeDescriptor] associated with the provided [connectionType], if found. */
  // TODO(b/251295492) This function can disappear.
  fun <T : Connection> findDataType(connectionType: Class<T>?): DataTypeDescriptor?

  /** Returns the [DataTypeDescriptor] associated with the provided [ConnectionName], if found. */
  fun <T : Connection> findDataType(connectionName: ConnectionName<T>): DataTypeDescriptor?

  /** Returns a new [ChronicleContext] containing the provided [ProcessorNode]. */
  fun withNode(node: ProcessorNode): ChronicleContext

  /** Returns a new [ChronicleContext] containing the provided [connectionContext]. */
  fun withConnectionContext(connectionContext: TypedMap): ChronicleContext
}
