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

package com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.client

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.remote.client.DefaultRemoteStoreClient
import com.google.android.libraries.pcc.chronicle.api.remote.client.Transport
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.ProtoSerializer
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PERSON_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PeopleReader
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PeopleWriter
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.Person
import java.time.Duration

/**
 * A [ConnectionProvider] implementation for a client process which needs [PeopleWriter] /
 * [PeopleReader] remote connections to a server (accessed via the provided [transport]).
 */
class PeopleClientConnectionProvider(transport: Transport) : ConnectionProvider {
  private val client =
    DefaultRemoteStoreClient(
      dataTypeName = PERSON_GENERATED_DTD.name,
      serializer = ProtoSerializer.createFrom(Person.getDefaultInstance()),
      transport = transport
    )
  override val dataType =
    ManagedDataType(
      descriptor = PERSON_GENERATED_DTD,
      managementStrategy =
        ManagementStrategy.Stored(
          encrypted = false,
          media = StorageMedia.LOCAL_DISK,
          ttl = Duration.ofDays(14)
        ),
      PeopleReader::class,
      PeopleWriter::class
    )

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
    return when (connectionRequest.connectionType) {
      PeopleReader::class.java ->
        PeopleReaderClient(client, checkNotNull(connectionRequest.policy))
      PeopleWriter::class.java -> PeopleWriterClient(client)
      else -> throw NotImplementedError("No support for ${connectionRequest.connectionType}")
    }
  }
}
