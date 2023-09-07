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

package com.google.android.libraries.pcc.chronicle.storage.datacache.impl

import android.util.LruCache
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toInstant
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage
import com.google.android.libraries.pcc.chronicle.util.Logcat
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import java.time.Duration
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/** A [LruCache] based implementation of [DataCacheStorage]. */
class DataCacheStorageImpl(private val timeSource: TimeSource) : DataCacheStorage {
  private val entityStore = atomic(emptyMap<Class<*>, DataCacheWrapper<*>>())

  override val registeredDataTypes: Set<Class<*>>
    get() = entityStore.value.keys

  override fun registerDataType(cls: Class<*>, maxSize: Int, onDisk: Boolean, ttl: Duration) {
    entityStore.update { currentMap ->
      val currentCache = storeForClass(cls)
      currentCache?.resize(maxSize)

      val newEntry =
        DataCacheWrapper(cache = currentCache ?: LruCache(maxSize), config = CacheConfig(ttl))

      currentMap + (cls to newEntry)
    }

    logger.d("[%s] registerDataType : %s", className, cls.name)
  }

  override fun unregisterDataType(cls: Class<*>) {
    removeAll(cls)
    entityStore.update { it - cls }
    logger.d("[%s] unregisterDataType : %s", className, cls.name)
  }

  override fun <T> size(cls: Class<T & Any>): Int = all(cls).size

  override fun <T> get(cls: Class<T & Any>, id: String): WrappedEntity<T>? {
    val dataCacheWrapper = dataCacheWrapperForClass<T>(cls) ?: return null
    val lookupEntityWrapper = dataCacheWrapper.cache.get(id) ?: return null
    val ttl = dataCacheWrapper.config.ttl
    if (lookupEntityWrapper.isExpired(ttl)) {
      return null
    }
    return lookupEntityWrapper
  }

  override fun <T> put(cls: Class<out T & Any>, entity: WrappedEntity<T>): Boolean {
    val entityCache = storeForClass<T>(cls) ?: return false
    entityCache.put(entity.metadata.id, entity)
    return true
  }

  override fun <T> remove(cls: Class<out T & Any>, id: String): WrappedEntity<T>? =
    storeForClass<T>(cls)?.remove(id)

  override fun <T> all(cls: Class<T & Any>): List<WrappedEntity<T>> {
    val dataCacheWrapper = dataCacheWrapperForClass<T>(cls) ?: return emptyList()
    val ttl = dataCacheWrapper.config.ttl
    return dataCacheWrapper.cache.snapshot().values.filterNot { it.isExpired(ttl) }
  }

  override fun <T> allAsMap(cls: Class<T & Any>): Map<String, WrappedEntity<T>> {
    val dataCacheWrapper = dataCacheWrapperForClass<T>(cls) ?: return emptyMap()
    val ttl = dataCacheWrapper.config.ttl
    return dataCacheWrapper.cache.snapshot().filterNot { it.value.isExpired(ttl) }
  }

  override fun removeAll(cls: Class<*>) {
    storeForClass(cls)?.evictAll()
  }

  override fun purgeExpiredEntities() {
    logger.d("[%s]: purging entities from data cache.", className)
    entityStore.value.values.forEach { dataCacheWrapper ->
      val ttl = dataCacheWrapper.config.ttl
      dataCacheWrapper.purgeEntitiesWhere { it.isExpired(ttl) }
    }
  }

  override fun purgeAllEntitiesForPackage(packageName: String): Int {
    logger.d("[%s]: purging all entities for package removal.", className)
    return entityStore.value.values
      .map { dataCacheWrapper ->
        dataCacheWrapper.purgeEntitiesWhere {
          packageName in it.metadata.associatedPackageNamesList
        }
      }
      .sum()
  }

  override fun purgeEntitiesForPackage(cls: Class<*>, packageName: String): Int {
    logger.d("[%s]: purging %s entities for package removal.", className, cls.name)
    return dataCacheWrapperForClass(cls)?.purgeEntitiesWhere {
      packageName in it.metadata.associatedPackageNamesList
    }
      ?: 0
  }

  override fun purgeAllEntitiesNotInPackages(packages: Set<String>): Int {
    logger.d("[%s]: purging entities for package reconciliation.", className)
    return entityStore.value.values
      .map { dataCacheWrapper ->
        dataCacheWrapper.purgeEntitiesWhere {
          it.metadata.associatedPackageNamesList.all { pkg -> pkg !in packages }
        }
      }
      .sum()
  }

  override fun purgeEntitiesWhere(
    cls: Class<out Any>,
    predicate: (WrappedEntity<*>) -> Boolean
  ): Int = dataCacheWrapperForClass(cls)?.purgeEntitiesWhere(predicate) ?: 0

  override fun purgeAllEntities(): Int {
    return entityStore.value.values
      .map { dataCacheWrapper -> dataCacheWrapper.purgeEntitiesWhere { true } }
      .sum()
  }

  private fun WrappedEntity<*>.isExpired(ttl: Duration): Boolean =
    metadata.created.toInstant().plusMillis(ttl.toMillis()).isBefore(timeSource.now())

  private fun DataCacheWrapper<*>.purgeEntitiesWhere(
    predicate: (WrappedEntity<*>) -> Boolean
  ): Int {
    return cache
      .snapshot()
      .asSequence()
      .filter { (_, entity) -> predicate(entity) }
      .map { (key, _) -> cache.remove(key) }
      .count()
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> storeForClass(cls: Class<out T & Any>): LruCache<String, WrappedEntity<T>>? =
    dataCacheWrapperForClass<T>(cls)?.cache as? LruCache<String, WrappedEntity<T>>

  @Suppress("UNCHECKED_CAST")
  private fun <T> dataCacheWrapperForClass(cls: Class<out T & Any>): DataCacheWrapper<T>? {
    val result = entityStore.value[cls] as? DataCacheWrapper<T>
    if (result == null) {
      logger.d("[%s] no store registered for %s.", className, cls)
    }
    return result
  }

  companion object {
    private val logger = Logcat.default
    private val className = DataCacheStorageImpl::class.java.simpleName
  }
}

data class DataCacheWrapper<T>(
  val cache: LruCache<String, WrappedEntity<T>>,
  val config: CacheConfig
)

data class CacheConfig(val ttl: Duration)
