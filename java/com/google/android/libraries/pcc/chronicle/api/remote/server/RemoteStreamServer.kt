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
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow

/**
 * A [RemoteServer] capable of providing indefinite publish/subscribe behavior for a given entity
 * type [T].
 */
interface RemoteStreamServer<T : Any> : RemoteServer<T> {
  /**
   * Returns a [Flow] of pages (i.e. lists) of wrapped entities. The flow emits a page or pages when
   * the backing "store" is updated, usually via calls to [publish].
   */
  fun subscribe(policy: Policy?): Flow<List<WrappedEntity<T>>>

  /** Publishes a batch of wrapped entities to the backing "store". */
  suspend fun publish(policy: Policy?, entities: List<WrappedEntity<T>>)
}
