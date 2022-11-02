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

import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PeopleWriter
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.Person
import java.time.Instant

internal class ServerLocalPeopleWriter(private val peopleStore: PeopleStore) : PeopleWriter {
  override suspend fun putPerson(person: Person) {
    peopleStore.putPeople(
      listOf(WrappedEntity(EntityMetadata(person.name, emptyList(), Instant.now()), person))
    )
  }

  override suspend fun deletePerson(name: String) {
    peopleStore.removePeople(listOf(name))
  }

  override suspend fun deleteAll() {
    peopleStore.deleteAll()
  }
}
