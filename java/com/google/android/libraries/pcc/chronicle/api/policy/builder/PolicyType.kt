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

/** The type of policy, specific to the reason this data is being collected / processed. */
abstract class PolicyType<T : FlavoredPoliciesBuilder<FlavorT>, FlavorT : FlavorConfigurator>(
  val egressType: String
) {
  /** Builds a [FlavoredPolicies] object. */
  fun flavoredPolicies(name: String, block: T.() -> Unit): FlavoredPolicies {
    return newBuilder(name).apply(block).buildAll()
  }

  /** Returns a new instance of the subclasses builder. */
  protected abstract fun newBuilder(name: String): T
}

/** Interface for per flavor configurator options. */
interface FlavorConfigurator

/** A [FlavorConfigurator] that doesn't offer any flavor-specific options. */
class EmptyFlavorConfigurator : FlavorConfigurator

/** Creates a new [FlavoredPolicies] based on the [PolicyType] arg. */
fun <T : FlavoredPoliciesBuilder<FlavorT>, FlavorT : FlavorConfigurator> flavoredPolicies(
  name: String,
  policyType: PolicyType<T, FlavorT>,
  block: T.() -> Unit
) = policyType.flavoredPolicies(name, block)
