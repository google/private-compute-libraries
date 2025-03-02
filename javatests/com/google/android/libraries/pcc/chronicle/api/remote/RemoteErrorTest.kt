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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata.Type.POLICY_VIOLATION
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteErrorTest {
  @Test
  fun writeToParcel_createFromParcel_roundtrip() {
    val metadata =
      RemoteErrorMetadata.newBuilder()
        .setErrorType(POLICY_VIOLATION)
        .setMessage("A policy was invalid!")
        .build()
    val extras = Bundle().apply { putString("MyString", "MyStringValue") }

    val request = RemoteError(metadata, extras)

    val parcel = Parcel.obtain()
    val output =
      try {
        request.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        RemoteError.CREATOR.createFromParcel(parcel).also { it.extras.keySet() }
      } finally {
        parcel.recycle()
      }

    Truth.assertThat(output.metadata).isEqualTo(request.metadata)
    Truth.assertThat(output.extras.getString("MyString"))
      .isEqualTo(request.extras.getString("MyString"))
  }
}
