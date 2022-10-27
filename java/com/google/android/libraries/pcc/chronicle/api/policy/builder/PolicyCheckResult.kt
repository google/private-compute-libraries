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

package com.google.android.libraries.pcc.chronicle.api.policy.builder

/**
 * A class to encapsulate policy checks. We use a string to capture the check, but we will make this
 * more structured as the need arises.
 */
data class PolicyCheck(val check: String) {
  override fun toString(): String = check
}

/** The possible results of a call to check the adherence to a [Policy]: either [Pass] or [Fail]. */
sealed class PolicyCheckResult {
  /** Denotes a successful policy check. */
  object Pass : PolicyCheckResult()

  /** Denotes a failed policy check. */
  data class Fail(val failingChecks: List<PolicyCheck>) : PolicyCheckResult() {
    val message = failingChecks.toString()
  }

  companion object {
    /**
     * A factory method to return the appropriate version of PolicyCheckResult based on whether the
     * provided [violations] list is empty.
     */
    fun make(violations: List<PolicyCheck>): PolicyCheckResult {
      return if (violations.isEmpty()) PolicyCheckResult.Pass
      else PolicyCheckResult.Fail(violations)
    }
  }
}
