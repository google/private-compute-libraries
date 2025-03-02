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

package com.google.android.libraries.pcc.chronicle.api.remote.serialization

import android.os.Parcel
import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParcelableSerializerTest {
  @Test
  fun serialize_deserialize_roundTrip() {
    val serializer = ParcelableSerializer.createFrom(Person::class)
    val parcel = Parcel.obtain()
    val entity = Person(name = "Sergey Brin", age = 42, hometown = City(name = "Mountain View"))
    val metadata =
      EntityMetadata(
        id = "sergey",
        associatedPackageName = "com.google.android.as.oss",
        created = Instant.now().minusSeconds(1337),
        updated = Instant.now(),
      )

    serializer.serialize(WrappedEntity(metadata, entity)).writeToParcel(parcel, 0)
    parcel.setDataPosition(0)
    val deserialized = serializer.deserialize<Person>(RemoteEntity.CREATOR.createFromParcel(parcel))

    assertThat(deserialized.entity).isEqualTo(entity)
    assertThat(deserialized.metadata).isEqualTo(metadata)
  }

  data class Person(val name: String, val age: Int, val hometown: City) : Parcelable {
    constructor(
      parcel: Parcel
    ) : this(
      parcel.readString()!!,
      parcel.readInt(),
      parcel.readParcelable(City::class.java.classLoader)!!,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
      parcel.writeString(name)
      parcel.writeInt(age)
      parcel.writeParcelable(hometown, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Person> {
      override fun createFromParcel(parcel: Parcel): Person = Person(parcel)

      override fun newArray(size: Int): Array<Person?> = arrayOfNulls(size)
    }
  }

  data class City(val name: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
      parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<City> {
      override fun createFromParcel(parcel: Parcel): City = City(parcel)

      override fun newArray(size: Int): Array<City?> = arrayOfNulls(size)
    }
  }
}
