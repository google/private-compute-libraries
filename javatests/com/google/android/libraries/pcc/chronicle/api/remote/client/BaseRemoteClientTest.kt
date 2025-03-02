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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponseMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class BaseRemoteClientTest {
  private val serializer = mock<Serializer<Foo>>()

  @Test
  fun buildRequestMetadata_nullPolicy_leavesUsageTypeEmpty() {
    val client = Impl()

    val metadata = client.callBuildRequestMetadata(null) {}

    assertThat(metadata.usageType).isEmpty()
  }

  @Test
  fun buildRequestMetadata_nonNullPolicy_usesPolicyIdAsUsageType() {
    val client = Impl()

    val metadata = client.callBuildRequestMetadata(policy("MyPolicyId", "Testing") {}) {}

    assertThat(metadata.usageType).isEqualTo("MyPolicyId")
  }

  @Test
  fun buildRequestMetadata_callsBlock() {
    val client = Impl()

    val metadata =
      client.callBuildRequestMetadata(null) { stream = StreamRequest.getDefaultInstance() }

    assertThat(metadata.stream).isEqualTo(StreamRequest.getDefaultInstance())
  }

  @Test
  fun serveAsWrappedEntityFlow_deserializesAndUnpaginatesCollectedResponses(): Unit = runBlocking {
    val responses =
      listOf(
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities =
            listOf(
              RemoteEntity(metadata = EntityMetadata.newBuilder().setId("1").build()),
              RemoteEntity(metadata = EntityMetadata.newBuilder().setId("2").build()),
              RemoteEntity(metadata = EntityMetadata.newBuilder().setId("3").build()),
            ),
        ),
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities =
            listOf(
              RemoteEntity(metadata = EntityMetadata.newBuilder().setId("4").build()),
              RemoteEntity(metadata = EntityMetadata.newBuilder().setId("5").build()),
            ),
        ),
      )
    val transport =
      object : Transport {
        override fun serve(request: RemoteRequest): Flow<RemoteResponse> = responses.asFlow()
      }
    val impl = Impl()
    whenever(serializer.deserialize<Foo>(any())).then {
      val arg = it.arguments[0] as RemoteEntity
      WrappedEntity(arg.metadata, Foo(arg.metadata.id))
    }

    val received =
      impl
        .callServeAsWrappedEntityFlow(
          transport,
          RemoteRequest(RemoteRequestMetadata.getDefaultInstance()),
        )
        .toList()

    assertThat(received)
      .containsExactly(
        WrappedEntity(EntityMetadata.newBuilder().setId("1").build(), Foo("1")),
        WrappedEntity(EntityMetadata.newBuilder().setId("2").build(), Foo("2")),
        WrappedEntity(EntityMetadata.newBuilder().setId("3").build(), Foo("3")),
        WrappedEntity(EntityMetadata.newBuilder().setId("4").build(), Foo("4")),
        WrappedEntity(EntityMetadata.newBuilder().setId("5").build(), Foo("5")),
      )
      .inOrder()
  }

  data class Foo(val name: String)

  private inner class Impl : BaseRemoteClient<Foo>(serializer) {
    fun callBuildRequestMetadata(
      policy: Policy?,
      block: RemoteRequestMetadata.Builder.() -> Unit,
    ): RemoteRequestMetadata = buildRequestMetadata(policy, block)

    fun callServeAsWrappedEntityFlow(
      transport: Transport,
      request: RemoteRequest,
    ): Flow<WrappedEntity<Foo>> = transport.serveAsWrappedEntityFlow(request)
  }
}
