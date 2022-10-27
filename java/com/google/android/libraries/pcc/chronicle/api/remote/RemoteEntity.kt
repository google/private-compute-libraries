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

package com.google.android.libraries.pcc.chronicle.api.remote

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.protobuf.MessageLite

/**
 * A wrapper for an entity intended to be passed between processes by remote Chronicle.
 *
 * @property metadata [EntityMetadata] associated with the data contained in [bytes] and [bundle].
 * @property bytes Serialized data, may be empty.
 * @property bundle Parcelable data, may be empty.
 */
class RemoteEntity(
  val metadata: EntityMetadata = EMPTY_METADATA,
  internal val bytes: ByteArray = EMPTY_BYTES,
  internal val bundle: Bundle = Bundle.EMPTY,
) : Parcelable {
  private constructor(
    parcel: Parcel
  ) : this(
    metadata = EntityMetadata.parseFrom(requireNotNull(parcel.createByteArray())),
    bytes = parcel.createByteArray() ?: EMPTY_BYTES,
    bundle = parcel.readBundle(RemoteEntity::class.java.classLoader) ?: Bundle.EMPTY
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    if (metadata == EMPTY_METADATA) {
      parcel.writeByteArray(EMPTY_METADATA_BYTES)
    } else {
      parcel.writeByteArray(metadata.toByteArray())
    }
    parcel.writeByteArray(bytes)
    parcel.writeBundle(bundle)
  }

  override fun describeContents(): Int {
    // We delegate to the bundle, in case it contains a file descriptor.
    return bundle.describeContents()
  }

  companion object {
    private val EMPTY_METADATA = EntityMetadata.getDefaultInstance()
    private val EMPTY_METADATA_BYTES = EMPTY_METADATA.toByteArray()
    private val EMPTY_BYTES = ByteArray(0)
    internal const val PARCELABLE_KEY = "___data"

    /**
     * Creates a [RemoteEntity] from a [Parcelable].
     *
     * When this constructor is used, [bundle] will contain the [parcelable] and [bytes] will be
     * empty.
     *
     * **Note:** Do not use parcelables without fully understanding the consequences first.
     */
    fun fromParcelable(metadata: EntityMetadata, parcelable: Parcelable): RemoteEntity {
      return RemoteEntity(
        metadata = metadata,
        bundle = Bundle().apply { putParcelable(PARCELABLE_KEY, parcelable) }
      )
    }

    /**
     * Creates a [RemoteEntity] from a [MessageLite] (a proto).
     *
     * When this constructor is used, [bytes] will contain the serialized [proto] and [bundle] will
     * be empty.
     */
    fun fromProto(metadata: EntityMetadata, message: MessageLite): RemoteEntity {
      return RemoteEntity(metadata = metadata, bytes = message.toByteArray())
    }

    @JvmField
    val CREATOR =
      object : Parcelable.Creator<RemoteEntity> {
        override fun createFromParcel(parcel: Parcel): RemoteEntity = RemoteEntity(parcel)
        override fun newArray(size: Int): Array<RemoteEntity?> = arrayOfNulls(size)
      }
  }
}

/**
 * Returns a [Parcelable] [T] interpretation of the [RemoteEntity]'s contents.
 *
 * For use when `RemoteEntity(EntityMetadata, Parcelable)` was the constructor used to create the
 * [RemoteEntity].
 */
fun <T : Parcelable> RemoteEntity.interpretParcelableEntity(): T =
  requireNotNull(bundle.getParcelable(RemoteEntity.PARCELABLE_KEY))

/** Returns a [Parcelable] [T] interpretation of the given [key] from [RemoteEntity]'s bundle. */
fun <T : Parcelable> RemoteEntity.interpretParcelableFromBundle(key: String): T? =
  bundle.getParcelable(key)

/**
 * Returns a proto [T] interpretation of the [RemoteEntity]'s contents.
 *
 * For use when `RemoteEntity(EntityMetadata, MessageLite)` was the constructor used to create the
 * [RemoteEntity].
 */
fun <T : MessageLite> RemoteEntity.interpretProtoEntity(deserializer: (ByteArray) -> T): T =
  deserializer(bytes)
