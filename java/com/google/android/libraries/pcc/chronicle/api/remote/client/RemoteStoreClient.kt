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

package com.google.android.libraries.pcc.chronicle.api.remote.client

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow

/** Defines a [RemoteClient] intended for communicating with a [RemoteStoreServer]. */
interface RemoteStoreClient<T : Any> : RemoteClient<T> {
  /** Storage: Gets a count of entities. */
  suspend fun count(policy: Policy?): Int

  /**
   * Storage: Fetches all entities in the store and returns them as a flow (automatically unwrapping
   * pages, if multiple pages are sent in the response).
   */
  fun fetchAll(policy: Policy?): Flow<WrappedEntity<T>>

  /**
   * Storage: Fetches all entities in the store whose identifiers (from [WrappedEntity.metadata])
   * are represented by those in the [ids] list and returns them as a flow (automatically unwrapping
   * pages, if multiple pages are sent in the response).
   */
  fun fetchById(policy: Policy?, ids: List<String>): Flow<WrappedEntity<T>>

  /** Storage: Deletes all entities in the store. */
  suspend fun deleteAll(policy: Policy?)

  /**
   * Storage: Deletes all entities in the store whose identifiers (from [WrappedEntity.metadata])
   * are respresented by those in the [ids] list.
   */
  suspend fun deleteById(policy: Policy?, ids: List<String>)

  /**
   * Storage: Creates new records from the provided [entities]. If an entity already exists with a
   * given [WrappedEntity.metadata] identifier, it will be updated.
   */
  suspend fun create(policy: Policy?, entities: List<WrappedEntity<T>>)

  /**
   * Storage: Updates existing records from the provided [entities]. If no record exists with an
   * entity's identifier (from [WrappedEntity.metadata]), it will be skipped - in other words: no
   * *new* entities will be created.
   */
  suspend fun update(policy: Policy?, entities: List<WrappedEntity<T>>)
}
