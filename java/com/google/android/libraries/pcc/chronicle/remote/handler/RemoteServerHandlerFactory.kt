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

import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteComputeServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer

/**
 * Defines a factory capable of constructing instances of [RemoteServerHandler] given
 * [RemoteRequestMetadata] and a [RemoteServer].
 */
open class RemoteServerHandlerFactory {
  /**
   * Builds a [RemoteServerHandler] for the given [RemoteRequestMetadata] and [server], or throws if
   * the provided server is not an appropriate type to be wrapped in a [RemoteServerHandler].
   */
  open fun buildServerHandler(
    requestMetadata: RemoteRequestMetadata,
    server: RemoteServer<*>,
  ): RemoteServerHandler =
    try {
      when (requestMetadata.requestTypeCase) {
        RemoteRequestMetadata.RequestTypeCase.STORE ->
          RemoteStoreServerHandler(requestMetadata.store, server as RemoteStoreServer<*>)
        RemoteRequestMetadata.RequestTypeCase.STREAM ->
          RemoteStreamServerHandler(requestMetadata.stream, server as RemoteStreamServer<*>)
        RemoteRequestMetadata.RequestTypeCase.COMPUTE ->
          RemoteComputeServerHandler(requestMetadata.compute, server as RemoteComputeServer<*, *>)
        RemoteRequestMetadata.RequestTypeCase.REQUESTTYPE_NOT_SET ->
          throw RemoteError(
            type = RemoteErrorMetadata.Type.UNSUPPORTED,
            message = "Request type not specified, or unrecognized: $requestMetadata."
          )
      }
    } catch (cce: ClassCastException) {
      @Suppress("UnusedException")
      throw RemoteError(
        type = RemoteErrorMetadata.Type.UNSUPPORTED,
        message =
          "Server does not handle request type [${requestMetadata.requestTypeCase}]: $server"
      )
    }
}
