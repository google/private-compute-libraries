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

package com.google.android.libraries.pcc.chronicle.api.remote.server

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.ComputeRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow

/**
 * A RemoteComputeServer encapsulates generalized computation logic intended to transform some
 * [Input][In] into some [Output][Out].
 *
 * When policies are checked by the ChronicleService before calling [run], the policy is currently
 * only checked against the [Out] data type, as that is what is considered as being "read" by the
 * client.
 *
 * In the future we will also consider checking policies on the [Input][In] data as well, especially
 * if the implementation of [run] needs to egress data from the Chronicle system.
 */
interface RemoteComputeServer<In : Any, Out : Any> : RemoteServer<Out> {
  /**
   * A [Serializer] used to deserialize the [Input][In] arguments to the [run] method.
   *
   * This property is used by the ChronicleService / RemoteRouter to pre-process input on behalf of
   * the [RemoteComputeServer].
   */
  val argumentSerializer: Serializer<In>

  /**
   * Runs the prescribed computation, given [input] to produce a [Flow] of pages (i.e. lists) of
   * metadata-wrapped [Output][Out] results.
   */
  fun run(
    policy: Policy?,
    method: ComputeRequest.MethodId,
    input: List<WrappedEntity<In>>,
  ): Flow<List<WrappedEntity<Out>>>
}
