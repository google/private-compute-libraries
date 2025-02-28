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

package com.google.android.libraries.pcc.chronicle.api.remote.client

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.ComputeRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow

/** Defines a [RemoteClient] intended for communicating with a [RemoteComputeServer]. */
interface RemoteComputeClient<T : Any> : RemoteClient<T> {
  /** Compute: Computes results given [parameters] using a method identified by a [methodId]. */
  fun <Arg : Any> run(
    policy: Policy?,
    methodId: ComputeRequest.MethodId,
    parameters: Parameters<Arg>,
  ): Flow<WrappedEntity<T>>

  /** Represents parameters passed to a RemoteComputeServer. */
  data class Parameters<Arg : Any>(
    /** The name of the DataTypeDescriptor representing the [arguments]. */
    val dataTypeName: String,
    /**
     * A [Serializer] capable of transforming [WrappedEntity] instances of type [Arg] into
     * [RemoteEntities] for transmission to the server.
     */
    val serializer: Serializer<Arg>,
    /** The arguments to provide to the RemoteComputeServer when running a method. */
    val arguments: List<WrappedEntity<Arg>>,
  )
}
