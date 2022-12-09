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

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.storage.TypedManagedStore
import java.time.Duration

/** A [TypedDataCache<T>] that is managed with Chronicle. */
open class ManagedDataCache<T>
constructor(
  private val entityClass: Class<T>,
  private val cache: DataCacheStorage,
  private val maxSize: Int,
  private val ttl: Duration,
  override val dataTypeDescriptor: DataTypeDescriptor,
) : TypedDataCache<T>, TypedManagedStore<T> {
  override val managementStrategy: ManagementStrategy

  init {
    cache.registerDataType(entityClass, maxSize = maxSize, ttl = ttl)
    @Suppress("LeakingThis")
    managementStrategy =
      ManagementStrategy.Stored(
        encrypted = false,
        media = StorageMedia.MEMORY,
        ttl = ttl,
        deletionTriggers = setOf(DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "packageName"))
      )
  }

  override fun size(): Int = cache.size(entityClass)

  override fun get(id: String): WrappedEntity<T>? = cache.get(entityClass, id)

  override fun put(entity: WrappedEntity<T>): Boolean = cache.put(entityClass, entity)

  override fun remove(id: String): WrappedEntity<T>? = cache.remove(entityClass, id)

  override fun all(): List<WrappedEntity<T>> = cache.all(entityClass)

  override fun allAsMap(): Map<String, WrappedEntity<T>> = cache.allAsMap(entityClass)

  override fun removeAll() = cache.removeAll(entityClass)

  override fun purgeExpiredEntities() = cache.purgeExpiredEntities()

  companion object {
    /** Returns an instance of [ManagedDataCache<T>]. */
    inline fun <reified T> create(
      cache: DataCacheStorage,
      maxSize: Int,
      ttl: Duration,
      dataTypeDescriptor: DataTypeDescriptor,
    ) = ManagedDataCache(T::class.java, cache, maxSize, ttl, dataTypeDescriptor)
  }
}
