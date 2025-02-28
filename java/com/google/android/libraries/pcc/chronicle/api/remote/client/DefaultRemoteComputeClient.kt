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
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.client.RemoteComputeClient.Parameters
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow

/** Default implementation of [RemoteComputeClient]. */
class DefaultRemoteComputeClient<T : Any>(
  private val dataTypeName: String,
  resultSerializer: Serializer<T>,
  private val transport: Transport,
) : BaseRemoteClient<T>(resultSerializer), RemoteComputeClient<T> {
  override fun <Arg : Any> run(
    policy: Policy?,
    methodId: ComputeRequest.MethodId,
    parameters: Parameters<Arg>,
  ): Flow<WrappedEntity<T>> {
    logcat.v("Compute: run(%s, %s)", methodId, parameters)
    val request =
      RemoteRequest(
        metadata =
          super.buildRequestMetadata(policy) {
            compute =
              ComputeRequest.newBuilder()
                .setMethodId(methodId)
                .setResultDataTypeName(dataTypeName)
                .addParameterDataTypeNames(parameters.dataTypeName)
                .build()
          },
        entities = parameters.arguments.map(parameters.serializer::serialize),
      )
    return transport.serveAsWrappedEntityFlow(request)
  }
}
