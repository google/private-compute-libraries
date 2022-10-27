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

import android.content.Context
import androidx.room.Room
import com.google.android.libraries.pcc.chronicle.storage.blobstore.db.BlobDatabase
import com.google.android.libraries.pcc.chronicle.storage.blobstore.inmemory.InMemoryBlobStore
import com.google.android.libraries.pcc.chronicle.storage.blobstore.inmemory.InMemoryBlobStoreManagement
import com.google.android.libraries.pcc.chronicle.storage.blobstore.inmemory.InMemoryStorage
import com.google.android.libraries.pcc.chronicle.storage.blobstore.persisted.PersistedBlobStore
import com.google.android.libraries.pcc.chronicle.storage.blobstore.persisted.PersistedBlobStoreManagement
import com.google.android.libraries.pcc.chronicle.util.TimeSource

/**
 * Entry point to BlobStore. Data stewards will use this class to create their [BlobStores]
 * [BlobStore]. This class also creates the [BlobStoreManager], which handles logistics around
 * enforcing TTL and quota limits, clearing data according to user settings, and cleaning up data
 * when packages are uninstalled.
 */
class BlobStoreCore(context: Context, private val timeSource: TimeSource) : BlobStoreProvider {
  private val db = Room.databaseBuilder(context, BlobDatabase::class.java, DB_NAME).build()
  private val dao = db.blobDao()

  private val inMemoryStorage = InMemoryStorage()

  private val manager =
    BlobStoreManager(
      timeSource = timeSource,
      managements =
        setOf(PersistedBlobStoreManagement(dao), InMemoryBlobStoreManagement(inMemoryStorage))
    )

  /** Provides a [BlobStore] based on the given [ManagementInfo]. */
  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> provideBlobStore(managementInfo: ManagementInfo): BlobStore<T> {
    if (managementInfo is PersistedManagementInfo<*>) {
      require(managementInfo.quotaInfo.maxRowCount > managementInfo.quotaInfo.minRowsAfterTrim) {
        "maxRowCount must be greater than minRowsAfterTrim."
      }
    }

    val checkInfo = manager.addManagementInfo(managementInfo)
    if (
      (checkInfo != null) &&
        ((checkInfo == managementInfo) || (checkInfo.javaClass == managementInfo.javaClass))
    ) {
      return when (checkInfo) {
        is PersistedManagementInfo<*> ->
          PersistedBlobStore(dao, checkInfo, timeSource) as BlobStore<T>
        is InMemoryManagementInfo ->
          InMemoryBlobStore(inMemoryStorage.registerDataTypeStore(checkInfo), timeSource)
      }
    }

    throw IllegalArgumentException(
      "Persisted and in memory blob stores for the same DTD is not allowed."
    )
  }

  /**
   * Returns the [BlobStoreManager], which provides maintenance and clean up services for
   * [BlobStores][BlobStore].
   */
  fun provideManager(): BlobStoreManager = manager

  companion object {
    private const val DB_NAME = "ChronicleBlobStore"
  }
}
