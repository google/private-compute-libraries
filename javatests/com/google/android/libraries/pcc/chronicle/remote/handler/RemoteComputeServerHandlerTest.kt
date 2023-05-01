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
import com.google.android.libraries.pcc.chronicle.api.remote.ComputeRequest
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteComputeServer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RemoteComputeServerHandlerTest {
  private val argumentSerializer =
    mock<Serializer<ComputeArgument>> {
      on { deserialize<ComputeArgument>(any()) } doAnswer
        {
          val input = it.arguments[0] as RemoteEntity
          WrappedEntity(input.metadata, ComputeArgument(input.metadata.id))
        }
    }
  private val resultSerializer =
    mock<Serializer<ComputeResult>> {
      on { serialize<ComputeResult>(any()) } doAnswer
        {
          @Suppress("UNCHECKED_CAST") val input = it.arguments[0] as WrappedEntity<ComputeResult>
          RemoteEntity(input.metadata)
        }
    }
  private val computeServer =
    mock<RemoteComputeServer<ComputeArgument, ComputeResult>> {
      on { argumentSerializer } doReturn argumentSerializer
      on { serializer } doReturn resultSerializer
    }

  @Test
  fun handle_callsRunAndSendsEachResultPage(): Unit = runBlocking {
    val req =
      ComputeRequest.newBuilder()
        .addParameterDataTypeNames("ComputeArgument")
        .setResultDataTypeName("ComputeResult")
        .setMethodId(ComputeRequest.MethodId.MOIRAI_CLASSIFY)
        .build()
    val arguments = listOf(ComputeArgument("one"), ComputeArgument("two"))
    val resultPages =
      listOf(
        listOf(ComputeResult("a").wrap(), ComputeResult("b").wrap()),
        listOf(ComputeResult("c").wrap())
      )
    val onDataCaptor = argumentCaptor<RemoteResponse>()
    val callback =
      mock<IResponseCallback.Stub> { on { onData(onDataCaptor.capture()) } doAnswer {} }
    val handler = RemoteComputeServerHandler(req, computeServer)
    whenever(computeServer.run(any(), any(), any())).thenReturn(resultPages.asFlow())

    handler.handle(POLICY, arguments.map { it.toRemoteEntity() }, callback)

    verify(computeServer, times(1))
      .run(
        policy = eq(POLICY),
        method = eq(ComputeRequest.MethodId.MOIRAI_CLASSIFY),
        input = eq(arguments.map { it.wrap() })
      )
    val responses = onDataCaptor.allValues
    assertThat(responses).hasSize(2)
    assertThat(responses[0].entities).hasSize(2)
    assertThat(responses[0].entities[0].metadata.id).isEqualTo("a")
    assertThat(responses[0].entities[1].metadata.id).isEqualTo("b")
    assertThat(responses[1].entities).hasSize(1)
    assertThat(responses[1].entities[0].metadata.id).isEqualTo("c")
  }

  data class ComputeArgument(val foo: String) {
    fun wrap() = WrappedEntity(EntityMetadata.newBuilder().setId(foo).build(), this)
    fun toRemoteEntity() = RemoteEntity(wrap().metadata)
  }
  data class ComputeResult(val bar: String) {
    fun wrap(): WrappedEntity<ComputeResult> =
      WrappedEntity(EntityMetadata.newBuilder().setId(bar).build(), this)
  }

  companion object {
    private val POLICY = policy("Computer", "Testing")
  }
}
