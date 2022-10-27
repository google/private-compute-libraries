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

package com.google.android.libraries.pcc.chronicle.remote.impl

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteComputeServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.android.libraries.pcc.chronicle.remote.RemoteContext

/**
 * Default implementation of [RemoteContext] for use by the ChronicleService and RemoteRouter.
 *
 * For storage and streaming requests, this will return a server matching the type of data being
 * stored/streamed. For compute requests, this will return a server matching the *result* data type.
 */
class RemoteContextImpl(private val servers: Set<RemoteServer<*>>) : RemoteContext {
  override fun findServer(requestMetadata: RemoteRequestMetadata): RemoteServer<*>? {
    return when (requestMetadata.requestTypeCase) {
      RemoteRequestMetadata.RequestTypeCase.STORE -> {
        servers.find {
          it is RemoteStoreServer<*> &&
            it.dataTypeDescriptor.name == requestMetadata.store.dataTypeName
        }
      }
      RemoteRequestMetadata.RequestTypeCase.STREAM -> {
        servers.find {
          it is RemoteStreamServer<*> &&
            it.dataTypeDescriptor.name == requestMetadata.stream.dataTypeName
        }
      }
      RemoteRequestMetadata.RequestTypeCase.COMPUTE -> {
        servers.find {
          it is RemoteComputeServer<*, *> &&
            it.dataTypeDescriptor.name == requestMetadata.compute.resultDataTypeName
        }
      }
      RemoteRequestMetadata.RequestTypeCase.REQUESTTYPE_NOT_SET -> null
    }
  }

  override fun findServers(dtd: DataTypeDescriptor): Collection<RemoteServer<*>> =
    servers.filter { it.dataTypeDescriptor == dtd }
}
