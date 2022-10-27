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

/** This interface is the main entry point for devs wanting to use [BlobStore]. */
interface BlobStoreProvider {
  /**
   * Returns a [BlobStore] instance using the provided [ManagementInfo] to create it.
   *
   * Pass a [PersistedManagementInfo] for a [BlobStore] that persists data to disk and an
   * [InMemoryManagementInfo] for a [BlobStore] that keeps data in a cache.
   */
  fun <T : Any> provideBlobStore(managementInfo: ManagementInfo): BlobStore<T>
}
