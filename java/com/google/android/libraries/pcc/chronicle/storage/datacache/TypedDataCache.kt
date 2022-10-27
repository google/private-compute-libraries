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

import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity

/** A type based key-value entity store to store values of type [T]. */
interface TypedDataCache<T> : TypedDataCacheReader<T>, TypedDataCacheWriter<T>

/** An interface that provides typed read access to a [DataCacheStorage]. */
interface TypedDataCacheReader<T> {
  /** Returns the number of instances with type [T] in the storage. */
  fun size(): Int

  /**
   * Return the stored [WrappedEntity] instance with data type data type [T] identified by [id],
   * null if no such instance is found.
   */
  fun get(id: String): WrappedEntity<T>?

  /** Returns all stored [WrappedEntity] instances for a specific data type [T]. */
  fun all(): List<WrappedEntity<T>>

  /** Returns map of <EntityId, [WrappedEntity]> for instances for a specific data type [T]. */
  fun allAsMap(): Map<String, WrappedEntity<T>>
}

/** An interface that provides typed write access to a [DataCacheStorage]. */
interface TypedDataCacheWriter<T> {
  /**
   * Stores [WrappedEntity] instance represents data type [T] with [id] as the key.
   *
   * Returns [true] if the [WrappedEntity] instance is successfully stored, [false] otherwise.
   */
  fun put(entity: WrappedEntity<T>): Boolean

  /**
   * Removes stored instance with data type [T] and identified by [id].
   *
   * Returns the removed stored [WrappedEntity] instance, null if no instance is found with [id] for
   * type [T]
   */
  fun remove(id: String): WrappedEntity<T>?

  /** Removes all stored instances for a specific data type [T]. */
  fun removeAll()

  /** Purges all expired instances for each data type [T]. */
  fun purgeExpiredEntities()
}
