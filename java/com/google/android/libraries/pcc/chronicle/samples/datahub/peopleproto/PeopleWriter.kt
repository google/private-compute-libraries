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

package com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto

import com.google.android.libraries.pcc.chronicle.api.WriteConnection

/**
 * Interface describing a [WriteConnection] capable of storing and deleting [Person] data.
 *
 * All methods are `suspend` methods because they could involve IPC or disk access (or both)
 * depending on the implementation of the back-end.
 */
interface PeopleWriter : WriteConnection {
  /**
   * Creates or updates a given [Person].
   *
   * The value of [Person.getName] is used as a unique identifier for the Person.
   */
  suspend fun putPerson(person: Person)

  /** Deletes a [Person] with a given [name] from storage. */
  suspend fun deletePerson(name: String)

  /** Deletes all [Person] data. */
  suspend fun deleteAll()
}
