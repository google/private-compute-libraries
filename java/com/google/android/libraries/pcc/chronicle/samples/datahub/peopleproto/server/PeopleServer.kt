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

package com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.server

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.ProtoSerializer
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStoreServer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PERSON_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PeopleReader
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PeopleWriter
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.Person
import java.time.Duration
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [RemoteStoreServer] for managing [Person] data.
 *
 * In addition to being a server for remote connections accessing [Person] data, this implementation
 * also provides local-to-the-server connections by overriding [RemoteStoreServer.getConnection] and
 * returning implementations of [PeopleReader] and [PeopleWriter] for use by feature developers
 * running on the same process as this server.
 */
class PeopleServer(private val store: PeopleStore) : RemoteStoreServer<Person> {
  private val ttl = Duration.ofDays(14)

  override val dataType =
    ManagedDataType(
      descriptor = PERSON_GENERATED_DTD,
      managementStrategy =
        ManagementStrategy.Stored(encrypted = false, media = StorageMedia.LOCAL_DISK, ttl = ttl),
      PeopleReader::class,
      PeopleWriter::class,
    )
  override val dataTypeDescriptor = dataType.descriptor
  override val serializer = ProtoSerializer.createFrom(Person.getDefaultInstance())

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
    return when (connectionRequest.connectionType) {
      PeopleReader::class.java -> ServerLocalPeopleReader(store)
      PeopleWriter::class.java -> ServerLocalPeopleWriter(store)
      else -> throw IllegalArgumentException("$connectionRequest not supported")
    }
  }

  override suspend fun count(policy: Policy?): Int = store.count(policy?.findPersonTtl())

  override fun fetchById(policy: Policy?, ids: List<String>): Flow<List<WrappedEntity<Person>>> =
    store.fetchByName(ids, policy.findPersonTtl())

  override fun fetchAll(policy: Policy?): Flow<List<WrappedEntity<Person>>> =
    store.fetchAll(policy.findPersonTtl())

  override suspend fun create(policy: Policy?, wrappedEntities: List<WrappedEntity<Person>>) {
    store.putPeople(wrappedEntities)
  }

  override suspend fun update(policy: Policy?, wrappedEntities: List<WrappedEntity<Person>>) {
    store.putPeople(wrappedEntities)
  }

  override suspend fun deleteAll(policy: Policy?) {
    store.deleteAll()
  }

  override suspend fun deleteById(policy: Policy?, ids: List<String>) {
    store.removePeople(ids)
  }

  private fun Policy?.findPersonTtl(): Duration? {
    return this?.targets
      ?.find { it.schemaName == dataTypeDescriptor.name }
      ?.maxAgeMs
      ?.let { Duration.ofMillis(it) }
  }
}
