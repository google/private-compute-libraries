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

package com.google.android.libraries.pcc.chronicle.api.policy

import com.google.android.libraries.pcc.chronicle.api.error.MalformedPolicySet

/**
 * Applies the following conformance rules to the set of [Policies][Policy] passed to it:
 *
 * 1. All [Policies][Policy] must have non-empty [Policy.description] values.
 * 1. All [PolicyTargets][PolicyTarget] must contain at least one [PolicyRetention] value.
 */
class DefaultPolicyConformanceCheck : PolicyConformanceCheck {
  override fun checkPoliciesConform(policies: Set<Policy>) {
    val issues =
      policies.flatMap { ensureDescriptionExists(it) + ensureTargetsSpecifyRetentionRules(it) }

    if (issues.isNotEmpty()) {
      throw MalformedPolicySet(issues.joinToString(", ", prefix = "Malformed policies found: "))
    }
  }

  private fun ensureDescriptionExists(policy: Policy): List<String> {
    if (policy.description.isNotBlank()) return emptyList()
    return listOf("Policy: \"${policy.name}\" has an empty description")
  }

  private fun ensureTargetsSpecifyRetentionRules(policy: Policy): List<String> {
    return policy.targets.mapNotNull { target ->
      if (target.retentions.isNotEmpty()) return@mapNotNull null

      "Target \"${target.schemaName}\" from policy: \"${policy.name}\" does not specify " +
        "any retention rules"
    }
  }
}
