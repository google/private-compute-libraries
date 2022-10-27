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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Defines a table for storing Blob Entities. */
@Entity(tableName = "blobs", indices = [Index(value = ["key", "dtdName"], unique = true)])
data class BlobEntity(
  /** Unique ID for this blob entity, auto generated if not provided. */
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  /** Key provided and used by developer. */
  @ColumnInfo(name = KEY) val key: String,
  /** Locus ID associated with the blob. */
  @ColumnInfo(name = LOCUS_ID) val locusId: String,
  /** The time the blob was created. */
  @ColumnInfo(name = CREATED_TIMESTAMP_MILLIS) val createdTimestampMillis: Long,
  /** The time the blob was last updated. */
  @ColumnInfo(name = UPDATE_TIMESTAMP_MILLIS) val updateTimestampMillis: Long,
  /** The name of the blob data type. */
  @ColumnInfo(name = DTD_NAME) val dtdName: String,
  /** The serialized blob. */
  @ColumnInfo(name = BLOB) val blob: ByteArray,
) {
  companion object {
    const val TABLE_NAME = "blobs"
    const val ID = "id"
    const val KEY = "key"
    const val LOCUS_ID = "locusId"
    const val CREATED_TIMESTAMP_MILLIS = "createdTimestampMillis"
    const val UPDATE_TIMESTAMP_MILLIS = "updateTimestampMillis"
    const val DTD_NAME = "dtdName"
    const val BLOB = "blob"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BlobEntity) return false

    if (id != other.id) return false
    if (key != other.key) return false
    if (locusId != other.locusId) return false
    if (createdTimestampMillis != other.createdTimestampMillis) return false
    if (updateTimestampMillis != other.updateTimestampMillis) return false
    if (dtdName != other.dtdName) return false
    if (!blob.contentEquals(other.blob)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + key.hashCode()
    result = 31 * result + locusId.hashCode()
    result = 31 * result + createdTimestampMillis.hashCode()
    result = 31 * result + updateTimestampMillis.hashCode()
    result = 31 * result + dtdName.hashCode()
    result = 31 * result + blob.contentHashCode()
    return result
  }
}

/**
 * Intermediate data class representing a [BlobEntity] and its associated packages from the Package
 * table.
 */
data class BlobEntityWithPackages(
  /** The blob entity. */
  @Embedded val blobEntity: BlobEntity,
  /** List of packages associated with the blob entity. */
  @Relation(parentColumn = "id", entityColumn = "blobId") val packages: List<PackageEntity>
)

/**
 * Intermediate data class representing a partial [BlobEntity] that can be used to update an
 * existing entry.
 */
data class UpdateBlobEntity(val id: Long, val updateTimestampMillis: Long, val blob: ByteArray) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is UpdateBlobEntity) return false

    if (id != other.id) return false
    if (updateTimestampMillis != other.updateTimestampMillis) return false
    if (!blob.contentEquals(other.blob)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + updateTimestampMillis.hashCode()
    result = 31 * result + blob.contentHashCode()
    return result
  }
}
