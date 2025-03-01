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

import com.google.protobuf.MessageLite

sealed class ManagementInfo {
  abstract val dtdName: String
  abstract val ttlMillis: Long
}

/**
 * Represents the quota and ttl information for a specific data type for BlobStore to use to ensure
 * persisted data is deleted correctly.
 */
data class PersistedManagementInfo<T : MessageLite>(
  override val dtdName: String,
  override val ttlMillis: Long,
  val quotaInfo: QuotaInfo,
  val deserializer: (ByteArray) -> T,
) : ManagementInfo()

/**
 * Represents the quota and ttl information for a specific data type for BlobStore to use to ensure
 * in memory data is deleted correctly.
 */
data class InMemoryManagementInfo(
  override val dtdName: String,
  override val ttlMillis: Long,
  val maxItems: Int,
) : ManagementInfo()

/** Represents the quota information for BlobStore to use to trim rows in storage. */
data class QuotaInfo(val maxRowCount: Int, val minRowsAfterTrim: Int, val trimOrder: TrimOrder)

/** Enum indicating whether oldest or newest entries in storage should be deleted first. */
enum class TrimOrder {
  NEWEST,
  OLDEST,
}
