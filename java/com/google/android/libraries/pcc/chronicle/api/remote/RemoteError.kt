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
import com.google.android.libraries.pcc.chronicle.api.error.ChronicleError

/**
 * A RemoteError is transmitted from server to client whenever an exception is thrown while
 * processing a request for a client. The [RemoteErrorMetadata] contains the type of error as well
 * as a message from the server.
 *
 * **Note:** [extras] exists purely as a mechanism to help support forwards compatibility if the
 * need were to arise where we would need to include additional Binders or ParcelFileDescriptors or
 * the like with a [RemoteError]. It should not be used to contain metadata about the response which
 * could otherwise be conveyed via the [RemoteErrorMetadata] proto.
 *
 * Adding, removing, or changing fields in this data class must be done with __extreme__ caution.
 */
data class RemoteError(val metadata: RemoteErrorMetadata, val extras: Bundle = Bundle.EMPTY) :
  Parcelable, ChronicleError(metadata.message) {
  constructor(
    type: RemoteErrorMetadata.Type,
    message: String,
  ) : this(RemoteErrorMetadata.newBuilder().setErrorType(type).setMessage(message).build())

  constructor(
    parcel: Parcel
  ) : this(
    metadata = RemoteErrorMetadata.parseFrom(requireNotNull(parcel.createByteArray())),
    extras = parcel.readBundle(RemoteError::class.java.classLoader) ?: Bundle.EMPTY,
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeByteArray(metadata.toByteArray())
    parcel.writeBundle(extras)
  }

  override fun describeContents(): Int = extras.describeContents()

  companion object CREATOR : Parcelable.Creator<RemoteError> {
    override fun createFromParcel(parcel: Parcel): RemoteError = RemoteError(parcel)

    override fun newArray(size: Int): Array<RemoteError?> = arrayOfNulls(size)
  }
}
