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

package com.google.android.libraries.pcc.chronicle.remote.handler

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import kotlinx.coroutines.flow.collect

/**
 * Implementation of [RemoteServerHandler] which can handle a [StreamRequest]-typed [RemoteRequest]
 * by delegating to an appropriate [RemoteStreamServer].
 *
 * **Note:** This class is an internal helper for the RemoteRouter. It is not intended to be used
 * directly by clients of Chronicle (neither data stewards nor feature developers).
 */
class RemoteStreamServerHandler<T : Any>(
  private val request: StreamRequest,
  private val server: RemoteStreamServer<T>,
) : RemoteServerHandler {
  override suspend fun handle(
    policy: Policy?,
    input: List<RemoteEntity>,
    callback: IResponseCallback,
  ) {
    when (request.operation) {
      StreamRequest.Operation.PUBLISH -> publish(policy, input, callback)
      StreamRequest.Operation.SUBSCRIBE -> subscribe(policy, callback)
      StreamRequest.Operation.UNSPECIFIED, StreamRequest.Operation.UNRECOGNIZED -> {
        throw RemoteError(
          RemoteErrorMetadata.newBuilder()
            .setErrorType(RemoteErrorMetadata.Type.UNSUPPORTED)
            .setMessage("Invalid request: stream operation not set or unrecognized")
            .build()
        )
      }
    }
  }

  private suspend fun publish(
    policy: Policy?,
    entities: List<RemoteEntity>,
    callback: IResponseCallback,
  ) {
    callback.onComplete()
    if (entities.isEmpty()) return
    server.publish(policy, entities.map(server.serializer::deserialize))
  }

  private suspend fun subscribe(policy: Policy?, callback: IResponseCallback) {
    server.subscribe(policy).sendEachPage(callback, server.serializer).collect()
  }
}
