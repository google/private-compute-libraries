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

package com.google.android.libraries.pcc.chronicle.storage.blobstore

import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity

/** Interface for developers to read/write to BlobStore. */
interface BlobStore<T> {
  /**
   * Writes entity of type [T] to storage.
   *
   * Both created and updated timestamps are handled by [BlobStore], so the provided timestamps in
   * the [WrappedEntity]'s [EntityMetadata] will be overwritten.
   *
   * For updates, only the update timestamp and the actual entity are updated. If the
   * [WrappedEntity]'s [EntityMetadata] require an update, the entry should be deleted first and
   * then reinserted.
   */
  suspend fun putEntity(wrappedEntity: WrappedEntity<T>)

  /**
   * Writes collection of entities of type [T] to storage.
   *
   * Both created and updated timestamps are handled by [BlobStore], so the provided timestamps in
   * the [WrappedEntity]'s [EntityMetadata] will be overwritten.
   *
   * For updates, only the update timestamp and the actual entity are updated. If the
   * [WrappedEntity]'s [EntityMetadata] require an update, the entry should be deleted first and
   * then reinserted.
   */
  suspend fun putEntities(wrappedEntities: Collection<WrappedEntity<T>>)

  /**
   * Retrieves entity of type [T] from storage using the entity's key. The id in the
   * [EntityMetadata] of a [WrappedEntity] is used as the key of the entity.
   */
  suspend fun getEntityByKey(key: String): WrappedEntity<T>?

  /** Retrieves all entities of type [T] from storage. */
  suspend fun getAllEntities(): List<WrappedEntity<T>>

  /** Removes entity of type [T] from storage using the entity's key. */
  suspend fun removeEntityByKey(key: String)

  /** Removes all entities of type [T] from storage. */
  suspend fun removeAll()
}
