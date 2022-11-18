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

package com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.proxy

import android.util.Log
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.client.DefaultRemoteStoreClient
import com.google.android.libraries.pcc.chronicle.api.remote.client.Transport
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.ProtoSerializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PERSON_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Defines a [RemoteStoreServer] that itself uses a [RemoteStoreClient] (via the provided
 * [Transport]) to proxy data to its own clients from another server.
 */
class PeopleProxy(
  transport: Transport,
  private val pageSize: Int = 10,
) : RemoteStoreServer<Person> {
  override val dataType: DataType =
    ManagedDataType(PERSON_GENERATED_DTD, ManagementStrategy.PassThru)
  override val dataTypeDescriptor: DataTypeDescriptor = PERSON_GENERATED_DTD
  override val serializer = ProtoSerializer.createFrom(Person.getDefaultInstance())

  private val remoteClient =
    DefaultRemoteStoreClient(PERSON_GENERATED_DTD.name, serializer, transport)

  override suspend fun count(policy: Policy?): Int {
    Log.i("ChroniclePeopleProxy", "Proxying count Request to Server")
    return remoteClient.count(policy)
  }

  override fun fetchById(policy: Policy?, ids: List<String>): Flow<List<WrappedEntity<Person>>> {
    Log.i("ChroniclePeopleProxy", "Proxying fetchById Request to Server")
    return remoteClient.fetchById(policy, ids).paginate(pageSize)
  }

  override fun fetchAll(policy: Policy?): Flow<List<WrappedEntity<Person>>> {
    Log.i("ChroniclePeopleProxy", "Proxying fetchAll Request to Server")
    return remoteClient.fetchAll(policy).paginate(pageSize)
  }

  override suspend fun create(policy: Policy?, wrappedEntities: List<WrappedEntity<Person>>) {
    Log.i("ChroniclePeopleProxy", "Proxying create Request to Server")
    remoteClient.create(policy, wrappedEntities)
  }

  override suspend fun update(policy: Policy?, wrappedEntities: List<WrappedEntity<Person>>) {
    Log.i("ChroniclePeopleProxy", "Proxying update Request to Server")
    remoteClient.update(policy, wrappedEntities)
  }

  override suspend fun deleteAll(policy: Policy?) {
    Log.i("ChroniclePeopleProxy", "Proxying deleteAll Request to Server")
    remoteClient.deleteAll(policy)
  }

  override suspend fun deleteById(policy: Policy?, ids: List<String>) {
    Log.i("ChroniclePeopleProxy", "Proxying deleteByid Request to Server")
    remoteClient.deleteById(policy, ids)
  }

  /**
   * Chunks the Flow of [T] into a Flow of lists of length [pageSize] of [T]. The last page may be
   * smaller than [pageSize].
   */
  private fun <T> Flow<T>.paginate(pageSize: Int): Flow<List<T>> {
    return flow {
      var buffer = mutableListOf<T>()
      collect { item ->
        buffer.add(item)
        if (buffer.size == pageSize) {
          emit(buffer)
          buffer = mutableListOf()
        }
      }
      if (buffer.isNotEmpty()) emit(buffer)
    }
  }
}
