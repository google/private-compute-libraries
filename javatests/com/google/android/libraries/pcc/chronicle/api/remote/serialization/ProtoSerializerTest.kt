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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.City
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.Person
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtoSerializerTest {
  @Test
  fun serialize_deserialize_roundTrip() {
    val serializer = ProtoSerializer.createFrom(Person.getDefaultInstance())
    val parcel = Parcel.obtain()
    val entity = Person.newBuilder()
      .apply {
        name = "Sergey Brin"
        age = 42
        hometown = City.newBuilder()
          .apply { name = "Mountain View" }
          .build()
      }
      .build()
    val metadata = EntityMetadata(
      id = "sergey",
      associatedPackageName = "com.google.android.as.oss",
      created = Instant.now().minusSeconds(1337),
      updated = Instant.now()
    )

    serializer.serialize(WrappedEntity(metadata, entity))
      .writeToParcel(parcel, 0)
    parcel.setDataPosition(0)
    val deserialized = serializer.deserialize<Person>(RemoteEntity.CREATOR.createFromParcel(parcel))

    assertThat(deserialized.entity).isEqualTo(entity)
    assertThat(deserialized.metadata).isEqualTo(metadata)
  }
}
