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
import com.google.android.libraries.pcc.chronicle.api.remote.ComputeRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponseMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultRemoteComputeClientTest {
  private val transport = mock<Transport> {
    on { serve(any()) } doReturn emptyFlow()
  }
  private val serializer = mock<Serializer<Foo>> {
    on { deserialize<Foo>(any()) } doAnswer {
      val arg = it.arguments[0] as RemoteEntity
      WrappedEntity(EntityMetadata.getDefaultInstance(), Foo(arg.metadata.id))
    }
  }
  private val parameterSerializer = mock<Serializer<Parameter>> {
    on { serialize<Parameter>(any()) } doAnswer {
      @Suppress("UNCHECKED_CAST")
      val arg = it.arguments[0] as WrappedEntity<Parameter>
      RemoteEntity(EntityMetadata.newBuilder().setId(arg.entity.name).build())
    }
  }

  @Test
  fun run_buildsRemoteRequest(): Unit = runBlocking {
    val client = DefaultRemoteComputeClient("Foo", serializer, transport)
    val captor = argumentCaptor<RemoteRequest>()
    whenever(transport.serve(captor.capture())) doReturn emptyFlow()

    client.run(
      policy = null,
      methodId = ComputeRequest.MethodId.MOIRAI_CLASSIFY,
      parameters = RemoteComputeClient.Parameters(
        dataTypeName = "Parameter",
        serializer = parameterSerializer,
        arguments = listOf(
          WrappedEntity(EntityMetadata.getDefaultInstance(), Parameter("one")),
          WrappedEntity(EntityMetadata.getDefaultInstance(), Parameter("two")),
        )
      )
    ).toList()

    val request = captor.firstValue

    assertThat(request.metadata.compute.methodId).isEqualTo(ComputeRequest.MethodId.MOIRAI_CLASSIFY)
    assertThat(request.metadata.compute.resultDataTypeName).isEqualTo("Foo")
    assertThat(request.metadata.compute.parameterDataTypeNamesList).containsExactly("Parameter")
    assertThat(request.entities[0].metadata)
      .isEqualTo(EntityMetadata.newBuilder().setId("one").build())
    assertThat(request.entities[1].metadata)
      .isEqualTo(EntityMetadata.newBuilder().setId("two").build())
  }

  @Test
  fun run_deserializesAndUnPaginatesResults(): Unit = runBlocking {
    val client = DefaultRemoteComputeClient("Foo", serializer, transport)
    val remoteResponses = listOf(
      RemoteResponse(
        RemoteResponseMetadata.getDefaultInstance(),
        entities = listOf(
          RemoteEntity(EntityMetadata.newBuilder().setId("one").build()),
          RemoteEntity(EntityMetadata.newBuilder().setId("two").build()),
        )
      ),
      RemoteResponse(
        RemoteResponseMetadata.getDefaultInstance(),
        entities = listOf(
          RemoteEntity(EntityMetadata.newBuilder().setId("three").build()),
        )
      )
    )
    whenever(transport.serve(any())) doReturn remoteResponses.asFlow()

    val result = client.run(
      policy = null,
      methodId = ComputeRequest.MethodId.MOIRAI_CLASSIFY,
      parameters = RemoteComputeClient.Parameters(
        dataTypeName = "Parameter",
        serializer = parameterSerializer,
        arguments = emptyList()
      )
    ).toList()

    assertThat(result)
      .containsExactly(
        WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("one")),
        WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("two")),
        WrappedEntity(EntityMetadata.getDefaultInstance(), Foo("three")),
      )
  }

  data class Foo(val name: String)
  data class Parameter(val name: String)
}
