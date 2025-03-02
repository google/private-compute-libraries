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

package com.google.android.libraries.pcc.chronicle.storage.stream

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.ProtoSerializer
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.Person
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.storage.stream.impl.EntityStreamProviderImpl
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManagedEntityStreamServerTest {
  private val serializer = ProtoSerializer.createFrom(Person.getDefaultInstance())
  private val entityStreamProvider = EntityStreamProviderImpl()

  @Test
  fun dataType_isPassThruWithProvidedDtdAndConnectionTypes() {
    val server =
      ManagedEntityStreamServer(
        dataTypeDescriptor = PERSON_DTD,
        serializer = serializer,
        entityStreamProvider = entityStreamProvider,
        localConnectionBuilders =
          mapOf(
            PersonReader::class.java to { _, _ -> PersonReader() },
            PersonWriter::class.java to { _, _ -> PersonWriter() },
          ),
      )

    assertThat(server.dataType.descriptor).isEqualTo(PERSON_DTD)
    assertThat(server.dataType.managementStrategy).isEqualTo(ManagementStrategy.PassThru)
    assertThat(server.dataType.ttl).isEqualTo(Duration.ZERO)
    assertThat(server.dataType.connectionTypes)
      .containsExactly(PersonReader::class.java, PersonWriter::class.java)
  }

  @Test
  fun getConnection_localConnectionType() {
    var passedRequest: ConnectionRequest<*>? = null
    var passedStream: EntityStream<Person>? = null
    val readerBuilder = { req: ConnectionRequest<*>, stream: EntityStream<Person> ->
      passedRequest = req
      passedStream = stream
      PersonReader()
    }
    val server =
      ManagedEntityStreamServer(
        dataTypeDescriptor = PERSON_DTD,
        serializer = serializer,
        entityStreamProvider = entityStreamProvider,
        localConnectionBuilders = mapOf(PersonReader::class.java to readerBuilder),
      )

    val readerRequest = ConnectionRequest(PersonReader::class.java, ProcNode, null)

    val result = server.getConnection(readerRequest)
    assertThat(result).isInstanceOf(PersonReader::class.java)
    assertThat(passedRequest).isEqualTo(readerRequest)
    assertThat(passedStream).isEqualTo(entityStreamProvider.getStream<Person>())
  }

  @Test
  fun getConnection_notFound() {
    val server =
      ManagedEntityStreamServer(
        dataTypeDescriptor = PERSON_DTD,
        serializer = serializer,
        entityStreamProvider = entityStreamProvider,
        localConnectionBuilders = mapOf(PersonReader::class.java to { _, _ -> PersonReader() }),
      )

    val writerRequest = ConnectionRequest(PersonWriter::class.java, ProcNode, null)

    val e = assertFailsWith<IllegalArgumentException> { server.getConnection(writerRequest) }
    assertThat(e).hasMessageThat().contains("No connection builder found")
  }

  @Test
  fun publish_subscribe(): Unit = runBlocking {
    val people =
      listOf(
        listOf(
          WrappedEntity(
            metadata = EntityMetadata.getDefaultInstance(),
            entity = Person.newBuilder().setName("sundar").build(),
          )
        ),
        listOf(
          WrappedEntity(
            metadata = EntityMetadata.getDefaultInstance(),
            entity = Person.newBuilder().setName("larry").build(),
          ),
          WrappedEntity(
            metadata = EntityMetadata.getDefaultInstance(),
            entity = Person.newBuilder().setName("sergey").build(),
          ),
        ),
      )
    val server =
      ManagedEntityStreamServer(
        dataTypeDescriptor = PERSON_DTD,
        serializer = serializer,
        entityStreamProvider = entityStreamProvider,
      )

    val subscriptionResults =
      async(start = CoroutineStart.UNDISPATCHED) {
        server.subscribe(policy = null).take(2).toList()
      }
    server.publish(policy = null, people[0])
    server.publish(policy = null, people[1])
    assertThat(subscriptionResults.await()).isEqualTo(people)
  }

  class PersonReader : ReadConnection

  class PersonWriter : WriteConnection

  object ProcNode : ProcessorNode {
    override val requiredConnectionTypes: Set<Class<out Connection>> = emptySet()
  }

  companion object {
    private val PERSON_DTD = dataTypeDescriptor("Person", Person::class)
  }
}
