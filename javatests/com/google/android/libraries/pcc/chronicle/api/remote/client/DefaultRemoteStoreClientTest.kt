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
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultRemoteStoreClientTest {
  private val transport = mock<Transport>()
  private val serializer =
    object : Serializer<Foo> {
      override fun <P : Foo> serialize(wrappedEntity: WrappedEntity<P>): RemoteEntity {
        return RemoteEntity(EntityMetadata.newBuilder().setId(wrappedEntity.entity.name).build())
      }

      override fun <P : Foo> deserialize(remoteEntity: RemoteEntity): WrappedEntity<P> {
        @Suppress("UNCHECKED_CAST") // because clearly, it's correct for this test.
        return WrappedEntity(remoteEntity.metadata, Foo(remoteEntity.metadata.id) as P)
      }
    }

  @Test
  fun count_returnsMetadataCount(): Unit = runBlocking {
    val client = DefaultRemoteStoreClient("Foo", serializer, transport)
    val requestCaptor = argumentCaptor<RemoteRequest>()

    whenever(transport.serve(requestCaptor.capture())) doReturn
      flowOf(RemoteResponse(RemoteResponseMetadata.newBuilder().setCount(42).build()))

    val result = client.count(POLICY)
    assertThat(result).isEqualTo(42)

    val request = requestCaptor.firstValue
    assertThat(request.metadata.usageType).isEqualTo("FooPolicy")
    assertThat(request.metadata.store.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.store.hasCount()).isTrue()
  }

  @Test
  fun fetchAll_deserializesAndUnPaginatesResults(): Unit = runBlocking {
    val client = DefaultRemoteStoreClient("Foo", serializer, transport)
    val requestCaptor = argumentCaptor<RemoteRequest>()

    whenever(transport.serve(requestCaptor.capture())) doReturn
      flowOf(
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities = listOf(RemoteEntity(EntityMetadata.newBuilder().setId("one").build()))
        ),
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities =
            listOf(
              RemoteEntity(EntityMetadata.newBuilder().setId("two").build()),
              RemoteEntity(EntityMetadata.newBuilder().setId("three").build()),
            )
        )
      )

    val result = client.fetchAll(POLICY).toList()
    assertThat(result)
      .containsExactly(
        WrappedEntity(EntityMetadata.newBuilder().setId("one").build(), Foo("one")),
        WrappedEntity(EntityMetadata.newBuilder().setId("two").build(), Foo("two")),
        WrappedEntity(EntityMetadata.newBuilder().setId("three").build(), Foo("three")),
      )
      .inOrder()

    val request = requestCaptor.firstValue
    assertThat(request.metadata.usageType).isEqualTo("FooPolicy")
    assertThat(request.metadata.store.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.store.hasFetchAll()).isTrue()
  }

  @Test
  fun fetchById_deserializesAndUnPaginatesResults(): Unit = runBlocking {
    val client = DefaultRemoteStoreClient("Foo", serializer, transport)
    val requestCaptor = argumentCaptor<RemoteRequest>()

    whenever(transport.serve(requestCaptor.capture())) doReturn
      flowOf(
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities = listOf(RemoteEntity(EntityMetadata.newBuilder().setId("one").build()))
        ),
        RemoteResponse(
          metadata = RemoteResponseMetadata.getDefaultInstance(),
          entities =
            listOf(
              RemoteEntity(EntityMetadata.newBuilder().setId("two").build()),
              RemoteEntity(EntityMetadata.newBuilder().setId("three").build()),
            )
        )
      )

    val result = client.fetchById(POLICY, listOf("one", "two", "three")).toList()
    assertThat(result)
      .containsExactly(
        WrappedEntity(EntityMetadata.newBuilder().setId("one").build(), Foo("one")),
        WrappedEntity(EntityMetadata.newBuilder().setId("two").build(), Foo("two")),
        WrappedEntity(EntityMetadata.newBuilder().setId("three").build(), Foo("three")),
      )
      .inOrder()

    val request = requestCaptor.firstValue
    assertThat(request.metadata.usageType).isEqualTo("FooPolicy")
    assertThat(request.metadata.store.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.store.fetchById.idsList)
      .containsExactlyElementsIn(listOf("one", "two", "three"))
  }

  @Test
  fun deleteAll_sendsRequest_collectsResponse(): Unit = runBlocking {
    val client = DefaultRemoteStoreClient("Foo", serializer, transport)
    val requestCaptor = argumentCaptor<RemoteRequest>()
    var collected = false
    whenever(transport.serve(requestCaptor.capture())) doReturn flow { collected = true }

    client.deleteAll(policy = null)
    assertThat(collected).isTrue()

    val request = requestCaptor.firstValue
    assertThat(request.metadata.usageType).isEmpty()
    assertThat(request.metadata.store.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.store.hasDeleteAll())
  }

  @Test
  fun deleteById_sendsRequest_collectsResponse(): Unit = runBlocking {
    val client = DefaultRemoteStoreClient("Foo", serializer, transport)
    val requestCaptor = argumentCaptor<RemoteRequest>()
    var collected = false
    whenever(transport.serve(requestCaptor.capture())) doReturn flow { collected = true }

    client.deleteById(policy = null, ids = listOf("one", "two", "three"))
    assertThat(collected).isTrue()

    val request = requestCaptor.firstValue
    assertThat(request.metadata.usageType).isEmpty()
    assertThat(request.metadata.store.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.store.deleteById.idsList)
      .containsExactlyElementsIn(listOf("one", "two", "three"))
  }

  @Test
  fun create_sendsRequest_collectsResponse(): Unit = runBlocking {
    val client = DefaultRemoteStoreClient("Foo", serializer, transport)
    val requestCaptor = argumentCaptor<RemoteRequest>()
    var collected = false
    whenever(transport.serve(requestCaptor.capture())) doReturn flow { collected = true }

    client.create(
      policy = null,
      entities =
        listOf(
          WrappedEntity(EntityMetadata.newBuilder().setId("one").build(), Foo("one")),
          WrappedEntity(EntityMetadata.newBuilder().setId("two").build(), Foo("two")),
          WrappedEntity(EntityMetadata.newBuilder().setId("three").build(), Foo("three")),
        )
    )
    assertThat(collected).isTrue()

    val request = requestCaptor.firstValue
    assertThat(request.metadata.usageType).isEmpty()
    assertThat(request.metadata.store.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.store.hasCreate()).isTrue()
    assertThat(request.entities[0].metadata.id).isEqualTo("one")
    assertThat(request.entities[1].metadata.id).isEqualTo("two")
    assertThat(request.entities[2].metadata.id).isEqualTo("three")
  }

  @Test
  fun update_sendsRequest_collectsResponse(): Unit = runBlocking {
    val client = DefaultRemoteStoreClient("Foo", serializer, transport)
    val requestCaptor = argumentCaptor<RemoteRequest>()
    var collected = false
    whenever(transport.serve(requestCaptor.capture())) doReturn flow { collected = true }

    client.update(
      policy = null,
      entities =
        listOf(
          WrappedEntity(EntityMetadata.newBuilder().setId("one").build(), Foo("one")),
          WrappedEntity(EntityMetadata.newBuilder().setId("two").build(), Foo("two")),
          WrappedEntity(EntityMetadata.newBuilder().setId("three").build(), Foo("three")),
        )
    )
    assertThat(collected).isTrue()

    val request = requestCaptor.firstValue
    assertThat(request.metadata.usageType).isEmpty()
    assertThat(request.metadata.store.dataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.store.hasUpdate()).isTrue()
    assertThat(request.entities[0].metadata.id).isEqualTo("one")
    assertThat(request.entities[1].metadata.id).isEqualTo("two")
    assertThat(request.entities[2].metadata.id).isEqualTo("three")
  }

  data class Foo(val name: String)

  companion object {
    private val POLICY = policy("FooPolicy", "Testing")
  }
}
