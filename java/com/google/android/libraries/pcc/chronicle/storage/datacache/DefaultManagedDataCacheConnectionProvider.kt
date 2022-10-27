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

package com.google.android.libraries.pcc.chronicle.storage.datacache

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest

/**
 * A derived class of [ManagedDataCacheConnectionProvider] that provides a default implementation of
 * [getConnection][ConnectionProvider.getConnnection] that returns the instances passed through the
 * [connections] map.
 */
abstract class DefaultManagedDataCacheConnectionProvider<T>(
  private val cache: ManagedDataCache<T>,
  private val connections: Map<Class<out Connection>, () -> Connection>
) : ManagedDataCacheConnectionProvider<T>(cache, connections.keys) {
  final override fun getConnection(
    connectionRequest: ConnectionRequest<out Connection>
  ): Connection {
    return requireNotNull(connections[connectionRequest.connectionType]?.invoke()) {
      "Unsupported connection type $connectionRequest"
    }
  }
}
