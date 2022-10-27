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
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExtensionsTest {
  @Test
  fun sendEachPage_callsCallbackOnData_forEachPage() = runBlocking {
    val captor = argumentCaptor<RemoteResponse>()
    val callback = mock<IResponseCallback.Stub> { on { onData(captor.capture()) } doAnswer { } }
    val pages =
      listOf(
        listOf(Foo("sundar").wrap()),
        listOf(Foo("larry").wrap(), Foo("sergey").wrap())
      )
    val pageFlow = pages.asFlow()

    pageFlow.sendEachPage(callback, FooSerializer).collect()

    val captured = captor.allValues
    assertThat(captured).hasSize(2)
    assertThat(captured[0].entities).hasSize(1)
    assertThat(captured[0].entities[0].metadata).isEqualTo(pages[0][0].metadata)
    assertThat(captured[1].entities).hasSize(2)
    assertThat(captured[1].entities[0].metadata).isEqualTo(pages[1][0].metadata)
    assertThat(captured[1].entities[1].metadata).isEqualTo(pages[1][1].metadata)
  }

  data class Foo(val name: String) {
    fun wrap() = WrappedEntity(EntityMetadata.newBuilder().setId(name).build(), this)
  }
  object FooSerializer : Serializer<Foo> {
    override fun <P : Foo> serialize(wrappedEntity: WrappedEntity<P>) =
      RemoteEntity(wrappedEntity.metadata)
    @Suppress("UNCHECKED_CAST")
    override fun <P : Foo> deserialize(remoteEntity: RemoteEntity): WrappedEntity<P> {
      return WrappedEntity(remoteEntity.metadata, Foo(remoteEntity.metadata.id) as P)
    }
  }
}
