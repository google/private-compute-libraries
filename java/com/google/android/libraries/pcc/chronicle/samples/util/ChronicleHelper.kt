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

import android.content.Context
import com.google.android.libraries.pcc.chronicle.analysis.DefaultChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.DefaultPolicySet
import com.google.android.libraries.pcc.chronicle.analysis.PolicySet
import com.google.android.libraries.pcc.chronicle.analysis.impl.ChroniclePolicyEngine
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.flags.Flags
import com.google.android.libraries.pcc.chronicle.api.flags.FlagsReader
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultChronicle
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultChronicle.Config.PolicyMode
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultDataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.policy.DefaultPolicyConformanceCheck
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.remote.RemoteContext
import com.google.android.libraries.pcc.chronicle.remote.RemotePolicyChecker
import com.google.android.libraries.pcc.chronicle.remote.RemoteRouter
import com.google.android.libraries.pcc.chronicle.remote.handler.RemoteServerHandlerFactory
import com.google.android.libraries.pcc.chronicle.remote.impl.ClientDetailsProviderImpl
import com.google.android.libraries.pcc.chronicle.remote.impl.RemoteContextImpl
import com.google.android.libraries.pcc.chronicle.remote.impl.RemotePolicyCheckerImpl
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Serves as a single point of integration when introducing Chronicle to a host application using
 * default implementations of many of Chronicle's components.
 *
 * **Note:** This, or something quite similar to it, is under consideration for being made into
 * general public Chronicle API.
 *
 * This class will typically be instantiated either by a dependency injection framework like Dagger
 * or directly, within the host application's [android.app.Application] class. For example:
 *
 * ```
 * class MyApplication : Application() {
 *   lateinit var chronicleHelper: ChronicleHelper
 *
 *   override fun onCreate() {
 *     super.onCreate()
 *
 *     // ... Other application.onCreate() setup ...
 *
 *     chronicleHelper =
 *       ChronicleHelper(
 *         policies = setOf(PolicyA, PolicyB),
 *         connectionProviders = setOf(FooConnectionProvider(), BarConnectionProvider()),
 *         remoteServers = setOf(BazRemoteStoreServer())
 *       )
 *   }
 * }
 * ```
 */
class ChronicleHelper(
  policies: Set<Policy>,
  connectionProviders: Set<ConnectionProvider>,
  remoteServers: Set<RemoteServer<*>> = emptySet(),
  initialConnectionContext: TypedMap = TypedMap(),
  initialFlags: Flags = Flags(),
  initialProcessorNodes: Set<ProcessorNode> = emptySet()
) {
  private val policySet: PolicySet = DefaultPolicySet(policies)
  private val dataTypeDescriptors: DataTypeDescriptorSet =
    DefaultDataTypeDescriptorSet(connectionProviders.dtds + remoteServers.dtds)
  private val flagsFlow = MutableStateFlow(initialFlags)
  private val chronicleContext =
    DefaultChronicleContext(
      connectionProviders + remoteServers,
      initialProcessorNodes,
      policySet,
      dataTypeDescriptors,
      initialConnectionContext
    )
  private val defaultChronicle: DefaultChronicle by lazy {
    DefaultChronicle(
      chronicleContext,
      ChroniclePolicyEngine(),
      DefaultChronicle.Config(PolicyMode.STRICT, DefaultPolicyConformanceCheck()),
      object : FlagsReader {
        override val config: StateFlow<Flags> = flagsFlow
      }
    )
  }
  private val remoteContext: RemoteContext = RemoteContextImpl(remoteServers)
  private val remotePolicyChecker: RemotePolicyChecker by lazy {
    RemotePolicyCheckerImpl(chronicle, policySet)
  }

  /**
   * Returns an instance of [Chronicle] that can be used by feature developers within
   * [ProcessorNodes][ProcessorNode].
   *
   * For example:
   * ```
   * class MyActivity : Activity(), ProcessorNode {
   *   private val chronicle = (applicationContext as MyApplication).chronicleHelper.chronicle
   *   // ... other fields for MyActivity ...
   *
   *   fun handlePersonCreateButtonClicked() {
   *     scope.launch {
   *       val connection =
   *         chronicle.getConnectionOrThrow(
   *           ConnectionRequest(PeopleWriter::class.java, this, policy = null)
   *         )
   *       connection.putPerson(Person.newBuilder().setName("Larry").setAge(42).build())
   *     }
   *   }
   *
   *   // ... other methods for MyActivity ...
   * }
   * ```
   */
  val chronicle: Chronicle
    get() = defaultChronicle

  /** Updates Chronicle feature flags. */
  fun setFlags(flags: Flags) {
    flagsFlow.value = flags
  }

  /**
   * Updates the connection context [TypedMap] used by the policy engine to verify a policy is valid
   * given its [Policy.allowedContext] rules.
   */
  fun setConnectionContext(connectionContext: TypedMap) {
    defaultChronicle.updateConnectionContext(connectionContext)
  }

  /**
   * Creates an implementation of the [IBinder] used to support the server/proxy-side of Chronicle
   * remote connections: [IRemote].
   *
   * The provided [serviceScope] is used to launch coroutines when handling remote requests from
   * clients. It should be configured with a [CoroutineDispatcher] which does not run on the main
   * thread and it is suggested to also use a [SupervisorJob] so that one failing request does not
   * cause all other requests to fail for the lifetime of the service.
   *
   * This method should be used within [Service.onBind]:
   * ```
   * class MyChronicleService : Service() {
   *   private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
   *
   *   override fun onBind(intent: Intent?): IBinder? {
   *     super.onBind(intent)
   *     val chronicleHelper = (applicationContext as MyApplication).chronicleHelper
   *     return chronicleHelper.createRemoteConnectionBinder(this, scope, intent)
   *   }
   *
   *   override fun onDestroy() {
   *     scope.cancel()
   *     super.onDestroy()
   *   }
   * }
   * ```
   *
   * @param context Reference to the Android component which will own the returned [IRemote] binder
   * (an [android.app.Service], typically).
   * @param scope A [CoroutineScope] tied to the lifecycle of the Android component which will own
   * the returned [IRemote] binder (an [android.app.Service], typically).
   */
  fun createRemoteConnectionBinder(context: Context, scope: CoroutineScope): IRemote {
    return RemoteRouter(
      scope,
      remoteContext,
      remotePolicyChecker,
      RemoteServerHandlerFactory(),
      ClientDetailsProviderImpl(context)
    )
  }

  private val Set<ConnectionProvider>.dtds: Set<DataTypeDescriptor>
    get() = map { it.dataType.descriptor }.toSet()
}
