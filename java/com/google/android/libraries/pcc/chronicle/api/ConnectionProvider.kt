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

/**
 * A [ConnectionProvider] is capable of providing [Connection]s to data.
 *
 * For each type of data a [ConnectionProvider] provides connections to, it also must supply
 * [Chronicle] with information about the capabilities of the storage of said data (See
 * [ManagedDataType] and [ManagementStrategy]).
 */
interface ConnectionProvider {
  val dataType: DataType

  /**
   * Builds and returns a connection of the provided [connectionRequest].
   *
   * NOTE: the [ConnectionRequest] here will contain either [ConnectionRequest.connectionType] or
   * [ConnectionRequest.connectionName], which should match what was specified in [ProcessorNode].
   */
  fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection

  @Suppress("UNCHECKED_CAST")
  fun <T : Connection> getTypedConnection(connectionRequest: ConnectionRequest<T>): T {
    return requireNotNull(getConnection(connectionRequest) as? T) {
      "Returned connection could not be cast to $connectionRequest"
    }
  }
}
