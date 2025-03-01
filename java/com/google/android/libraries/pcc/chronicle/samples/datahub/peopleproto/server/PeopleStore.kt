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

package com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.server

import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.Person
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Defines a custom implementation of in-memory storage of [Person] data.
 *
 * Alternative implementations could use something like [BlobStore] to the same effect, but there is
 * value in illustrating that using a custom storage back-end is straightforward with Chronicle.
 */
class PeopleStore(private val ttl: Duration, private val pageSize: Int = 10) {
  private val people = mutableMapOf<String, WrappedEntity<Person>>()
  private val mutex = Mutex()

  /**
   * Puts (creates or updates, based on the value of [Person.getName]) a list of [WrappedEntity]
   * values containing [Person] data.
   */
  suspend fun putPeople(peopleToPut: List<WrappedEntity<Person>>) {
    mutex.withLock {
      // In practice you will probably want to override the created/updated timestamps to reflect
      // the server times so that a client can't sneakily update timestamps to avoid TTL.
      peopleToPut.forEach { people[it.entity.name] = it }
    }
  }

  /** Removes all [Person] data from storage with the provided [names]. */
  suspend fun removePeople(names: List<String>) {
    mutex.withLock { names.forEach(people::remove) }
  }

  /** Deletes all [Person] data. */
  suspend fun deleteAll() {
    mutex.withLock { people.clear() }
  }

  /**
   * Fetches [Person] values (wrapped in [WrappedEntity] instances) where those values are
   * identified by the given [names] as a [Flow].
   *
   * @param ttlOverride duration used to shorten the effective time-to-live. This is useful if
   *   storage is allowed to retain [Person] data longer than a client is allowed access to the
   *   data.
   */
  fun fetchByName(
    names: List<String>,
    ttlOverride: Duration? = null,
  ): Flow<List<WrappedEntity<Person>>> {
    return flow {
      val filtered =
        mutex.withLock {
          cleanupInternal()
          names.mapNotNull { people[it] }.filterToTtlOverride(ttlOverride)
        }

      filtered.chunked(pageSize).forEach { emit(it) }
    }
  }

  /**
   * Fetches all [Person] data (wrapped in [WrappedEntity] instances) as a [Flow].
   *
   * @param ttlOverride duration used to shorten the effective time-to-live.
   */
  fun fetchAll(ttlOverride: Duration? = null): Flow<List<WrappedEntity<Person>>> {
    return flow {
      val filtered =
        mutex.withLock {
          cleanupInternal()
          people.values.filterToTtlOverride(ttlOverride)
        }

      filtered.chunked(pageSize).forEach { emit(it) }
    }
  }

  /**
   * Returns the total number of non-expired [Person] data.
   *
   * @param ttlOverride duration used to shorten the effective time-to-live.
   */
  suspend fun count(ttlOverride: Duration?): Int {
    return mutex.withLock {
      cleanupInternal()
      people.values.filterToTtlOverride(ttlOverride).size
    }
  }

  /**
   * Performs TTL expiration on the [Person] data being stored.
   *
   * This method should be triggered periodically via a WorkManager task or JobService job.
   */
  suspend fun cleanup() {
    mutex.withLock { cleanupInternal() }
  }

  private fun Collection<WrappedEntity<Person>>.filterToTtlOverride(
    ttlOverride: Duration?
  ): List<WrappedEntity<Person>> {
    return filter {
      ttlOverride == null ||
        it.metadata.created.toInstant().isAfter(Instant.now().minus(ttlOverride))
    }
  }

  private fun cleanupInternal() {
    people.entries
      .filter { it.value.metadata.created.toInstant().isBefore(Instant.now().minus(ttl)) }
      .forEach { people.remove(it.key) }
  }
}
