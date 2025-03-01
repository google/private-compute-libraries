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

package com.google.android.libraries.pcc.chronicle.api.testutil

import com.google.android.libraries.pcc.chronicle.analysis.DefaultChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.DefaultPolicySet
import com.google.android.libraries.pcc.chronicle.analysis.PolicyEngine
import com.google.android.libraries.pcc.chronicle.analysis.impl.ChroniclePolicyEngine
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.flags.FakeFlagsReader
import com.google.android.libraries.pcc.chronicle.api.flags.Flags
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultChronicle
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultDataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.policy.DefaultPolicyConformanceCheck
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyConformanceCheck

/**
 * Allows for the construction of [DefaultChronicle] using kotlin-style builder patterns. Uses a
 * static set of flags, so not suitable for production use.
 */
fun fakeChronicle(builder: FakeChronicleBuilder.() -> Unit): Chronicle {
  return FakeChronicleBuilder().apply(builder).build()
}

/** Builder of [DefaultChronicle] objects for testing. */
class FakeChronicleBuilder {
  private val flags = FakeFlagsReader(Flags())
  private val connectionProviders = mutableSetOf<ConnectionProvider>()
  private val policies = mutableSetOf<Policy>()
  private var config: DefaultChronicle.Config =
    DefaultChronicle.Config(
      policyMode = DefaultChronicle.Config.PolicyMode.STRICT,
      policyConformanceCheck = DefaultPolicyConformanceCheck(),
    )

  /**
   * The implementation of a [PolicyEngine] for checking a [Policy] against the state of a
   * [ChronicleContext].
   *
   * This usually only needs to be changed during testing.
   */
  var policyEngine: PolicyEngine = ChroniclePolicyEngine()

  /**
   * Specifies that the implementation of [PolicyEngine] used by the built [Chronicle] will be the
   * default.
   */
  fun useDefaultPolicyEngine(): FakeChronicleBuilder = apply {
    policyEngine = ChroniclePolicyEngine()
  }

  /** Set the [policyMode] value in the [Config]. */
  var policyMode: DefaultChronicle.Config.PolicyMode
    get() = config.policyMode
    set(newValue) {
      config = config.copy(policyMode = newValue)
    }

  /** Set the [policyConformanceCheck] mechanism in the [Config]. */
  var policyConformanceCheck: PolicyConformanceCheck
    get() = config.policyConformanceCheck
    set(newValue) {
      config = config.copy(policyConformanceCheck = newValue)
    }

  /** Registers a [ConnectionProvider] for the built [DefaultChronicle]'s clients. */
  fun register(connectionProvider: ConnectionProvider): FakeChronicleBuilder = apply {
    connectionProviders.add(connectionProvider)
  }

  /** Registers a [Policy] for enforcement during [DefaultChronicle.getConnection]. */
  fun register(policy: Policy): FakeChronicleBuilder = apply { policies.add(policy) }

  /** Builds a [DefaultChronicle] object. */
  fun build(): Chronicle {
    val dtds = connectionProviders.map { it.dataType.descriptor }.toSet()
    return DefaultChronicle(
      DefaultChronicleContext(
        connectionProviders,
        emptySet(),
        DefaultPolicySet(policies),
        DefaultDataTypeDescriptorSet(dtds),
      ),
      policyEngine,
      config,
      flags,
    )
  }
}
