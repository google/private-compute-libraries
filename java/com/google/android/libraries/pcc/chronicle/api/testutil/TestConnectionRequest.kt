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

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import kotlin.reflect.KClass

/**
 * Constructs an empty [ConnectionRequest] from the receiving [Connection] class object. Intended
 * for use by tests of [ConnectionProviders][ConnectionProvider].
 */
fun <T : Connection> Class<T>.toTestRequest(policy: Policy? = null): ConnectionRequest<T> =
  ConnectionRequest(this, EmptyProcessorNode, policy)

/**
 * Constructs an empty [ConnectionRequest] from the receiving [Connection] [KClass] object. Intended
 * for use by tests of [ConnectionProviders][ConnectionProvider].
 */
fun <T : Connection> KClass<T>.toTestRequest(policy: Policy? = null): ConnectionRequest<T> =
  java.toTestRequest(policy)
