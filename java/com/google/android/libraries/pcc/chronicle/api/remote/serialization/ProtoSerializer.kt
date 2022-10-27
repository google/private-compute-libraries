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
import com.google.android.libraries.pcc.chronicle.api.remote.interpretProtoEntity
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.protobuf.MessageLite

/** Implementation of [Serializer] supporting protos of type [T]. */
class ProtoSerializer<T : MessageLite> private constructor(
  private val defaultInstance: T
) : Serializer<T> {

  override fun <P : T> serialize(wrappedEntity: WrappedEntity<P>): RemoteEntity =
    RemoteEntity.fromProto(metadata = wrappedEntity.metadata, message = wrappedEntity.entity)

  @Suppress("UNCHECKED_CAST")
  override fun <P : T> deserialize(remoteEntity: RemoteEntity): WrappedEntity<P> {
    return WrappedEntity(
      metadata = remoteEntity.metadata,
      entity = remoteEntity.interpretProtoEntity {
        defaultInstance.parserForType.parseFrom(it) as P
      },
    )
  }

  companion object {
    /**
     * Creates a [Serializer] for the [MessageLite] type [T] using the provided [defaultInstance].
     */
    fun <T : MessageLite> createFrom(defaultInstance: T): Serializer<T> =
      ProtoSerializer(defaultInstance)
  }
}
