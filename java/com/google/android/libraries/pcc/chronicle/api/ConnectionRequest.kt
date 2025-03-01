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

package com.google.android.libraries.pcc.chronicle.api

import com.google.android.libraries.pcc.chronicle.api.policy.Policy

/**
 * Encapsulates a request for a [Connection] using a [ConnectionName] object, which wraps the
 * [Connection] with a type that doesn't require a Class object. Here you can use the [Name] class
 * to decorate a [Connection] using (a hierarchy of) strings.
 *
 * NOTE: Use this constructor if your [ProcessorNode] overrides
 * [ProcessorNode.requiredConnectionNames].
 */
data class ConnectionRequest<T : Connection>(
  val connectionName: ConnectionName<T>,
  val requester: ProcessorNode,
  val policy: Policy?,
) {
  // TODO(b/251295492) mark as deprecated, then remove.
  var connectionType: Class<T>? = null

  /**
   * Encapsulates a request for a [Connection] using the Class object of the [Connection]. If you
   * don't have a Class object, use the other constructor instead.
   *
   * NOTE: Use this constructor if your [ProcessorNode] overrides
   * [ProcessorNode.requiredConnectionTypes].
   */
  // TODO(b/251295492) mark as deprecated, then remove.
  constructor(
    connectionType: Class<T>,
    requester: ProcessorNode,
    policy: Policy?,
  ) : this(Connection.connectionName(connectionType), requester, policy) {
    this.connectionType = connectionType
  }

  fun isReadConnection(): Boolean =
    connectionType?.isReadConnection ?: (connectionName is ConnectionName.Reader)

  override fun toString(): String =
    """
      ConnectionRequest(
        connectionName=${connectionName}
        requester=${requester::class.simpleName}
        policy=${policy?.name}
      )
    """
      .trimIndent()
}
