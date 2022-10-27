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

package com.google.android.libraries.pcc.chronicle.api.remote.client

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow

/** Defines a [RemoteClient] intended for communicating with a [RemoteStreamServer]. */
interface RemoteStreamClient<T : Any> : RemoteClient<T> {
  /** Stream: Publishes the provided [entities] to a stream. */
  suspend fun publish(policy: Policy?, entities: List<WrappedEntity<T>>)

  /**
   * Stream: Subscribes to observe new entities from a stream. The returned [Flow] will not be
   * completed unless by cancellation or error - subscription is indefinite.
   */
  fun subscribe(policy: Policy?): Flow<WrappedEntity<T>>
}
