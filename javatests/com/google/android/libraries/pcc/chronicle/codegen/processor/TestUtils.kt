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

package com.google.android.libraries.pcc.chronicle.codegen.processor

import com.google.android.libraries.pcc.chronicle.storage.datacache.ManagedDataCache

/**
 * Do a field-by-field comparison of two [ManagedDataCache] objects. We don't override equals(),
 * since this is only needed for testing.
 */
fun ManagedDataCache<*>.configEquals(other: Any?): Boolean {
  if (this === other) return true
  if (other !is ManagedDataCache<*>) return false

  val entityClassField = ManagedDataCache::class.java.getDeclaredField("entityClass")
  entityClassField.isAccessible = true
  if (entityClassField.get(this) != entityClassField.get(other)) return false

  val cacheField = ManagedDataCache::class.java.getDeclaredField("cache")
  cacheField.isAccessible = true
  if (cacheField.get(this) != cacheField.get(other)) return false

  val maxSizeField = ManagedDataCache::class.java.getDeclaredField("maxSize")
  maxSizeField.isAccessible = true
  if (maxSizeField.getInt(this) != maxSizeField.getInt(other)) return false

  val ttlField = ManagedDataCache::class.java.getDeclaredField("ttl")
  ttlField.isAccessible = true
  if (ttlField.get(this) != ttlField.get(other)) return false

  if (dataTypeDescriptor != other.dataTypeDescriptor) return false
  if (managementStrategy != other.managementStrategy) return false

  return true
}
