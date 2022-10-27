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

import android.os.Parcelable
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.interpretParcelableEntity
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlin.reflect.KClass

/**
 * Implementation of [Serializer] dealing with [Parcelables][Parcelable] of type [T].
 *
 * **Note:** Do not use parcelables without fully understanding the version skew and conditional
 * usage ramifications of doing so.
 */
class ParcelableSerializer<T : Parcelable> private constructor() : Serializer<T> {
  override fun <P : T> serialize(wrappedEntity: WrappedEntity<P>): RemoteEntity =
    RemoteEntity.fromParcelable(
      metadata = wrappedEntity.metadata,
      parcelable = wrappedEntity.entity
    )

  override fun <P : T> deserialize(remoteEntity: RemoteEntity): WrappedEntity<P> =
    WrappedEntity(
      metadata = remoteEntity.metadata,
      entity = remoteEntity.interpretParcelableEntity()
    )

  companion object {
    /** Creates a [Serializer] from the given [KClass] for a [Parcelable]. */
    fun <T : Parcelable> createFrom(cls: KClass<T>): Serializer<T> = ParcelableSerializer()
  }
}
