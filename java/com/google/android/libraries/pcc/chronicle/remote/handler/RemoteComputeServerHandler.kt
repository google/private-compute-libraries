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
import com.google.android.libraries.pcc.chronicle.api.remote.ComputeRequest
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteComputeServer
import kotlinx.coroutines.flow.collect

/**
 * Implementation of a [RemoteServerHandler] which handles [ComputeRequest]-typed [RemoteRequest]
 * objects by delegating to a [RemoteComputeServer].
 *
 * **Note:** This class is an internal helper for the RemoteRouter. It is not intended to be used
 * directly by clients of Chronicle (neither data stewards nor feature developers).
 */
class RemoteComputeServerHandler<In : Any, Out : Any>(
  private val request: ComputeRequest,
  private val server: RemoteComputeServer<In, Out>,
) : RemoteServerHandler {
  override suspend fun handle(
    policy: Policy?,
    input: List<RemoteEntity>,
    callback: IResponseCallback,
  ) {
    server
      .run(policy, request.methodId, input.map(server.argumentSerializer::deserialize))
      .sendEachPage(callback, server.serializer)
      .collect()
  }
}
