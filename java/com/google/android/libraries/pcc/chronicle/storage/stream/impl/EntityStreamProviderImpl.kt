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

package com.google.android.libraries.pcc.chronicle.storage.stream.impl

import com.google.android.libraries.pcc.chronicle.storage.stream.EntityStream
import com.google.android.libraries.pcc.chronicle.storage.stream.EntityStreamProvider
import javax.annotation.concurrent.GuardedBy
import kotlin.reflect.KClass

/** Implementation of [EntityStreamProvider]. */
class EntityStreamProviderImpl : EntityStreamProvider {
  private val lock = Any()

  @GuardedBy("lock")
  private val streams = mutableMapOf<KClass<*>, EntityStream<*>>()

  override fun <T : Any> getStream(cls: KClass<out T>): EntityStream<T> {
    val stream = synchronized(lock) {
      streams.computeIfAbsent(cls) { EntityStreamImpl<T>() }
    }
    @Suppress("UNCHECKED_CAST") // Checked by virtue of the map.
    return stream as EntityStream<T>
  }
}
