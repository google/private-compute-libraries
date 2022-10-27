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

package com.google.android.libraries.pcc.chronicle.api.error

import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.policy.Policy

/**
 * A [Policy] declared within a [ConnectionRequest] was not found to have been pre-registered with
 * [Chronicle].
 */
class PolicyNotFound(message: String) : ChronicleError(message) {
  constructor(policy: Policy) : this(constructMessage(policy))

  companion object {
    private fun constructMessage(policy: Policy): String {
      return "Policy: $policy was not registered with Chronicle ahead of time."
    }
  }
}
