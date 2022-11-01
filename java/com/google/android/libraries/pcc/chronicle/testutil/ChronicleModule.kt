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

package com.google.android.libraries.pcc.chronicle.testutil

import com.google.android.libraries.pcc.chronicle.analysis.ChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.DefaultChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.PolicyEngine
import com.google.android.libraries.pcc.chronicle.analysis.PolicySet
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.flags.FlagsReader
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultChronicle
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultDataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.integration.NoOpChronicle
import com.google.android.libraries.pcc.chronicle.api.policy.DefaultPolicyConformanceCheck
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.util.Logcat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton
import kotlin.system.exitProcess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Module
@InstallIn(SingletonComponent::class)
abstract class ChronicleModule {
  @Multibinds abstract fun providesPolicies(): Set<Policy>

  @Multibinds abstract fun providesConnectionProviders(): Set<ConnectionProvider>

  @Multibinds abstract fun bindDtds(): Set<DataTypeDescriptor>

  companion object {
    val log = Logcat.default

    @Provides
    @Singleton
    fun bindDtdSetImpl(dtds: Set<@JvmSuppressWildcards DataTypeDescriptor>): DataTypeDescriptorSet =
      DefaultDataTypeDescriptorSet(dtds)

    @Provides
    @Singleton
    fun bindChronicleContextImpl(
      providers: Set<@JvmSuppressWildcards ConnectionProvider>,
      policySet: PolicySet,
      dtdSet: DataTypeDescriptorSet
    ): ChronicleContext = DefaultChronicleContext(providers, emptySet(), policySet, dtdSet)

    @OptIn(DelicateCoroutinesApi::class)
    @Provides
    @Singleton
    fun bindsChronicle(
      context: ChronicleContext,
      engine: PolicyEngine,
      flags: FlagsReader,
    ): Chronicle {
      // Because some policy checks happen in `ChronicleImpl` init(), when `failNewConnections`
      // goes from true -> false have to wait for an app restart to get the real impl back.
      val current = flags.config.value
      if (current.failNewConnections || current.emergencyDisable) {
        log.d("Starting Chronicle in no-op mode.")
        return NoOpChronicle(disabledFromStartupFlags = true)
      }
      // This listener is here rather than in ChronicleImpl to make the decision on whether to use
      // a NoOpImpl atomic with emergencyDisable and only enable this force-exit logic when the real
      // impl is active.
      flags.config
        .onEach { value ->
          if (value.emergencyDisable) {
            // Force exit the app. It will be restarted by SystemServer and when it comes back up
            // we will be using the no-op impl.
            log.i("Emergency disable engaged, restarting APK.")
            exitProcess(0)
          }
        }
        .launchIn(GlobalScope)
      log.d("Starting Chronicle in enabled mode.")
      return DefaultChronicle(
        context,
        engine,
        DefaultChronicle.Config(
          policyMode = DefaultChronicle.Config.PolicyMode.STRICT,
          policyConformanceCheck = DefaultPolicyConformanceCheck()
        ),
        flags
      )
    }
  }
}
