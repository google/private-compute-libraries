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

import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.future

/** The chronicle async reader interface for [DataCache]. */
interface DataCacheReaderAsync<T> : ReadConnection {
  /** Retrieves all entities of type [T]. */
  suspend fun all(): List<T>

  /** Retrieves entity with the specified ID, `null` when not found. */
  suspend fun forId(entityId: String): T?

  /**
   * Returns a list of [Timestamped<T>] for all stored labels, ordered by update timestamp from
   * latest to earliest.
   */
  suspend fun allTimestamped(): List<Timestamped<T>>

  /** Returns a map of <InstanceId, [WrappedEntity]<T>> for all stored labels. */
  suspend fun allAsMap(): Map<String, WrappedEntity<T>>

  /** Returns a [Timestamped<T>] for stored label instance indexed by [entityId] */
  suspend fun entityTimestampedById(entityId: String): Timestamped<T>?

  /** Retrieves all entities of type [T]. */
  fun allAsync(scope: CoroutineScope): ListenableFuture<List<T>> = scope.future { all() }

  /** Retrieves entity with the specified ID, `null` when not found. */
  fun forIdAsync(scope: CoroutineScope, entityId: String): ListenableFuture<T?> {
    return scope.future { forId(entityId) }
  }

  /**
   * Returns a list of [Timestamped<T>] for all stored labels, ordered by update timestamp from
   * latest to earliest.
   */
  fun allTimestampedAsync(scope: CoroutineScope): ListenableFuture<List<Timestamped<T>>> {
    return scope.future { allTimestamped() }
  }

  /** Returns a map of <InstanceId, [WrappedEntity]<T>> for all stored labels. */
  fun allAsMapAsync(scope: CoroutineScope): ListenableFuture<Map<String, WrappedEntity<T>>> {
    return scope.future { allAsMap() }
  }

  /** Returns a [Timestamped<T>] for stored label instance indexed by [entityId] */
  fun entityTimestampedByIdAsync(
    scope: CoroutineScope,
    entityId: String,
  ): ListenableFuture<Timestamped<T>?> = scope.future { entityTimestampedById(entityId) }
}
