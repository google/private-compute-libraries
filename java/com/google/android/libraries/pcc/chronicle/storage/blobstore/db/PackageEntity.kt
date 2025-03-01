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

package com.google.android.libraries.pcc.chronicle.storage.blobstore.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/** Defines a table for storing Packages. */
@Entity(
  tableName = "packages",
  foreignKeys =
    [
      // **BLOB DELETION PROPAGATION**
      // This foreign key ensures that entities are automatically removed when the corresponding
      // blob is deleted.
      ForeignKey(
        entity = BlobEntity::class,
        parentColumns = ["id"],
        childColumns = ["blobId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
      )
    ],
  primaryKeys = ["blobId", "packageName"],
)
data class PackageEntity(
  /** The blob id associated with this package entity. */
  @ColumnInfo(name = BLOB_ID) val blobId: Long,
  /** The name of the package. */
  @ColumnInfo(name = PACKAGE_NAME) val packageName: String,
) {
  companion object {
    const val TABLE_NAME = "packages"
    const val BLOB_ID = "blobId"
    const val PACKAGE_NAME = "packageName"
  }
}
