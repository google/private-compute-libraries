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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.City
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.Person
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

// TODO(b/218400708): Add a test case which uses a parcelable with a directly-parceled PFD once
//  the bug is resolved.
@RunWith(AndroidJUnit4::class)
class RemoteEntityTest {
  @Test
  fun constructor_withProto_roundtrip() {
    val metadata = EntityMetadata.newBuilder().apply { id = "Sergey" }.build()
    val person =
      Person.newBuilder()
        .apply {
          name = "Sergey Brin"
          age = 37
          hometown = City.newBuilder().apply { name = "Mountain View" }.build()
        }
        .build()

    val entity = RemoteEntity.fromProto(metadata = metadata, message = person)

    val parcel = Parcel.obtain()
    try {
      entity.writeToParcel(parcel, 0)
      parcel.setDataPosition(0)
      val outEntity = RemoteEntity.CREATOR.createFromParcel(parcel)
      val outPerson = outEntity.interpretProtoEntity(Person::parseFrom)

      assertThat(outEntity.metadata).isEqualTo(metadata)
      assertThat(outPerson).isEqualTo(person)
    } finally {
      parcel.recycle()
    }
  }

  @Test
  fun constructor_withParcelable_roundtrip() = runBlocking {
    val metadata = EntityMetadata.newBuilder().setId("foo").build()
    val originalSimpleParcelable = SimpleParcelable(name = "Sergey Brin", age = 37)
    val entity =
      RemoteEntity.fromParcelable(metadata = metadata, parcelable = originalSimpleParcelable)

    val parcel = Parcel.obtain()
    try {
      entity.writeToParcel(parcel, 0)

      parcel.setDataPosition(0)

      val outEntity = RemoteEntity.CREATOR.createFromParcel(parcel)
      val outSimpleParcelable = outEntity.interpretParcelableEntity<SimpleParcelable>()

      assertThat(outEntity.metadata).isEqualTo(metadata)
      assertThat(outSimpleParcelable).isEqualTo(originalSimpleParcelable)
    } finally {
      parcel.recycle()
    }
  }

  @Test
  fun constructor_withProtoAndParcelable_roundTrip() = runBlocking {
    val metadata = EntityMetadata.newBuilder().setId("foo").build()
    val person =
      Person.newBuilder()
        .apply {
          name = "Sergey Brin"
          age = 37
          hometown = City.newBuilder().apply { name = "Mountain View" }.build()
        }
        .build()
    val originalSimpleParcelable = SimpleParcelable(name = "Sergey Brin", age = 37)
    val parcelableKey = "key"
    val parcelableKeyForNull = "nullKey"
    val entity =
      RemoteEntity(
        metadata = metadata,
        bytes = person.toByteArray(),
        bundle = Bundle().apply { putParcelable(parcelableKey, originalSimpleParcelable) },
      )

    val parcel = Parcel.obtain()
    try {
      entity.writeToParcel(parcel, 0)
      parcel.setDataPosition(0)

      val outEntity = RemoteEntity.CREATOR.createFromParcel(parcel)
      val outPerson = outEntity.interpretProtoEntity(Person::parseFrom)
      val outSimpleParcelable =
        outEntity.interpretParcelableFromBundle<SimpleParcelable>(parcelableKey)
      val outNullParcelable =
        outEntity.interpretParcelableFromBundle<SimpleParcelable>(parcelableKeyForNull)

      assertThat(outEntity.metadata).isEqualTo(metadata)
      assertThat(outPerson).isEqualTo(person)
      assertThat(outSimpleParcelable).isEqualTo(originalSimpleParcelable)
      assertThat(outNullParcelable).isNull()
    } finally {
      parcel.recycle()
    }
  }

  data class SimpleParcelable(val name: String, val age: Int) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
      parcel.writeString(name)
      parcel.writeInt(age)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SimpleParcelable> {
      override fun createFromParcel(parcel: Parcel): SimpleParcelable = SimpleParcelable(parcel)

      override fun newArray(size: Int): Array<SimpleParcelable?> = arrayOfNulls(size)
    }
  }
}
