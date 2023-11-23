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

package com.google.android.libraries.pcc.chronicle.api.remote

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.analysis.DefaultChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.DefaultPolicySet
import com.google.android.libraries.pcc.chronicle.analysis.impl.ChroniclePolicyEngine
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.error.PolicyViolation
import com.google.android.libraries.pcc.chronicle.api.flags.FakeFlagsReader
import com.google.android.libraries.pcc.chronicle.api.flags.Flags
import com.google.android.libraries.pcc.chronicle.api.getConnectionOrThrow
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultChronicle
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultDataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.policy.DefaultPolicyConformanceCheck
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.client.AidlTransport
import com.google.android.libraries.pcc.chronicle.api.remote.client.ChronicleServiceConnector
import com.google.android.libraries.pcc.chronicle.api.remote.client.DefaultRemoteStoreClient
import com.google.android.libraries.pcc.chronicle.api.remote.client.DefaultRemoteStreamClient
import com.google.android.libraries.pcc.chronicle.api.remote.client.ManualChronicleServiceConnector
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.ProtoSerializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.RandomProtoGenerator
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.SIMPLE_PROTO_MESSAGE_DTD
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.SimpleProtoMessage
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toProtoTimestamp
import com.google.android.libraries.pcc.chronicle.remote.RemoteRouter
import com.google.android.libraries.pcc.chronicle.remote.handler.RemoteServerHandlerFactory
import com.google.android.libraries.pcc.chronicle.remote.impl.ClientDetailsProviderImpl
import com.google.android.libraries.pcc.chronicle.remote.impl.RemoteContextImpl
import com.google.android.libraries.pcc.chronicle.remote.impl.RemotePolicyCheckerImpl
import com.google.android.libraries.pcc.chronicle.storage.datacache.ManagedDataCache
import com.google.android.libraries.pcc.chronicle.storage.datacache.impl.DataCacheStorageImpl
import com.google.android.libraries.pcc.chronicle.storage.stream.EntityStream
import com.google.android.libraries.pcc.chronicle.storage.stream.EntityStreamProvider
import com.google.android.libraries.pcc.chronicle.storage.stream.ManagedEntityStreamServer
import com.google.android.libraries.pcc.chronicle.storage.stream.impl.EntityStreamProviderImpl
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test exercising client-server remote connections using full chronicle instances in
 * each endpoint and communicating with one-another over in-process gRPC.
 *
 * TODO(b/195135029): Create a JUnit rule to make constructing new e2e tests easier.
 */
@RunWith(AndroidJUnit4::class)
class RemoteStoreIntegrationTest {
  private val protoGenerator = RandomProtoGenerator(System.currentTimeMillis())
  private val serviceConnector = ManualChronicleServiceConnector()

  @Test
  fun clientHasValidPolicy_serverHasInvalidPolicy(): Unit = runBlocking {
    // Initialize our "processes".
    val server = ServerProcess(setOf(INVALID_POLICY_MISSING_FIELDS), enableStoreServer = true)
    serviceConnector.binder = server.router
    val client = ClientProcess(setOf(VALID_POLICY), serviceConnector)

    // Put together a simple processor node we can use to access both connection types.
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> =
          setOf(SimpleProtoMessageWriter::class.java, SimpleProtoMessageReader::class.java)
      }

    // Populate the store from the server.
    val serverWriteConnection =
      server.chronicle.getConnectionOrThrow<SimpleProtoMessageWriter>(processorNode)
    serverWriteConnection.writeMessage(protoGenerator.generateSimpleProtoMessage())

    // Get connection from our client "process"'s Chronicle.
    // - Getting a connection should pass, since at this point the only policy check performed is
    //   on the client.
    val clientReadConnection =
      client.chronicle.getConnectionOrThrow<SimpleProtoMessageReader>(processorNode, VALID_POLICY)

    // Use the connection. This step should fail because the server-side check performed when the
    // connection impl sends a grpc message won't see a policy which allows egress of the full
    // data.
    val error = assertFailsWith<RemoteError> { clientReadConnection.getMessages() }
    assertThat(error.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.POLICY_VIOLATION)
  }

  @Test
  fun clientHasInvalidPolicy_serverHasValidPolicy(): Unit = runBlocking {
    // Initialize our "processes".
    val server = ServerProcess(setOf(VALID_POLICY), enableStoreServer = true)
    serviceConnector.binder = server.router
    val client = ClientProcess(setOf(INVALID_POLICY_MISSING_FIELDS), serviceConnector)

    // Put together a simple processor node we can use to access both connection types.
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> =
          setOf(SimpleProtoMessageWriter::class.java, SimpleProtoMessageReader::class.java)
      }

    // Populate the store from the server.
    val serverWriteConnection =
      server.chronicle.getConnectionOrThrow<SimpleProtoMessageWriter>(processorNode)
    serverWriteConnection.writeMessage(protoGenerator.generateSimpleProtoMessage())

    // Get connection from our client "process"'s Chronicle.
    // - Getting a connection should fail, since at this point we are checking policy at the
    //   client.
    assertFailsWith<PolicyViolation> {
      client.chronicle.getConnectionOrThrow<SimpleProtoMessageReader>(
        processorNode,
        INVALID_POLICY_MISSING_FIELDS
      )
    }
  }

  @Test
  fun clientWrites_clientAndServerRead(): Unit = runBlocking {
    // Initialize our "processes".
    val server = ServerProcess(setOf(VALID_POLICY), enableStoreServer = true)
    serviceConnector.binder = server.router
    val client = ClientProcess(setOf(VALID_POLICY), serviceConnector)

    // Put together a simple processor node we can use to access both connection types.
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> =
          setOf(SimpleProtoMessageWriter::class.java, SimpleProtoMessageReader::class.java)
      }

    // Get connections from our client "process"'s Chronicle
    val clientWriteConnection =
      client.chronicle.getConnectionOrThrow<SimpleProtoMessageWriter>(processorNode, VALID_POLICY)
    val clientReadConnection =
      client.chronicle.getConnectionOrThrow<SimpleProtoMessageReader>(processorNode, VALID_POLICY)

    // Get connection from our server "process"'s Chronicle
    val serverReadConnection =
      server.chronicle.getConnectionOrThrow<SimpleProtoMessageReader>(processorNode, VALID_POLICY)

    // Create some data.
    val messageA = protoGenerator.generateDistinctSimpleProtoMessage()
    val messageB = protoGenerator.generateDistinctSimpleProtoMessage(messageA)

    // Write from the client.
    clientWriteConnection.writeMessage(messageA)
    clientWriteConnection.writeMessage(messageB)

    // Verify that the client and server each have *both* written objects.
    assertThat(clientReadConnection.getMessages()).containsExactly(messageA, messageB)
    assertThat(serverReadConnection.getMessages()).containsExactly(messageA, messageB)
  }

  @Test
  fun serverWrites_clientAndServerRead(): Unit = runBlocking {
    // Initialize our "processes".
    val server = ServerProcess(setOf(VALID_POLICY), enableStoreServer = true)
    serviceConnector.binder = server.router
    val client = ClientProcess(setOf(VALID_POLICY), serviceConnector)

    // Put together a simple processor node we can use to access both connection types.
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> =
          setOf(SimpleProtoMessageWriter::class.java, SimpleProtoMessageReader::class.java)
      }

    // Get connections from our server "process"'s Chronicle
    val serverWriteConnection =
      server.chronicle.getConnectionOrThrow<SimpleProtoMessageWriter>(processorNode, VALID_POLICY)
    val serverReadConnection =
      server.chronicle.getConnectionOrThrow<SimpleProtoMessageReader>(processorNode, VALID_POLICY)

    // Get connection from our client "process"'s Chronicle
    val clientReadConnection =
      client.chronicle.getConnectionOrThrow<SimpleProtoMessageReader>(processorNode, VALID_POLICY)

    // Create some data.
    val messageA = protoGenerator.generateDistinctSimpleProtoMessage()
    val messageB = protoGenerator.generateDistinctSimpleProtoMessage(messageA)

    // Write from the server.
    serverWriteConnection.writeMessage(messageA)
    serverWriteConnection.writeMessage(messageB)

    // Verify that the client and server each have *both* written objects.
    assertThat(clientReadConnection.getMessages()).containsExactly(messageA, messageB)
    assertThat(serverReadConnection.getMessages()).containsExactly(messageA, messageB)
  }

  // for CoroutineStart.ATOMIC
  @Test
  fun clientAndServerBothPublishAndSubscribe(): Unit = runBlocking {
    // Initialize our "processes".
    val server = ServerProcess(setOf(VALID_POLICY), enableStreamServer = true)
    serviceConnector.binder = server.router
    val client = ClientProcess(setOf(VALID_POLICY), serviceConnector)

    // Put together a simple processor node we can use to access both connection types.
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> =
          setOf(SimpleProtoMessageSubscriber::class.java, SimpleProtoMessagePublisher::class.java)
      }

    // Get connections from our client "process"'s Chronicle
    val clientWriteConnection =
      client.chronicle.getConnectionOrThrow<SimpleProtoMessagePublisher>(
        processorNode,
        VALID_POLICY
      )
    val clientReadConnection =
      client.chronicle.getConnectionOrThrow<SimpleProtoMessageSubscriber>(
        processorNode,
        VALID_POLICY
      )

    // Get connections from our server "process"'s Chronicle
    val serverWriteConnection =
      server.chronicle.getConnectionOrThrow<SimpleProtoMessagePublisher>(
        processorNode,
        VALID_POLICY
      )
    val serverReadConnection =
      server.chronicle.getConnectionOrThrow<SimpleProtoMessageSubscriber>(
        processorNode,
        VALID_POLICY
      )

    // Create some data.
    val messageA = protoGenerator.generateDistinctSimpleProtoMessage()
    val messageB = protoGenerator.generateDistinctSimpleProtoMessage(messageA)

    val messagesReceivedByClient = mutableListOf<SimpleProtoMessage>()
    val messagesReceivedByServer = mutableListOf<SimpleProtoMessage>()

    val clientSubscription =
      launch(start = CoroutineStart.ATOMIC) {
        clientReadConnection.subscribe().take(2).collect { messagesReceivedByClient.add(it) }
      }
    val serverSubscription =
      launch(start = CoroutineStart.ATOMIC) {
        serverReadConnection.subscribe().take(2).collect { messagesReceivedByServer.add(it) }
      }

    withContext(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
      // Suspend until both subscriptions have actually been received by the server.
      server.subscriptions.first { it == 2 }

      // Write from the server and the client.
      clientWriteConnection.publish(messageA)
      serverWriteConnection.publish(messageB)

      // Wait until both subscribers have seen both messages.
      clientSubscription.join()
      serverSubscription.join()

      // Verify that the client and server each received both objects.
      assertThat(messagesReceivedByClient).containsExactly(messageA, messageB).inOrder()
      assertThat(messagesReceivedByServer).containsExactly(messageA, messageB).inOrder()
    }
  }

  @Test
  fun serverCanServeBothStoreAndStreamConnectionsSimultaneously(): Unit = runBlocking {
    // Initialize our "processes".
    val server =
      ServerProcess(setOf(VALID_POLICY), enableStoreServer = true, enableStreamServer = true)
    serviceConnector.binder = server.router
    val client = ClientProcess(setOf(VALID_POLICY), serviceConnector)

    // Put together a simple processor node we can use to access both connection types.
    val processorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> =
          setOf(SimpleProtoMessageSubscriber::class.java, SimpleProtoMessagePublisher::class.java)
      }

    // Get connections from our client "process"'s Chronicle
    client.chronicle.getConnectionOrThrow<SimpleProtoMessagePublisher>(processorNode, VALID_POLICY)
    client.chronicle.getConnectionOrThrow<SimpleProtoMessageSubscriber>(processorNode, VALID_POLICY)

    // Get connections from our server "process"'s Chronicle
    server.chronicle.getConnectionOrThrow<SimpleProtoMessagePublisher>(processorNode, VALID_POLICY)
    server.chronicle.getConnectionOrThrow<SimpleProtoMessageSubscriber>(processorNode, VALID_POLICY)
  }

  /**
   * Generates a new [SimpleProtoMessage] with distinct [SimpleProtoMessage.getStringField] values
   * from any of the [distinctFrom] values.
   */
  private fun RandomProtoGenerator.generateDistinctSimpleProtoMessage(
    vararg distinctFrom: SimpleProtoMessage
  ): SimpleProtoMessage {
    val stringFieldValuesToAvoid = distinctFrom.map { it.stringField }.toSet()

    var message: SimpleProtoMessage
    do {
      message = generateSimpleProtoMessage()
    } while (message.stringField in stringFieldValuesToAvoid)
    return message
  }

  /**
   * Defines what amounts to a "Process" acting as a client in a Client-Server remote storage
   * scenario.
   *
   * @param policies set of [Policies][Policy] to support in the [chronicle] instance.
   */
  class ClientProcess(policies: Set<Policy>, serviceConnector: ChronicleServiceConnector) {
    private val fakeTime = Instant.now()
    private val timeSource = TimeSource { fakeTime }
    private val configReader = FakeFlagsReader(Flags())

    /** Client store which connects to the server running in the [ServerProcess]. */
    private val remoteStoreClient =
      DefaultRemoteStoreClient(
        dataTypeName = SIMPLE_PROTO_MESSAGE_DTD.name,
        serializer = ProtoSerializer.createFrom(SimpleProtoMessage.getDefaultInstance()),
        transport = AidlTransport(serviceConnector)
      )

    private val remoteStreamClient =
      DefaultRemoteStreamClient(
        dataTypeName = SIMPLE_PROTO_MESSAGE_DTD.name,
        serializer = ProtoSerializer.createFrom(SimpleProtoMessage.getDefaultInstance()),
        transport = AidlTransport(serviceConnector)
      )

    /**
     * Connection provider instance using the [remoteStoreClient] to back the
     * [SimpleProtoMessageReader]/[SimpleProtoMessageWriter] interfaces.
     */
    private val connectionProvider =
      object : ConnectionProvider {
        override val dataType =
          object : DataType {
            override val descriptor = SIMPLE_PROTO_MESSAGE_DTD
            override val managementStrategy = MANAGEMENT_STRATEGY
            override val connectionTypes =
              setOf(
                SimpleProtoMessageReader::class.java,
                SimpleProtoMessageWriter::class.java,
                SimpleProtoMessageSubscriber::class.java,
                SimpleProtoMessagePublisher::class.java
              )
          }

        override fun getConnection(
          connectionRequest: ConnectionRequest<out Connection>
        ): Connection {
          return when (connectionRequest.connectionType) {
            SimpleProtoMessageReader::class.java ->
              object : SimpleProtoMessageReader {
                override suspend fun getMessages(): List<SimpleProtoMessage> {
                  return remoteStoreClient
                    .fetchAll(connectionRequest.policy)
                    .map { it.entity }
                    .toList()
                }
              }
            SimpleProtoMessageWriter::class.java ->
              object : SimpleProtoMessageWriter {
                override suspend fun writeMessage(message: SimpleProtoMessage) {
                  val timestamp = timeSource.now().toProtoTimestamp()
                  remoteStoreClient.create(
                    connectionRequest.policy,
                    listOf(
                      WrappedEntity(
                        EntityMetadata.newBuilder()
                          .setId(message.toString())
                          .setCreated(timestamp)
                          .setUpdated(timestamp)
                          .build(),
                        message
                      )
                    )
                  )
                }
              }
            SimpleProtoMessageSubscriber::class.java ->
              object : SimpleProtoMessageSubscriber {
                override fun subscribe(): Flow<SimpleProtoMessage> {
                  return remoteStreamClient.subscribe(connectionRequest.policy).map { it.entity }
                }
              }
            SimpleProtoMessagePublisher::class.java ->
              object : SimpleProtoMessagePublisher {
                override suspend fun publish(message: SimpleProtoMessage) {
                  remoteStreamClient.publish(
                    connectionRequest.policy,
                    listOf(
                      WrappedEntity(
                        EntityMetadata.newBuilder().setId(message.toString()).build(),
                        message
                      )
                    )
                  )
                }
              }
            else -> throw IllegalArgumentException("not supported: $connectionRequest")
          }
        }
      }

    /** [Chronicle] instance "running" in the client "process". */
    val chronicle: Chronicle =
      DefaultChronicle(
        chronicleContext =
          DefaultChronicleContext(
            setOf(connectionProvider),
            emptySet(),
            DefaultPolicySet(policies),
            DefaultDataTypeDescriptorSet(setOf(connectionProvider.dataType.descriptor))
          ),
        policyEngine = ChroniclePolicyEngine(),
        config =
          DefaultChronicle.Config(
            DefaultChronicle.Config.PolicyMode.STRICT,
            DefaultPolicyConformanceCheck()
          ),
        flags = configReader
      )
  }

  /**
   * Defines what amounts to a "Process" acting as a server in a Client-Server remote storage
   * scenario. Currently, remote connection servers are either Store or Stream servers, not both
   * simultaneously. This construct represents what we have in reality at this point in time, though
   * technically Chronicle can support having both simultaneously.
   *
   * @param policies set of [Policies][Policy] to support in the [chronicle] instance.
   * @param enableStoreServer use the internal [simpleProtoMessageStoreServer] when serving
   *   connections.
   * @param enableStreamServer use the internal [simpleProtoMessageStreamServer] when serving
   *   connections.
   */
  class ServerProcess(
    private val policies: Set<Policy>,
    enableStoreServer: Boolean = false,
    enableStreamServer: Boolean = false
  ) {
    private val fakeTime = Instant.now()
    private val timeSource = TimeSource { fakeTime }
    private val dataCache = DataCacheStorageImpl(timeSource)
    private val configReader = FakeFlagsReader(Flags())
    val subscriptions = MutableStateFlow(0)

    /** Actual store containing [SimpleProtoMessage] objects. */
    private val managedDataCache =
      ManagedDataCache(
        entityClass = SimpleProtoMessage::class.java,
        cache = dataCache,
        maxSize = 100,
        ttl = MANAGEMENT_STRATEGY.ttl!!,
        dataTypeDescriptor = SIMPLE_PROTO_MESSAGE_DTD
      )

    /** [RemoteStoreServer] instance to expose the [managedDataCache] to the client "process". */
    private val simpleProtoMessageStoreServer =
      object : RemoteStoreServer<SimpleProtoMessage> {
        override val dataType =
          ManagedDataType(
            SIMPLE_PROTO_MESSAGE_DTD,
            managedDataCache.managementStrategy,
            setOf(SimpleProtoMessageReader::class.java, SimpleProtoMessageWriter::class.java)
          )

        override val dataTypeDescriptor = SIMPLE_PROTO_MESSAGE_DTD
        override val serializer =
          ProtoSerializer.createFrom(SimpleProtoMessage.getDefaultInstance())

        override fun getConnection(
          connectionRequest: ConnectionRequest<out Connection>
        ): Connection =
          when (connectionRequest.connectionType) {
            SimpleProtoMessageReader::class.java -> SimpleProtoMessageReaderImpl(managedDataCache)
            SimpleProtoMessageWriter::class.java ->
              SimpleProtoMessageWriterImpl(managedDataCache, timeSource)
            else -> throw IllegalArgumentException("not supported: $connectionRequest")
          }

        override suspend fun count(policy: Policy?): Int = throw NotImplementedError("")

        override fun fetchById(
          policy: Policy?,
          ids: List<String>
        ): Flow<List<WrappedEntity<SimpleProtoMessage>>> = flow {
          emit(ids.mapNotNull { managedDataCache.get(it) })
        }

        override fun fetchAll(policy: Policy?): Flow<List<WrappedEntity<SimpleProtoMessage>>> =
          flow {
            emit(managedDataCache.all())
          }

        override suspend fun create(
          policy: Policy?,
          wrappedEntities: List<WrappedEntity<SimpleProtoMessage>>
        ) {
          wrappedEntities.forEach {
            if (managedDataCache.get(it.entity.stringField) == null) {
              managedDataCache.put(it)
            }
          }
        }

        override suspend fun update(
          policy: Policy?,
          wrappedEntities: List<WrappedEntity<SimpleProtoMessage>>
        ) = wrappedEntities.forEach { managedDataCache.put(it) }

        override suspend fun deleteAll(policy: Policy?) = managedDataCache.removeAll()

        override suspend fun deleteById(policy: Policy?, ids: List<String>) =
          ids.forEach { managedDataCache.remove(it) }
      }

    // We create a wrapper EntityStreamProvider which lets us count the number of subscriptions so
    // that we can wait to continue in test cases until the subscription count reaches the number we
    // expect before we start publishing.
    private val entityStreamProviderDelegate = EntityStreamProviderImpl()
    private val entityStreamProvider =
      object : EntityStreamProvider {
        override fun <T : Any> getStream(cls: KClass<out T>): EntityStream<T> {
          val delegate = entityStreamProviderDelegate.getStream(cls)
          return object : EntityStream<T> {
            override suspend fun publishGroup(group: List<WrappedEntity<T>>) {
              delegate.publishGroup(group)
            }

            override fun subscribeGroups(): Flow<List<WrappedEntity<T>>> {
              subscriptions.update { it + 1 }
              return delegate.subscribeGroups()
            }
          }
        }
      }

    /**
     * [RemoteStreamServer] instance to expose the [entityStreamProvider] to the client "process".
     */
    private val simpleProtoMessageStreamServer: RemoteStreamServer<SimpleProtoMessage> =
      ManagedEntityStreamServer(
        SIMPLE_PROTO_MESSAGE_DTD,
        ProtoSerializer.createFrom(SimpleProtoMessage.getDefaultInstance()),
        entityStreamProvider,
        mapOf<
          Class<out Connection>,
          (ConnectionRequest<out Connection>, EntityStream<SimpleProtoMessage>) -> Connection
        >(
          SimpleProtoMessageSubscriber::class.java to
            { _, stream: EntityStream<SimpleProtoMessage> ->
              object : SimpleProtoMessageSubscriber {
                override fun subscribe(): Flow<SimpleProtoMessage> {
                  return stream.subscribe().map { it.entity }
                }
              }
            },
          SimpleProtoMessagePublisher::class.java to
            { _, stream: EntityStream<SimpleProtoMessage> ->
              object : SimpleProtoMessagePublisher {
                override suspend fun publish(message: SimpleProtoMessage) {
                  stream.publish(
                    WrappedEntity(
                      EntityMetadata.newBuilder().setId(message.toString()).build(),
                      message
                    )
                  )
                }
              }
            }
        )
      )

    val servers = buildSet {
      if (enableStoreServer) add(simpleProtoMessageStoreServer)
      if (enableStreamServer) add(simpleProtoMessageStreamServer)
    }

    /** [Chronicle] instance "running" in the server "process". */
    val chronicle: Chronicle =
      DefaultChronicle(
        chronicleContext =
          DefaultChronicleContext(
            servers,
            emptySet(),
            DefaultPolicySet(policies),
            DefaultDataTypeDescriptorSet(servers.map { it.dataTypeDescriptor }.toSet())
          ),
        policyEngine = ChroniclePolicyEngine(),
        config =
          DefaultChronicle.Config(
            DefaultChronicle.Config.PolicyMode.STRICT,
            DefaultPolicyConformanceCheck()
          ),
        flags = configReader
      )

    val router: RemoteRouter =
      RemoteRouter(
        CoroutineScope(SupervisorJob()),
        RemoteContextImpl(servers),
        RemotePolicyCheckerImpl(chronicle, DefaultPolicySet(policies)),
        RemoteServerHandlerFactory(),
        ClientDetailsProviderImpl(ApplicationProvider.getApplicationContext())
      )
  }

  interface SimpleProtoMessageReader : ReadConnection {
    suspend fun getMessages(): List<SimpleProtoMessage>
  }

  class SimpleProtoMessageReaderImpl(val managedDataCache: ManagedDataCache<SimpleProtoMessage>) :
    SimpleProtoMessageReader {
    override suspend fun getMessages(): List<SimpleProtoMessage> {
      return managedDataCache.all().map { it.entity }
    }
  }

  interface SimpleProtoMessageWriter : WriteConnection {
    suspend fun writeMessage(message: SimpleProtoMessage)
  }

  class SimpleProtoMessageWriterImpl(
    val managedDataCache: ManagedDataCache<SimpleProtoMessage>,
    val timeSource: TimeSource
  ) : SimpleProtoMessageWriter {
    override suspend fun writeMessage(message: SimpleProtoMessage) {
      managedDataCache.put(
        WrappedEntity(EntityMetadata(message.stringField, "no-package", timeSource.now()), message)
      )
    }
  }

  interface SimpleProtoMessageSubscriber : ReadConnection {
    fun subscribe(): Flow<SimpleProtoMessage>
  }

  interface SimpleProtoMessagePublisher : WriteConnection {
    suspend fun publish(message: SimpleProtoMessage)
  }

  companion object {
    private val MANAGEMENT_STRATEGY =
      ManagementStrategy.Stored(
        encrypted = false,
        media = StorageMedia.MEMORY,
        ttl = Duration.ofDays(1)
      )

    private val VALID_POLICY =
      policy("SimpleProtoMessagePolicy", "Read") {
        description = "Valid policy for egressing SimpleProtoMessages"

        target(SIMPLE_PROTO_MESSAGE_DTD, Duration.ofDays(2)) {
          retention(StorageMedium.RAM, false)

          "double_field" { rawUsage(UsageType.EGRESS) }
          "float_field" { rawUsage(UsageType.EGRESS) }
          "int_field" { rawUsage(UsageType.EGRESS) }
          "unsigned_int_field" { rawUsage(UsageType.EGRESS) }
          "signed_int_field" { rawUsage(UsageType.EGRESS) }
          "fixed_width_int_field" { rawUsage(UsageType.EGRESS) }
          "signed_fixed_width_int_field" { rawUsage(UsageType.EGRESS) }
          "long_field" { rawUsage(UsageType.EGRESS) }
          "unsigned_long_field" { rawUsage(UsageType.EGRESS) }
          "signed_long_field" { rawUsage(UsageType.EGRESS) }
          "fixed_width_long_field" { rawUsage(UsageType.EGRESS) }
          "signed_fixed_width_long_field" { rawUsage(UsageType.EGRESS) }
          "bool_field" { rawUsage(UsageType.EGRESS) }
          "string_field" { rawUsage(UsageType.EGRESS) }
          "enum_field" { rawUsage(UsageType.EGRESS) }
        }
      }

    private val INVALID_POLICY_MISSING_FIELDS =
      policy("SimpleProtoMessagePolicy", "Read") {
        description = "Valid policy for egressing SimpleProtoMessages"

        target(SIMPLE_PROTO_MESSAGE_DTD, Duration.ofDays(2)) {
          retention(StorageMedium.RAM, false)

          "bool_field" { rawUsage(UsageType.EGRESS) }
          "string_field" { rawUsage(UsageType.EGRESS) }
          "enum_field" { rawUsage(UsageType.EGRESS) }
        }
      }
  }
}
