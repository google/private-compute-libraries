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

package com.google.android.libraries.pcc.chronicle.storage.datacache

import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.future

/** The chronicle async writer interface for [DataCache]. */
interface DataCacheWriterAsync<T> : WriteConnection {
  /** Write entity, returning once the store acks the request. */
  suspend fun write(entity: WrappedEntity<T>)

  /** Removes the entity with the specified [id], no-op if that [id] isn't found. */
  suspend fun remove(id: String)

  /** Write entity, completing the future when the store completes the request. */
  fun writeAsync(
    scope: CoroutineScope,
    entity: WrappedEntity<T>,
  ): ListenableFuture<Unit> {
    return scope.future { write(entity) }
  }

  /** Java compatibility method, completes ListenableFuture when the store completes the request. */
  fun removeAsync(scope: CoroutineScope, id: String): ListenableFuture<Unit> {
    return scope.future { remove(id) }
  }
}
