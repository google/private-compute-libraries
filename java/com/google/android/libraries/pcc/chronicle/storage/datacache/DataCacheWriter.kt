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

import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import java.time.Instant

/** The chronicle default writer interface for [DataCache]. */
interface DataCacheWriter<T> : WriteConnection {
  fun write(entity: T, entityId: String, ttlTimestamp: Instant, packageName: String)

  fun remove(id: String)

  companion object {
    /** Creates a [DataCacheWriter] connection that reads from a [TypedDataCacheWriter<T>]. */
    fun <T> createDefault(
      cache: TypedDataCacheWriter<T>,
      timeSource: TimeSource,
    ): DataCacheWriter<T> {
      return object : DataCacheWriter<T> {
        override fun write(
          entity: T,
          entityId: String,
          ttlTimestamp: Instant,
          packageName: String,
        ) {
          cache.put(
            WrappedEntity(
              metadata = EntityMetadata(entityId, packageName, ttlTimestamp, timeSource.now()),
              entity = entity,
            )
          )
        }

        override fun remove(id: String) {
          cache.remove(id)
        }
      }
    }
  }
}
