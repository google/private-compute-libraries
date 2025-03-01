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

package com.google.android.libraries.pcc.chronicle.api.remote.server

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow

/**
 * A [RemoteServer] variant providing simple CRUD (Create, Read, Update, Delete) access to a store
 * of data via Chronicle Remote Connections.
 */
interface RemoteStoreServer<T : Any> : RemoteServer<T> {
  // region: read methods
  /** Counts the entities currently contained within the store and returns the result. */
  suspend fun count(policy: Policy?): Int

  /**
   * Fetches a subset of the entities contained by the store, by their identifiers and returns a
   * [Flow] of pages (i.e. lists) of those entities wrapped with metadata.
   */
  fun fetchById(policy: Policy?, ids: List<String>): Flow<List<WrappedEntity<T>>>

  /**
   * Fetches all entities contained by the store and returns a [Flow] of pages (i.e. lists) of those
   * entities wrapped with metadata.
   */
  fun fetchAll(policy: Policy?): Flow<List<WrappedEntity<T>>>

  // endregion

  // region: write methods
  /**
   * Creates records in the store for all provided [wrappedEntities]. If an entity already exists
   * with a given [WrappedEntity.metadata.id], update it.
   */
  suspend fun create(policy: Policy?, wrappedEntities: List<WrappedEntity<T>>)

  /**
   * Updates records on the store for all provided [wrappedEntities]. If an entity does not exist in
   * the store for a particular [WrappedEntity.metadata.id], it will not be created.
   */
  suspend fun update(policy: Policy?, wrappedEntities: List<WrappedEntity<T>>)

  /** Deletes all entities contained by the store. */
  suspend fun deleteAll(policy: Policy?)

  /**
   * Deletes all entities from the store whose identifiers are represented in the provided list of
   * [ids].
   */
  suspend fun deleteById(policy: Policy?, ids: List<String>)
  // endregion
}
