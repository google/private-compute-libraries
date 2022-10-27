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

package com.google.android.libraries.pcc.chronicle.api.remote

/**
 * Extension property to return whether or not the receiving [RemoteRequestMetadata] represents a
 * reading operation.
 */
val RemoteRequestMetadata.isReadRequest: Boolean
  get() =
    when (requestTypeCase) {
      RemoteRequestMetadata.RequestTypeCase.STORE -> when (store.operationCase) {
        StoreRequest.OperationCase.COUNT,
        StoreRequest.OperationCase.FETCH_ALL,
        StoreRequest.OperationCase.FETCH_BY_ID -> true
        StoreRequest.OperationCase.DELETE_ALL,
        StoreRequest.OperationCase.DELETE_BY_ID,
        StoreRequest.OperationCase.CREATE,
        StoreRequest.OperationCase.UPDATE -> false
        StoreRequest.OperationCase.OPERATION_NOT_SET -> false
      }
      RemoteRequestMetadata.RequestTypeCase.STREAM -> when (stream.operation) {
        StreamRequest.Operation.PUBLISH -> false
        StreamRequest.Operation.SUBSCRIBE -> true
        StreamRequest.Operation.UNSPECIFIED,
        StreamRequest.Operation.UNRECOGNIZED -> false
      }
      RemoteRequestMetadata.RequestTypeCase.COMPUTE -> true
      RemoteRequestMetadata.RequestTypeCase.REQUESTTYPE_NOT_SET -> false
    }
