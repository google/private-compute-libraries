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

package com.google.android.libraries.pcc.chronicle.storage.stream

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.ManagedDataTypeWithRemoteConnectionNames
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

/**
 * Type alias for a function which builds a [Connection] instance given a [ConnectionRequest] and an
 * [EntityStream] intended for local usage (ie. on the same process as the server).
 */
private typealias LocalConnectionBuilder<T> = (ConnectionRequest<*>, EntityStream<T>) -> Connection

/** Type alias for a map from [Connection] class to [LocalConnectionBuilder]. */
private typealias LocalConnectionBuilders<T> = Map<Class<out Connection>, LocalConnectionBuilder<T>>

/**
 * Wrapper of an [EntityStream], providing [ConnectionProvider] and [RemoteStreamServer]
 * capabilities.
 *
 * To enable remote publish/subscribe behavior, ManagedEntityStream instances should be provided on
 * the server-side of a client/server relationship.
 *
 * @param dataTypeDescriptor The [DataTypeDescriptor] for [T].
 * @param serializer A [Serializer] capable of serializing/deserializing [T] instances for remote
 * transmission.
 * @param entityStreamProvider Manager of the underlying [EntityStream] implementation to use for
 * publish/subscribe.
 * @param localConnectionBuilders Map of [Connection] class to builder method, used in
 * [getConnection], to construct new connection implementations for local access.
 */
class ManagedEntityStreamServer<T : Any>(
  override val dataTypeDescriptor: DataTypeDescriptor,
  override val serializer: Serializer<T>,
  entityStreamProvider: EntityStreamProvider,
  private val localConnectionBuilders: LocalConnectionBuilders<T> = emptyMap(),
) : ConnectionProvider, RemoteStreamServer<T> {
  override val dataType: DataType =
    ManagedDataTypeWithRemoteConnectionNames(
      descriptor = dataTypeDescriptor,
      managementStrategy = ManagementStrategy.PassThru,
      connectionTypes = localConnectionBuilders.keys
    )

  @Suppress("UNCHECKED_CAST") // Checked by privacy review and policy.
  private val entityStream: EntityStream<T> =
    entityStreamProvider.getStream(dataTypeDescriptor.cls as KClass<T>)

  override fun subscribe(policy: Policy?): Flow<List<WrappedEntity<T>>> =
    entityStream.subscribeGroups()

  override suspend fun publish(policy: Policy?, entities: List<WrappedEntity<T>>) =
    entityStream.publishGroup(entities)

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
    val builder =
      requireNotNull(localConnectionBuilders[connectionRequest.connectionType]) {
        "No connection builder found for $connectionRequest"
      }
    return builder(connectionRequest, entityStream)
  }
}
