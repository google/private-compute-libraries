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

package com.google.android.libraries.pcc.chronicle.api.remote.server

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer

/**
 * Implementations of RemoteServer expose read/write behavior to remote clients via the
 * ChronicleService / RemoteRouter.
 *
 * A RemoteServer is also a [ConnectionProvider] in the eyes of Chronicle (as it provides access to
 * data, and that access should be policy-checked in the same way as policies are checked with other
 * connection providers).
 *
 * **Note:** This interface is not intended to be implemented directly, instead: choose one of its
 * sub-interfaces: [RemoteStoreServer], [RemoteComputeServer], or [RemoteStreamServer].
 */
interface RemoteServer<T : Any> : ConnectionProvider {
  /** Descriptor of the data type managed by this [RemoteServer]. */
  val dataTypeDescriptor: DataTypeDescriptor

  /**
   * Serializer used by the ChronicleService / RemoteRouter to translate between RemoteEntity
   * instances and domain-specific [WrappedEntities]
   * [com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity] of type [T].
   */
  val serializer: Serializer<T>

  /**
   * It is not required that a RemoteServer serve local requests, thus this method is defaulted. And
   * an error is thrown in an attempt to signal to the user/caller that if local-to-the-server
   * connections are required, then this method must be overridden.
   */
  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
    throw NotImplementedError("RemoteServers only serve remote connections.")
}
