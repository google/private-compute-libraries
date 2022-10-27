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

package com.google.android.libraries.pcc.chronicle.storage

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType

/** An abstract [ConnectionProvider] that maps an underlying [ManagedStore] to Chronicle. */
abstract class ManagedConnectionProvider(
  underlying: ManagedStore,
  private val connections: Map<Class<out Connection>, (ConnectionRequest<*>) -> Connection>
) : ConnectionProvider {
  final override val dataType: DataType =
    ManagedDataType(underlying.dataTypeDescriptor, underlying.managementStrategy, connections.keys)

  final override fun getConnection(
    connectionRequest: ConnectionRequest<out Connection>
  ): Connection {
    val factory =
      requireNotNull(connections[connectionRequest.connectionType]) {
        "Unsupported connection type ${connectionRequest.connectionType}"
      }
    return factory(connectionRequest)
  }
}
