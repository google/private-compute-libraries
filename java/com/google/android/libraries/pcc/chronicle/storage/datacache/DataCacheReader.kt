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
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import java.time.Instant

/** The chronicle default reader interface for [DataCache]. */
interface DataCacheReader<T> : ReadConnection {
  fun all(): List<T>

  /**
   * Returns a list of [Timestamped<T>] for all stored labels, ordered by update timestamp from
   * latest to earliest.
   */
  fun allTimestamped(): List<Timestamped<T>>

  fun forEntity(entityId: String): T?

  /** Returns a [Timestamped<T>] for stored label instance indexed by [entityId] */
  fun forEntityTimestamped(entityId: String): Timestamped<T>?

  companion object {
    /** Wraps the provided [TypedDataCache] with a [DataCacheReader] connection. */
    fun <T> createDefault(cache: TypedDataCacheReader<T>): DataCacheReader<T> {
      return object : DataCacheReader<T> {
        override fun all(): List<T> = cache.all().map { it.entity }

        override fun allTimestamped(): List<Timestamped<T>> =
          cache
            .all()
            .asSequence()
            .map { it.asTimestampedInstance() }
            .sortedBy { it.updateTimestamp }
            .toList()

        override fun forEntity(entityId: String): T? = cache.get(entityId)?.entity

        override fun forEntityTimestamped(entityId: String): Timestamped<T>? =
          cache.get(entityId)?.asTimestampedInstance()
      }
    }
  }
}

/** A wrapper class for a label instance with its last updated time in the storage. */
data class Timestamped<T>(val instance: T, val updateTimestamp: Instant)

/** Helper function to convert a [WrappedEntity] to a [Timestamped] instance. */
fun <T> WrappedEntity<T>.asTimestampedInstance(): Timestamped<T> =
  Timestamped(entity, metadata.updated.toInstant())
