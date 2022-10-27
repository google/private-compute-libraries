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

package com.google.android.libraries.pcc.chronicle.storage.blobstore.inmemory

import android.util.LruCache
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.storage.blobstore.InMemoryManagementInfo
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/**
 * This class contains the full collection of in memory caches for BlobStore. They are organized
 * into a map, keyed by DTD name.
 */
class InMemoryStorage {
  internal val store = atomic(emptyMap<String, CacheWrapper<*>>())

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> registerDataTypeStore(managementInfo: InMemoryManagementInfo): CacheWrapper<T> {
    store.update { currentStore ->
      val cache = currentStore[managementInfo.dtdName] as? CacheWrapper<T>
      if (cache != null) {
        return@update currentStore
      }
      val newCache =
        CacheWrapper(LruCache<String, WrappedEntity<T>>(managementInfo.maxItems), managementInfo)
      return@update currentStore + (managementInfo.dtdName to newCache)
    }
    return store.value[managementInfo.dtdName] as CacheWrapper<T>
  }
}

data class CacheWrapper<T>(
  val cache: LruCache<String, WrappedEntity<T>>,
  val managementInfo: InMemoryManagementInfo
)
