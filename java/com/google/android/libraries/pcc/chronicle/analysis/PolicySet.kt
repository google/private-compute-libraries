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

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.policy.Policy

/**
 * Data structure which manages the collection of known [Policies][Policy] and provides utilities to
 * understand the context it implies.
 */
interface PolicySet {
  /** Finds and returns (if found) the [Policy] in this [PolicySet] with the given [policyName]. */
  fun findByName(policyName: String): Policy?

  /** Returns whether or not the given [Policy] is known to the [PolicySet] already. */
  operator fun contains(policy: Policy): Boolean

  /**
   * Returns the set of [ManagementStrategies][ManagementStrategy] of storage that is specified in
   * all [policies][Policy] mentioning the given [dataTypeDescriptor].
   */
  fun findManagementStrategies(dataTypeDescriptor: DataTypeDescriptor): Set<ManagementStrategy>

  /** Returns the policies as a set. */
  fun toSet(): Set<Policy>
}
