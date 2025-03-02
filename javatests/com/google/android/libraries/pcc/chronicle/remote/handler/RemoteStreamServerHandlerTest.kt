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
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RemoteStreamServerHandlerTest {
  private val serializer =
    mock<Serializer<Foo>> {
      on { serialize(any<WrappedEntity<Foo>>()) } doAnswer
        {
          RemoteEntity((it.arguments[0] as WrappedEntity<*>).metadata)
        }
      on { deserialize<Foo>(any()) } doAnswer
        {
          val remoteEntity = it.arguments[0] as RemoteEntity
          WrappedEntity(remoteEntity.metadata, Foo(remoteEntity.metadata.id))
        }
    }
  private val server = mock<RemoteStreamServer<Foo>> { on { serializer } doReturn serializer }

  @Test
  fun handle_publish_noEntities() = runBlocking {
    val req =
      StreamRequest.newBuilder()
        .setDataTypeName("Foo")
        .setOperation(StreamRequest.Operation.PUBLISH)
        .build()
    val handler = RemoteStreamServerHandler(req, server)
    val callback = mock<IResponseCallback>()

    handler.handle(POLICY, emptyList(), callback)

    verifyNoMoreInteractions(server)
    verify(callback).onComplete()
    verifyNoMoreInteractions(callback)
  }

  @Test
  fun handle_publish_delegatesToServer() = runBlocking {
    val req =
      StreamRequest.newBuilder()
        .setDataTypeName("Foo")
        .setOperation(StreamRequest.Operation.PUBLISH)
        .build()
    val handler = RemoteStreamServerHandler(req, server)
    val callback = mock<IResponseCallback>()

    handler.handle(
      POLICY,
      listOf(
        Foo.asRemoteEntity("sundar"),
        Foo.asRemoteEntity("larry"),
        Foo.asRemoteEntity("sergey"),
      ),
      callback,
    )

    verify(server, times(1)).serializer
    verify(server, times(1))
      .publish(
        eq(POLICY),
        eq(listOf(Foo("sundar").wrap(), Foo("larry").wrap(), Foo("sergey").wrap())),
      )
    verify(serializer, times(3)).deserialize<Foo>(any())
    verify(callback).onComplete()
    verifyNoMoreInteractions(callback)
  }

  @Test
  fun handle_publish_doesNotCompleteUntilAfterDelegationCompletes() = runBlocking {
    val req =
      StreamRequest.newBuilder()
        .setDataTypeName("Foo")
        .setOperation(StreamRequest.Operation.PUBLISH)
        .build()
    val publishShouldFinish = CompletableDeferred<Unit>()
    val wrappedServer =
      object : RemoteStreamServer<Foo> by server {
        override suspend fun publish(policy: Policy?, entities: List<WrappedEntity<Foo>>) {
          server.publish(policy, entities)
          publishShouldFinish.await()
        }
      }
    val handler = RemoteStreamServerHandler(req, wrappedServer)
    val callback = mock<IResponseCallback>()

    val handleJob = launch {
      handler.handle(POLICY, listOf(Foo.asRemoteEntity("sundar")), callback)
    }
    // At this point the onComplete callback method shouldn't have been called yet, because the
    // publish method won't have been finished.
    verify(callback, never()).onComplete()

    // Complete the deferred so that the wrapped server can finish and allow the handler to call
    // `onComplete` on the callback.
    publishShouldFinish.complete(Unit)
    handleJob.join()

    // Verify that the callback has been called.
    verify(callback).onComplete()
  }

  @Test
  fun handle_subscribe_delegatesToServer_proxiesToCallback() = runBlocking {
    val req =
      StreamRequest.newBuilder()
        .setDataTypeName("Foo")
        .setOperation(StreamRequest.Operation.SUBSCRIBE)
        .build()
    val handler = RemoteStreamServerHandler(req, server)
    val captor = argumentCaptor<RemoteResponse>()
    val callback = mock<IResponseCallback.Stub> { on { onData(captor.capture()) }.doAnswer {} }
    whenever(server.subscribe(any()))
      .thenReturn(flowOf(listOf(Foo("sundar").wrap(), Foo("larry").wrap(), Foo("sergey").wrap())))

    handler.handle(POLICY, emptyList(), callback)

    assertThat(captor.allValues).hasSize(1)
    assertThat(captor.firstValue.entities).hasSize(3)
    assertThat(captor.firstValue.extras.isEmpty).isTrue()
    assertThat(captor.firstValue.metadata.count).isEqualTo(0)
    assertThat(captor.firstValue.entities[0].metadata.id).isEqualTo("sundar")
    assertThat(captor.firstValue.entities[1].metadata.id).isEqualTo("larry")
    assertThat(captor.firstValue.entities[2].metadata.id).isEqualTo("sergey")

    verify(server).subscribe(eq(POLICY))
    verify(callback).onData(any())
    verifyNoMoreInteractions(callback)
  }

  @Test
  fun handle_unspecified_throws() = runBlocking {
    val req = StreamRequest.newBuilder().setDataTypeName("Foo").build()
    val handler = RemoteStreamServerHandler(req, server)

    val e = assertFailsWith<RemoteError> { handler.handle(POLICY, emptyList(), mock()) }

    assertThat(e.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.UNSUPPORTED)
  }

  @Test
  fun handle_unrecognized_throws() = runBlocking {
    val req = StreamRequest.newBuilder().setDataTypeName("Foo").setOperationValue(1000).build()
    val handler = RemoteStreamServerHandler(req, server)

    val e = assertFailsWith<RemoteError> { handler.handle(POLICY, emptyList(), mock()) }

    assertThat(e.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.UNSUPPORTED)
  }

  data class Foo(val name: String) {
    fun wrap(): WrappedEntity<Foo> =
      WrappedEntity(EntityMetadata.newBuilder().setId(name).build(), this)

    companion object {
      fun asRemoteEntity(name: String): RemoteEntity {
        return RemoteEntity(EntityMetadata.newBuilder().setId(name).build())
      }
    }
  }

  companion object {
    private val POLICY = policy("FooPolicy", "Testing")
  }
}
