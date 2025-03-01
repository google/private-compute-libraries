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

package com.google.android.libraries.pcc.chronicle.storage.stream

import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * An EntityStream is a publish/subscribe mechanism. It does not keep any actual entities (of type
 * [T]) in memory, instead: it simply forwards entities provided to it via [publish] to subscribers
 * listening to the flow returned by [subscribe].
 */
interface EntityStream<T : Any> {
  /**
   * Publishes the provided [group] of [WrappedEntities][WrappedEntity] of type [T] to the
   * [EntityStream].
   */
  suspend fun publishGroup(group: List<WrappedEntity<T>>)

  /** Publishes the provided [entity] to the [EntityStream]. */
  suspend fun publish(entity: WrappedEntity<T>) = publishGroup(listOf(entity))

  /**
   * Returns a [Flow] of groups of [WrappedEntities][WrappedEntity].
   *
   * **Note:** This method is primarily intended to support remote subscriptions with minimal
   * overhead by not requiring new list allocations for each page in a remote response. For local
   * access, consider using the [subscribe] method instead.
   */
  fun subscribeGroups(): Flow<List<WrappedEntity<T>>>

  /** Returns a [Flow] of individual [WrappedEntities][WrappedEntity]. */
  fun subscribe(): Flow<WrappedEntity<T>> = flow {
    subscribeGroups().collect { list -> list.forEach { emit(it) } }
  }
}
