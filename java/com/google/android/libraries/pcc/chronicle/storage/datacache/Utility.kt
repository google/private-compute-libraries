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

/**
 * A simple helper interface to access [DataCacheStorage.registerDataType] interface with type
 * parameter. Example : DataCacheStorage.registerDataType<T>(maxSize).
 */
inline fun <reified T : Any> DataCacheStorage.registerDataType(maxSize: Int, ttl: Duration) =
  registerDataType(cls = T::class.java, maxSize = maxSize, ttl = ttl)

/**
 * A simple helper interface to access [DataCacheStorage.unregisterDataType] interface with type
 * parameter. Example : DataCacheStorage.unregisterDataType<T>().
 */
inline fun <reified T : Any> DataCacheStorage.unregisterDataType() =
  unregisterDataType(T::class.java)

/**
 * A simple helper interface to access [DataCacheStorage.size] interface with type parameter.
 * Example : DataCacheStorage.size<T>().
 */
inline fun <reified T : Any> DataCacheStorage.size() = size(T::class.java)

/**
 * A simple helper interface to access [DataCacheStorage.get] interface with type parameter. Example
 * : DataCacheStorage.get<T>(id).
 */
inline fun <reified T : Any> DataCacheStorage.get(id: String) = get(T::class.java, id)

/**
 * A simple helper interface to access [DataCacheStorage.put] interface with type parameter. Example
 * : DataCacheStorage.put<T>(id, entity).
 */
inline fun <reified T : Any> DataCacheStorage.put(entity: WrappedEntity<T>): Boolean =
  put(T::class.java, entity)

/**
 * A simple helper interface to access [DataCacheStorage.remove] interface with type parameter.
 * Example : DataCacheStorage.remove<T>(id).
 */
inline fun <reified T : Any> DataCacheStorage.remove(id: String): WrappedEntity<T>? =
  remove(T::class.java, id)

/**
 * A simple helper interface to access [DataCacheStorage.all] interface with type parameter. Example
 * : DataCacheStorage.all<T>().
 */
inline fun <reified T : Any> DataCacheStorage.all() = all(T::class.java)

/**
 * A simple helper interface to access [DataCacheStorage.removeAll] interface with type parameter.
 * Example : DataCacheStorage.removeAll<T>().
 */
inline fun <reified T : Any> DataCacheStorage.removeAll() = removeAll(T::class.java)
