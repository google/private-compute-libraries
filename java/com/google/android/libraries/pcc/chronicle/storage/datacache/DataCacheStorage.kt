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
import java.time.Duration

/*
 * TODO: go/nullness-caller-updates-lsc - Could this class bounds its type parameters with <T : Any>
 * instead of using Class<T & Any> throughout?
 */
/** A type based key-value entity store. */
interface DataCacheStorage {

  /** Returns a set of data types registered with the data store. */
  val registeredDataTypes: Set<Class<*>>

  /**
   * Registers a data type with the storage with storage configuration parameters. The data store
   * will only respond queries to the registered data types.
   */
  fun <T> registerDataType(
    cls: Class<T & Any>,
    maxSize: Int,
    onDisk: Boolean = false,
    ttl: Duration
  )

  /** Unregister a data type and remove all stored instances form the storage. */
  fun <T> unregisterDataType(cls: Class<T & Any>)

  /** Returns the number of instances with type [T] in the storage. */
  fun <T> size(cls: Class<T & Any>): Int

  /**
   * Return the stored [WrappedEntity] instance with data type data type [T] identified by [id],
   * null if no such instance is found.
   */
  fun <T> get(cls: Class<T & Any>, id: String): WrappedEntity<T>?

  /**
   * Stores [WrappedEntity] instance represents data type [T] with [id] as the key.
   *
   * Returns [true] if the [WrappedEntity] instance is successfully stored, [false] otherwise.
   */
  fun <T> put(cls: Class<out T & Any>, entity: WrappedEntity<T>): Boolean

  /**
   * Removes stored instance with data type [T] and identified by [id].
   *
   * Returns the removed stored [WrappedEntity] instance, null if no instance is found with [id] for
   * type [T]
   */
  fun <T> remove(cls: Class<out T & Any>, id: String): WrappedEntity<T>?

  /** Returns all stored [WrappedEntity] instances for a specific data type [T]. */
  fun <T> all(cls: Class<T & Any>): List<WrappedEntity<T>>

  /** Returns map of <EntityId, [WrappedEntity]> for instances for a specific data type [T]. */
  fun <T> allAsMap(cls: Class<T & Any>): Map<String, WrappedEntity<T>>

  /** Removes all stored instances for a specific data type [T]. */
  fun <T> removeAll(cls: Class<T & Any>)

  /** Purges all expired instances for each data type [T]. */
  fun purgeExpiredEntities()

  /**
   * Remove all stored entities associated with the provided package name.
   *
   * @return The number of entities deleted
   */
  fun purgeAllEntitiesForPackage(packageName: String): Int

  /**
   * Remove all stored entities for a given data type associated with the provided package name.
   *
   * @return The number of entities deleted
   */
  fun <T> purgeEntitiesForPackage(cls: Class<T & Any>, packageName: String): Int

  /**
   * Remove all stored entities whose package name is not part of the provided set.
   *
   * @return The number of entities deleted
   */
  fun purgeAllEntitiesNotInPackages(packages: Set<String>): Int

  /** Remove all entities for [cls] type matches with filter conditions specified in [predicate]. */
  fun purgeEntitiesWhere(cls: Class<out Any>, predicate: (WrappedEntity<*>) -> Boolean): Int

  /** Completely remove all entities, return the count of entities removed. */
  fun purgeAllEntities(): Int
}
