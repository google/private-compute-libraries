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

package com.google.android.libraries.pcc.chronicle.analysis

import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheckResult

/**
 * A [PolicyEngine] is capable of comparing a known [Policy] against the state of Chronicle, as
 * represented by a [ChronicleContext].
 */
interface PolicyEngine {
  /**
   * Checks the supplied [context] for adherence to the provided [policy] and returns either
   * [PolicyCheckResult.Pass] or [PolicyCheckResult.Fail].
   */
  fun checkPolicy(
    policy: Policy,
    request: ConnectionRequest<*>,
    context: ChronicleContext,
  ): PolicyCheckResult

  /**
   * Checks that all [ConnectionProvider]-provided [WriteConnections'][WriteConnection]
   * [ManagedDataTypes][ManagedDataType] abide by (or are more restrictive-than) the retention and
   * ttl data outlined for their types in the [policySet].
   *
   * **Note:** We are currently very strict in applying the policy rules. We enforce that all write
   * connections' strategies are at least as strong as the strongest requirement from policies. We
   * may relax this restraint in the future, with appropriate tooling to support it.
   *
   * **Also:** If any [ManagedDataType] in the [ChronicleContext's][ChronicleContext]
   * [ConnectionProvider] collection isn't mentioned by any [Policy]
   * - a [PolicyCheckResult.Fail] will be returned.
   */
  fun checkWriteConnections(context: ChronicleContext): PolicyCheckResult
}
