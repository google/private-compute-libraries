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
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponseMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultRemoteStreamClientTest {
  private val transport = mock<Transport>()
  private val serializer = mock<Serializer<Foo>>()

  @Test
  fun publish_collectsTransportResponseFlow(): Unit = runBlocking {
    var collected = false
    whenever(transport.serve(any())).thenAnswer { flow<RemoteResponse> { collected = true } }
    whenever(serializer.serialize<Foo>(any()))
      .thenReturn(RemoteEntity(EntityMetadata.getDefaultInstance()))

    val client = DefaultRemoteStreamClient("Foo", serializer, transport)
    client.publish(
      POLICY,
      listOf(
        WrappedEntity(metadata = EntityMetadata.getDefaultInstance(), entity = Foo("one")),
        WrappedEntity(metadata = EntityMetadata.getDefaultInstance(), entity = Foo("two")),
      )
    )

    assertThat(collected).isTrue()
  }

  @Test
  fun publish_buildsRemoteRequest(): Unit = runBlocking {
    val requestCaptor = argumentCaptor<RemoteRequest>()
    whenever(transport.serve(requestCaptor.capture())).thenAnswer { emptyFlow<RemoteResponse>() }
    whenever(serializer.serialize<Foo>(any()))
      .thenReturn(RemoteEntity(EntityMetadata.getDefaultInstance()))

    val client = DefaultRemoteStreamClient("Foo", serializer, transport)
    client.publish(
      POLICY,
      listOf(
        WrappedEntity(metadata = EntityMetadata.getDefaultInstance(), entity = Foo("one")),
        WrappedEntity(metadata = EntityMetadata.getDefaultInstance(), entity = Foo("two")),
      )
    )

    verify(serializer, times(2)).serialize<Foo>(any())

    val request = requestCaptor.firstValue
    assertThat(request.metadata.stream.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.stream.operation).isEqualTo(StreamRequest.Operation.PUBLISH)
    assertThat(request.entities).hasSize(2)
  }

  @Test
  fun subscribe_buildsRemoteRequest(): Unit = runBlocking {
    val requestCaptor = argumentCaptor<RemoteRequest>()
    whenever(transport.serve(requestCaptor.capture())).thenAnswer { emptyFlow<RemoteResponse>() }
    whenever(serializer.serialize<Foo>(any()))
      .thenReturn(RemoteEntity(EntityMetadata.getDefaultInstance()))

    val client = DefaultRemoteStreamClient("Foo", serializer, transport)
    client.subscribe(POLICY).collect {}

    val request = requestCaptor.firstValue
    assertThat(request.metadata.stream.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.stream.operation).isEqualTo(StreamRequest.Operation.SUBSCRIBE)
    assertThat(request.entities).isEmpty()
  }

  @Test
  fun subscribe_servesItemsNotPages(): Unit = runBlocking {
    var calls = 0
    val responses =
      listOf(
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities = listOf(RemoteEntity(EntityMetadata.getDefaultInstance()))
        ),
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities =
            listOf(
              RemoteEntity(EntityMetadata.getDefaultInstance()),
              RemoteEntity(EntityMetadata.getDefaultInstance()),
              RemoteEntity(EntityMetadata.getDefaultInstance()),
            )
        )
      )
    whenever(transport.serve(any())).thenAnswer { responses.asFlow() }
    whenever(serializer.deserialize<Foo>(any())).thenAnswer {
      WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("${calls++}"))
    }

    val client = DefaultRemoteStreamClient("Foo", serializer, transport)
    val received = client.subscribe(POLICY).toList()
    assertThat(received)
      .containsExactly(
        WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("0")),
        WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("1")),
        WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("2")),
        WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("3")),
      )
      .inOrder()
  }

  data class Foo(val name: String)

  companion object {
    private val POLICY = policy("Foos", "Testing") {}
  }
}
