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
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponseMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StoreRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import kotlinx.coroutines.flow.collect

/**
 * Implementation of [RemoteServerHandler] which can handle a [StoreRequest]-typed [RemoteRequest]
 * by delegating to a [RemoteStoreServer].
 *
 * **Note:** This class is an internal helper for the RemoteRouter. It is not intended to be used
 * directly by clients of Chronicle (neither data stewards nor feature developers).
 */
class RemoteStoreServerHandler<T : Any>(
  private val request: StoreRequest,
  private val server: RemoteStoreServer<T>,
) : RemoteServerHandler {
  override suspend fun handle(
    policy: Policy?,
    input: List<RemoteEntity>,
    callback: IResponseCallback,
  ) {
    when (request.operationCase) {
      StoreRequest.OperationCase.COUNT -> count(policy, callback)
      StoreRequest.OperationCase.FETCH_ALL -> fetchAll(policy, callback)
      StoreRequest.OperationCase.FETCH_BY_ID ->
        fetchById(policy, request.fetchById.idsList, callback)
      StoreRequest.OperationCase.DELETE_ALL -> deleteAll(policy)
      StoreRequest.OperationCase.DELETE_BY_ID -> deleteById(policy, request.deleteById.idsList)
      StoreRequest.OperationCase.CREATE -> create(policy, input)
      StoreRequest.OperationCase.UPDATE -> update(policy, input)
      StoreRequest.OperationCase.OPERATION_NOT_SET,
      null ->
        throw RemoteError(
          RemoteErrorMetadata.newBuilder()
            .setErrorType(RemoteErrorMetadata.Type.UNSUPPORTED)
            .setMessage("Invalid request: store operation not set - ${request.operationCase}")
            .build()
        )
    }
  }

  private suspend fun count(policy: Policy?, callback: IResponseCallback) {
    callback.onData(
      RemoteResponse(
        metadata = RemoteResponseMetadata.newBuilder().setCount(server.count(policy)).build()
      )
    )
  }

  private suspend fun fetchAll(policy: Policy?, callback: IResponseCallback) {
    server.fetchAll(policy).sendEachPage(callback, server.serializer).collect()
  }

  private suspend fun fetchById(policy: Policy?, ids: List<String>, callback: IResponseCallback) {
    server.fetchById(policy, ids).sendEachPage(callback, server.serializer).collect()
  }

  private suspend fun deleteAll(policy: Policy?) {
    server.deleteAll(policy)
  }

  private suspend fun deleteById(policy: Policy?, ids: List<String>) {
    server.deleteById(policy, ids)
  }

  private suspend fun create(policy: Policy?, entities: List<RemoteEntity>) {
    server.create(policy, entities.map(server.serializer::deserialize))
  }

  private suspend fun update(policy: Policy?, entities: List<RemoteEntity>) {
    server.update(policy, entities.map(server.serializer::deserialize))
  }
}
