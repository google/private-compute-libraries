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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.ComputeRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata.RequestTypeCase
import com.google.android.libraries.pcc.chronicle.api.remote.StoreRequest
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteComputeServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class RemoteServerHandlerFactoryTest {
  private val impl = RemoteServerHandlerFactory()

  @Test
  fun buildServerHandler_store() {
    val server = mock<RemoteStoreServer<*>>()
    val metadata =
      RemoteRequestMetadata.newBuilder().setStore(StoreRequest.getDefaultInstance()).build()

    val result = impl.buildServerHandler(metadata, server)

    assertThat(result).isInstanceOf(RemoteStoreServerHandler::class.java)
  }

  @Test
  fun buildServerHandler_stream() {
    val server = mock<RemoteStreamServer<*>>()
    val metadata =
      RemoteRequestMetadata.newBuilder().setStream(StreamRequest.getDefaultInstance()).build()

    val result = impl.buildServerHandler(metadata, server)

    assertThat(result).isInstanceOf(RemoteStreamServerHandler::class.java)
  }

  @Test
  fun buildServerHandler_compute() {
    val server = mock<RemoteComputeServer<*, *>>()
    val metadata =
      RemoteRequestMetadata.newBuilder().setCompute(ComputeRequest.getDefaultInstance()).build()

    val result = impl.buildServerHandler(metadata, server)

    assertThat(result).isInstanceOf(RemoteComputeServerHandler::class.java)
  }

  @Test
  fun buildServerHandler_computeButServerTypeIsNot_throws() {
    val server = mock<RemoteStreamServer<*>>()
    val metadata =
      RemoteRequestMetadata.newBuilder().setCompute(ComputeRequest.getDefaultInstance()).build()

    val e = assertFailsWith<RemoteError> { impl.buildServerHandler(metadata, server) }

    assertThat(e.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.UNSUPPORTED)
    assertThat(e.metadata.message)
      .contains("Server does not handle request type [${RequestTypeCase.COMPUTE}]")
  }

  @Test
  fun buildServerHandler_notSet() {
    val server = mock<RemoteServer<*>>()
    val metadata = RemoteRequestMetadata.getDefaultInstance()

    val e = assertFailsWith<RemoteError> { impl.buildServerHandler(metadata, server) }

    assertThat(e.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.UNSUPPORTED)
    assertThat(e.metadata.message).contains("Request type not specified, or unrecognized")
  }
}
