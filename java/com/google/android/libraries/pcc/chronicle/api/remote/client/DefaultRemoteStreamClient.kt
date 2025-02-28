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
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/** Default implementation of [RemoteStreamClient]. */
class DefaultRemoteStreamClient<T : Any>(
  private val dataTypeName: String,
  serializer: Serializer<T>,
  private val transport: Transport,
) : BaseRemoteClient<T>(serializer), RemoteStreamClient<T> {
  override suspend fun publish(policy: Policy?, entities: List<WrappedEntity<T>>) {
    logcat.v("Stream: publish(#entities: %d)", entities.size)
    val request =
      RemoteRequest(
        metadata = buildStreamRequestMetadata(policy, StreamRequest.Operation.PUBLISH),
        entities = entities.map(serializer::serialize),
      )
    // Important note: we use the no-argument version of `collect` here so that we don't have to
    // incur an allocation of the lambda every time `publish` is called.
    transport.serve(request).collect()
  }

  override fun subscribe(policy: Policy?): Flow<WrappedEntity<T>> {
    logcat.v("Stream: subscribe()")
    val request =
      RemoteRequest(buildStreamRequestMetadata(policy, StreamRequest.Operation.SUBSCRIBE))
    return transport.serveAsWrappedEntityFlow(request)
  }

  private fun buildStreamRequestMetadata(
    policy: Policy?,
    operation: StreamRequest.Operation,
  ): RemoteRequestMetadata {
    return super.buildRequestMetadata(policy) {
      stream =
        StreamRequest.newBuilder().setDataTypeName(dataTypeName).setOperation(operation).build()
    }
  }
}
