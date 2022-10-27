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

import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity

/**
 * Defines a serialization/deserialization mechanism for converting objects of type [T] into
 * [RemoteEntities][RemoteEntity] and back.
 */
interface Serializer<T : Any> {
  /** Serializes the [T]-typed [entity] into a [RemoteEntity]. */
  fun <P : T> serialize(wrappedEntity: WrappedEntity<P>): RemoteEntity
  /** Deserializes the [remoteEntity] into a [T] instance. */
  fun <P : T> deserialize(remoteEntity: RemoteEntity): WrappedEntity<P>
}
