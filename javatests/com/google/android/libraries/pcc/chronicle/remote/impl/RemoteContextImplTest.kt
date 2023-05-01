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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.remote.ComputeRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StoreRequest
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteComputeServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class RemoteContextImplTest {
  private val fooDtd = dataTypeDescriptor("Foo", Foo::class)
  private val barDtd = dataTypeDescriptor("Bar", Bar::class)
  private val storeServer1 =
    mock<RemoteStoreServer<Foo>> { on { dataTypeDescriptor } doReturn fooDtd }
  private val storeServer2 =
    mock<RemoteStoreServer<Bar>> { on { dataTypeDescriptor } doReturn barDtd }
  private val streamServer1 =
    mock<RemoteStreamServer<Foo>> { on { dataTypeDescriptor } doReturn fooDtd }
  private val streamServer2 =
    mock<RemoteStreamServer<Bar>> { on { dataTypeDescriptor } doReturn barDtd }
  private val computeServer1 =
    mock<RemoteComputeServer<Bar, Foo>> { on { dataTypeDescriptor } doReturn fooDtd }
  private val computeServer2 =
    mock<RemoteComputeServer<Foo, Bar>> { on { dataTypeDescriptor } doReturn barDtd }

  @Test
  fun findServer_noRequestType_noMatchFound() {
    val impl = RemoteContextImpl(setOf(storeServer1, streamServer1, computeServer1))

    val metadata = RemoteRequestMetadata.newBuilder().setUsageType("Testing").build()

    assertThat(impl.findServer(metadata)).isNull()
  }

  @Test
  fun findServer_store_matchFound() {
    val impl = RemoteContextImpl(setOf(storeServer1, storeServer2, streamServer1, computeServer1))

    val metadata =
      RemoteRequestMetadata.newBuilder()
        .setUsageType("Testing")
        .setStore(StoreRequest.newBuilder().setDataTypeName("Bar").build())
        .build()

    assertThat(impl.findServer(metadata)).isSameInstanceAs(storeServer2)
  }

  @Test
  fun findServer_store_noMatchFound() {
    val impl = RemoteContextImpl(setOf(storeServer1, storeServer2, streamServer1, computeServer1))

    val metadata =
      RemoteRequestMetadata.newBuilder()
        .setUsageType("Testing")
        .setStore(StoreRequest.newBuilder().setDataTypeName("Baz").build())
        .build()

    assertThat(impl.findServer(metadata)).isNull()
  }

  @Test
  fun findServer_stream_matchFound() {
    val impl = RemoteContextImpl(setOf(storeServer1, streamServer1, streamServer2, computeServer1))

    val metadata =
      RemoteRequestMetadata.newBuilder()
        .setUsageType("Testing")
        .setStream(
          StreamRequest.newBuilder()
            .setDataTypeName("Bar")
            .setOperation(StreamRequest.Operation.PUBLISH)
            .build()
        )
        .build()

    assertThat(impl.findServer(metadata)).isSameInstanceAs(streamServer2)
  }

  @Test
  fun findServer_stream_noMatchFound() {
    val impl = RemoteContextImpl(setOf(storeServer1, streamServer1, streamServer2, computeServer1))

    val metadata =
      RemoteRequestMetadata.newBuilder()
        .setUsageType("Testing")
        .setStream(
          StreamRequest.newBuilder()
            .setDataTypeName("Baz")
            .setOperation(StreamRequest.Operation.SUBSCRIBE)
            .build()
        )
        .build()

    assertThat(impl.findServer(metadata)).isNull()
  }

  @Test
  fun findServer_compute_matchFound() {
    val impl = RemoteContextImpl(setOf(storeServer1, streamServer1, computeServer1, computeServer2))

    val metadata =
      RemoteRequestMetadata.newBuilder()
        .setUsageType("Testing")
        .setCompute(
          ComputeRequest.newBuilder()
            .setResultDataTypeName("Bar")
            .addParameterDataTypeNames("Foo")
            .setMethodId(ComputeRequest.MethodId.MOIRAI_CLASSIFY)
            .build()
        )
        .build()

    assertThat(impl.findServer(metadata)).isSameInstanceAs(computeServer2)
  }

  @Test
  fun findServer_compute_noMatchFound() {
    val impl = RemoteContextImpl(setOf(storeServer1, streamServer1, computeServer1, computeServer2))

    val metadata =
      RemoteRequestMetadata.newBuilder()
        .setUsageType("Testing")
        .setCompute(
          ComputeRequest.newBuilder()
            .setResultDataTypeName("Baz")
            .addParameterDataTypeNames("Blah")
            .setMethodId(ComputeRequest.MethodId.MOIRAI_CLASSIFY)
            .build()
        )
        .build()

    assertThat(impl.findServer(metadata)).isNull()
  }

  @Test
  fun findServers() {
    val impl = RemoteContextImpl(setOf(storeServer1, streamServer1, computeServer1))

    assertThat(impl.findServers(fooDtd))
      .containsExactly(storeServer1, streamServer1, computeServer1)
    assertThat(impl.findServers(barDtd)).isEmpty()
  }

  data class Foo(val name: String)
  data class Bar(val name: String)
}
