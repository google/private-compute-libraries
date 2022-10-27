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
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.StoreRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Empty
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteStoreServerHandlerTest {
  private val serializer =
    object : Serializer<Foo> {
      override fun <P : Foo> serialize(wrappedEntity: WrappedEntity<P>): RemoteEntity =
        RemoteEntity(wrappedEntity.metadata)

      @Suppress("UNCHECKED_CAST")
      override fun <P : Foo> deserialize(remoteEntity: RemoteEntity): WrappedEntity<P> =
        WrappedEntity(remoteEntity.metadata, Foo(remoteEntity.metadata.id) as P)
    }
  private val server = mock<RemoteStoreServer<Foo>> { on { serializer } doReturn serializer }
  private val onDataCaptor = argumentCaptor<RemoteResponse>()
  private val callback =
    mock<IResponseCallback.Stub> { on { onData(onDataCaptor.capture()) } doAnswer {} }

  @Test
  fun handle_countRequest_delegatesToServer_andRespondsViaMetadata(): Unit = runBlocking {
    val req =
      StoreRequest.newBuilder().setDataTypeName("Foo").setCount(Empty.getDefaultInstance()).build()
    val handler = RemoteStoreServerHandler(req, server)
    whenever(server.count(any())).thenReturn(42)

    handler.handle(POLICY, emptyList(), callback)

    verify(callback).onData(any())
    verify(server).count(eq(POLICY))
    verifyNoMoreInteractions(callback)
    val response = onDataCaptor.firstValue
    assertThat(response.metadata.count).isEqualTo(42)
  }

  @Test
  fun handle_fetchAllRequest_delegatesToServer_andRespondsBySendingPages(): Unit = runBlocking {
    val req =
      StoreRequest.newBuilder()
        .setDataTypeName("Foo")
        .setFetchAll(Empty.getDefaultInstance())
        .build()
    val handler = RemoteStoreServerHandler(req, server)
    val data = listOf(listOf(Foo("a")), listOf(Foo("b"), Foo("c")))
    whenever(server.fetchAll(any())).thenReturn(data.map { it.map { d -> d.wrap() } }.asFlow())

    handler.handle(POLICY, emptyList(), callback)

    verify(callback, times(2)).onData(any())
    verify(server).fetchAll(eq(POLICY))
    verifyNoMoreInteractions(callback)
    val responses = onDataCaptor.allValues
    assertThat(responses[0].entities).hasSize(1)
    assertThat(serializer.deserialize<Foo>(responses[0].entities[0])).isEqualTo(Foo("a").wrap())
    assertThat(responses[1].entities).hasSize(2)
    assertThat(serializer.deserialize<Foo>(responses[1].entities[0])).isEqualTo(Foo("b").wrap())
    assertThat(serializer.deserialize<Foo>(responses[1].entities[1])).isEqualTo(Foo("c").wrap())
  }

  @Test
  fun handle_fetchByIdRequest_delegatesToSever_andRespondsBySendingPages(): Unit = runBlocking {
    val req =
      StoreRequest.newBuilder()
        .setDataTypeName("Foo")
        .setFetchById(StoreRequest.IdentifierList.newBuilder().addAllIds(listOf("a", "b")).build())
        .build()
    val handler = RemoteStoreServerHandler(req, server)
    val data = listOf(listOf(Foo("a")), listOf(Foo("b"), Foo("c")))
    whenever(server.fetchById(any(), any()))
      .thenReturn(data.map { it.map { d -> d.wrap() } }.asFlow())

    handler.handle(POLICY, emptyList(), callback)

    verify(callback, times(2)).onData(any())
    verify(server).fetchById(eq(POLICY), eq(listOf("a", "b")))
    verifyNoMoreInteractions(callback)
    val responses = onDataCaptor.allValues
    assertThat(responses[0].entities).hasSize(1)
    assertThat(serializer.deserialize<Foo>(responses[0].entities[0])).isEqualTo(Foo("a").wrap())
    assertThat(responses[1].entities).hasSize(2)
    assertThat(serializer.deserialize<Foo>(responses[1].entities[0])).isEqualTo(Foo("b").wrap())
    assertThat(serializer.deserialize<Foo>(responses[1].entities[1])).isEqualTo(Foo("c").wrap())
  }

  @Test
  fun handle_deleteAllRequest_delegatesToServer_doesNotCallOnData(): Unit = runBlocking {
    val req =
      StoreRequest.newBuilder()
        .setDataTypeName("Foo")
        .setDeleteAll(Empty.getDefaultInstance())
        .build()
    val handler = RemoteStoreServerHandler(req, server)

    handler.handle(POLICY, emptyList(), callback)

    verifyNoMoreInteractions(callback)
    verify(server).deleteAll(eq(POLICY))
  }

  @Test
  fun handle_deleteByIdRequest_delegatesToServer_doesNotCallOnData(): Unit = runBlocking {
    val req =
      StoreRequest.newBuilder()
        .setDataTypeName("Foo")
        .setDeleteById(StoreRequest.IdentifierList.newBuilder().addAllIds(listOf("a", "b")).build())
        .build()
    val handler = RemoteStoreServerHandler(req, server)

    handler.handle(POLICY, emptyList(), callback)

    verifyNoMoreInteractions(callback)
    verify(server).deleteById(eq(POLICY), eq(listOf("a", "b")))
  }

  @Test
  fun handle_createReqeust_delegatesToServer_doesNotCallOnData(): Unit = runBlocking {
    val req =
      StoreRequest.newBuilder().setDataTypeName("Foo").setCreate(Empty.getDefaultInstance()).build()
    val handler = RemoteStoreServerHandler(req, server)
    val data = listOf(Foo("a"), Foo("b"), Foo("c"))

    handler.handle(POLICY, data.map { serializer.serialize(it.wrap()) }, callback)

    verifyNoMoreInteractions(callback)
    verify(server).create(eq(POLICY), eq(data.map { it.wrap() }))
  }

  @Test
  fun handle_updateRequest_delegatesToServer_doesNotCallOnData(): Unit = runBlocking {
    val req =
      StoreRequest.newBuilder().setDataTypeName("Foo").setUpdate(Empty.getDefaultInstance()).build()
    val handler = RemoteStoreServerHandler(req, server)
    val data = listOf(Foo("a"), Foo("b"), Foo("c"))

    handler.handle(POLICY, data.map { serializer.serialize(it.wrap()) }, callback)

    verifyNoMoreInteractions(callback)
    verify(server).update(eq(POLICY), eq(data.map { it.wrap() }))
  }

  @Test
  fun handle_noOperationSet_throwsRemoteError(): Unit = runBlocking {
    val req = StoreRequest.newBuilder().setDataTypeName("Foo").build()
    val handler = RemoteStoreServerHandler(req, server)

    val e = assertFailsWith<RemoteError> { handler.handle(POLICY, emptyList(), callback) }

    assertThat(e.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.UNSUPPORTED)
  }

  data class Foo(val name: String) {
    fun wrap(): WrappedEntity<Foo> =
      WrappedEntity(EntityMetadata.newBuilder().setId(name).build(), this)
  }

  companion object {
    private val POLICY = policy("FooPolicy", "Testing")
  }
}
