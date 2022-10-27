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
 * Default implementation of [PolicySet]. All [policies] provided at construction time are fixed and
 * immutable.
 */
data class DefaultPolicySet(private val policies: Set<Policy>) : PolicySet {
  private val managementStrategies =
    policies
      .flatMap { it.targets }
      .groupBy { it.schemaName }
      .mapValues { (_, targets) ->
        targets.flatMap { it.retentionsAsManagementStrategies() }.toSet()
      }

  override fun findByName(policyName: String): Policy? = policies.find { it.name == policyName }

  override fun contains(policy: Policy): Boolean = policy in policies

  override fun findManagementStrategies(
    dataTypeDescriptor: DataTypeDescriptor
  ): Set<ManagementStrategy> = managementStrategies[dataTypeDescriptor.name] ?: emptySet()

  override fun toSet(): Set<Policy> {
    return policies.toSet()
  }
}
