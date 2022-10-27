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

/** Interface for the management of a [BlobStore]. */
interface BlobStoreManagement {
  /** Deletes all entities from storage. Returns the number of rows deleted. */
  suspend fun clearAll(): Int

  /**
   * Deletes all entities that were created in a given time range from storage. Returns the number
   * of rows deleted.
   */
  suspend fun deleteEntitiesCreatedBetween(startTimeMillis: Long, endTimeMillis: Long): Int

  /** Deletes all expired entities from storage. */
  suspend fun deleteExpiredEntities(currentTimeMillis: Long, managementInfos: Set<ManagementInfo>)

  /**
   * Deletes a number of entities from storage of specified type based on the provided
   * [managementInfos].
   */
  suspend fun trim(managementInfos: Set<ManagementInfo>)

  /**
   * Deletes specified package and all associated entities from storage. Returns the number of rows
   * deleted.
   */
  suspend fun deletePackage(packageName: String): Int

  /**
   * Reconciles packages stored and associated entities with allowed packages. Returns the number of
   * rows deleted.
   */
  suspend fun reconcilePackages(allowedPackages: Set<String>): Int
}
