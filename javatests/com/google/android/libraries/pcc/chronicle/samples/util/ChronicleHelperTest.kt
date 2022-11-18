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

package com.google.android.libraries.pcc.chronicle.samples.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.flags.Flags
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.ICancellationSignal
import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.StoreRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.ProtoSerializer
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PERSON_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PeopleReader
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PeopleWriter
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.Person
import com.google.android.libraries.pcc.chronicle.samples.policy.peopleproto.PEOPLE_PROTO_POLICY
import com.google.protobuf.Empty
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import java.time.Duration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.test.assertFails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChronicleHelperTest {
  private val reader = PeopleReaderImpl()
  private val writer = PeopleWriterImpl()
  private val readerSpy = spy(reader)
  private val writerSpy = spy(writer)
  private val peopleServer = spy(PeopleServer())
  private val connectionProviders = setOf(peopleServer)
  private val servers = setOf(peopleServer)
  private val policies = setOf(PEOPLE_PROTO_POLICY)

  private val helper = ChronicleHelper(policies, connectionProviders, servers)

  @Test
  fun getChronicle_returnsWorkingChronicle(): Unit = runBlocking {
    val chronicle = helper.chronicle
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes = setOf(PeopleReader::class.java)
      }

    val connection =
      chronicle.getConnectionOrThrow(
        ConnectionRequest(PeopleReader::class.java, processorNode, PEOPLE_PROTO_POLICY)
      )
    connection.fetchAll()

    verify(readerSpy).fetchAll()
  }

  @Test
  fun setFlags_updatesFlagsInChronicle(): Unit = runBlocking {
    val chronicle = helper.chronicle
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes = setOf(PeopleReader::class.java)
      }

    helper.setFlags(Flags(failNewConnections = true))

    assertFails {
      chronicle.getConnectionOrThrow(
        ConnectionRequest(PeopleReader::class.java, processorNode, PEOPLE_PROTO_POLICY)
      )
    }
  }

  @Test
  fun createRemoteConnectionBinder_createsWorkingRemoteRouter() {
    // Arrange:
    // Have a ChronicleHelper create a remote connection binder, and prepare a "count" remote
    // request for the Person data type.
    val helper =
      ChronicleHelper(
        policies = setOf(PEOPLE_PROTO_POLICY),
        connectionProviders = emptySet(),
        remoteServers = setOf(peopleServer)
      )
    val binder: IRemote =
      helper.createRemoteConnectionBinder(
        ApplicationProvider.getApplicationContext(),
        CoroutineScope(SupervisorJob())
      )
    val request =
      RemoteRequest(
        RemoteRequestMetadata.newBuilder()
          .setStore(
            StoreRequest.newBuilder()
              .setCount(Empty.getDefaultInstance())
              .setDataTypeName(PERSON_GENERATED_DTD.name)
              .build()
          )
          .setUsageType(PEOPLE_PROTO_POLICY.name)
          .build()
      )

    runBlocking {
      // Act:
      // Call binder.serve, and suspend the coroutine until the callback's onComplete is triggered.
      suspendCancellableCoroutine { cont ->
        val callback =
          object : IResponseCallback.Stub() {
            override fun onData(data: RemoteResponse?) = Unit
            override fun provideCancellationSignal(signal: ICancellationSignal?) = Unit

            override fun onError(error: RemoteError?) {
              cont.resumeWithException(error!!)
            }

            override fun onComplete() {
              cont.resume(Unit)
            }
          }
        binder.serve(request, callback)
      }

      // Assert:
      // Assert that the IBinder returned from the helper actually called-through to the
      // peopleServer's `count` method in response to the request we sent it.
      verify(peopleServer).count(any())
    }
  }

  open inner class PeopleServer : RemoteStoreServer<Person> {
    override val dataType: DataType =
      ManagedDataType(
        PERSON_GENERATED_DTD,
        ManagementStrategy.Stored(false, StorageMedia.MEMORY, Duration.ofHours(5)),
        setOf(
          PeopleReader::class.java,
          PeopleWriter::class.java,
        )
      )
    override val dataTypeDescriptor: DataTypeDescriptor = dataType.descriptor
    override val serializer: Serializer<Person> =
      ProtoSerializer.createFrom(Person.getDefaultInstance())

    override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
      return when (connectionRequest.connectionType) {
        PeopleReader::class.java -> readerSpy
        PeopleWriter::class.java -> writerSpy
        else -> throw IllegalArgumentException()
      }
    }

    override suspend fun count(policy: Policy?): Int = 42
    override fun fetchById(policy: Policy?, ids: List<String>): Flow<List<WrappedEntity<Person>>> =
      emptyFlow()
    override fun fetchAll(policy: Policy?): Flow<List<WrappedEntity<Person>>> = emptyFlow()
    override suspend fun create(policy: Policy?, wrappedEntities: List<WrappedEntity<Person>>) =
      Unit
    override suspend fun update(policy: Policy?, wrappedEntities: List<WrappedEntity<Person>>) =
      Unit
    override suspend fun deleteAll(policy: Policy?) = Unit
    override suspend fun deleteById(policy: Policy?, ids: List<String>) = Unit
  }

  open class PeopleReaderImpl : PeopleReader {
    override suspend fun fetchAll(): List<Person> = emptyList()
  }

  open class PeopleWriterImpl : PeopleWriter {
    override suspend fun putPerson(person: Person) = Unit
    override suspend fun deletePerson(name: String) = Unit
    override suspend fun deleteAll() = Unit
  }
}
