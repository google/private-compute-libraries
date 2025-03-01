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

/**
 * This class encapsulates both [metadata] about the response as well as any appropriate payload, in
 * the form of [entities] and [extras].
 *
 * **Note:** [extras] exists purely as a mechanism to help support forwards compatibility if the
 * need were to arise where we would need to include additional Binders or ParcelFileDescriptors or
 * the like with a [RemoteResponse]. It should not be used to contain metadata about the response
 * which could otherwise be conveyed via the [RemoteResponseMetadata] proto.
 *
 * Adding, removing, or changing fields in this data class must be done with __extreme__ caution.
 */
data class RemoteResponse(
  val metadata: RemoteResponseMetadata,
  val entities: List<RemoteEntity> = emptyList(),
  val extras: Bundle = Bundle.EMPTY,
) : Parcelable {
  constructor(
    parcel: Parcel
  ) : this(
    metadata = RemoteResponseMetadata.parseFrom(requireNotNull(parcel.createByteArray())),
    entities = parcel.createTypedArrayList(RemoteEntity.CREATOR) ?: emptyList(),
    extras = parcel.readBundle(RemoteResponse::class.java.classLoader) ?: Bundle.EMPTY,
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeByteArray(metadata.toByteArray())
    parcel.writeTypedList(entities)
    parcel.writeBundle(extras)
  }

  override fun describeContents(): Int {
    val entityContents =
      entities.fold(0) { acc, remoteEntity -> acc or remoteEntity.describeContents() }
    return entityContents or extras.describeContents()
  }

  companion object {
    @JvmField
    val CREATOR =
      object : Parcelable.Creator<RemoteResponse> {
        override fun createFromParcel(parcel: Parcel): RemoteResponse = RemoteResponse(parcel)

        override fun newArray(size: Int): Array<RemoteResponse?> = arrayOfNulls(size)
      }
  }
}
