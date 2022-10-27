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

package com.google.android.libraries.pcc.chronicle.analysis.impl.testdata

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import java.time.Duration

interface PersonReader : ReadConnection

interface PersonWriter : WriteConnection

interface PetReader : ReadConnection

interface PetWriter : WriteConnection

interface HouseholdReader : ReadConnection

interface HouseholdWriter : WriteConnection

interface BusinessReader : ReadConnection

interface BusinessWriter : WriteConnection

val PERSON_DESCRIPTOR =
  dataTypeDescriptor("Person", Unit::class) {
    "name" to FieldType.String
    "age" to FieldType.String
  }

val PET_DESCRIPTOR =
  dataTypeDescriptor("Pet", Unit::class) {
    "name" to FieldType.String
    "species" to FieldType.String
    "breed" to FieldType.String
  }

val BUSINESS_DESCRIPTOR =
  dataTypeDescriptor("Business", Unit::class) {
    "name" to FieldType.String
    "industry" to FieldType.String
  }

// An example with several nesting levels.
val HOUSEHOLD_DESCRIPTOR =
  dataTypeDescriptor("HouseHold", Unit::class) {
    "address" to FieldType.String
    // Note we are not using `Person` as it conflicts with PERSON_DESCRIPTOR.
    // (This will interfere with the [SchemaRegistry].)
    "person" to
      dataTypeDescriptor("HousePerson", HousePerson::class) {
        "name" to FieldType.String
        "age" to FieldType.Integer
        // Note we are not using `Pet` as it conflicts with PET_DESCRIPTOR.
        // (This will interfere with the [SchemaRegistry].)
        "pet" to dataTypeDescriptor("HousePet", HousePet::class) { "breed" to FieldType.String }
      }
  }

val PERSON_DATA_TYPE =
  ManagedDataType(
    descriptor = PERSON_DESCRIPTOR,
    managementStrategy = ManagementStrategy.PassThru,
    connectionTypes = setOf(PersonReader::class.java, PersonWriter::class.java)
  )

val PET_DATA_TYPE =
  ManagedDataType(
    descriptor = PET_DESCRIPTOR,
    managementStrategy =
      ManagementStrategy.Stored(
        encrypted = true,
        media = StorageMedia.LOCAL_DISK,
        ttl = Duration.ofMinutes(5)
      ),
    connectionTypes = setOf(PetReader::class.java, PetWriter::class.java)
  )

val HOUSEHOLD_DATA_TYPE =
  ManagedDataType(
    descriptor = HOUSEHOLD_DESCRIPTOR,
    managementStrategy =
      ManagementStrategy.Stored(
        encrypted = true,
        media = StorageMedia.MEMORY,
        ttl = Duration.ofMinutes(5)
      ),
    connectionTypes = setOf(HouseholdReader::class.java, HouseholdWriter::class.java)
  )

val PET_DATA_TYPE_UNENCRYPTED_DISK =
  ManagedDataType(
    descriptor = PET_DESCRIPTOR,
    managementStrategy =
      ManagementStrategy.Stored(
        encrypted = false,
        media = StorageMedia.LOCAL_DISK,
        ttl = Duration.ofMinutes(5)
      ),
    connectionTypes = setOf(PetReader::class.java, PetWriter::class.java)
  )

val BUSINESS_DATA_TYPE =
  ManagedDataType(
    descriptor = BUSINESS_DESCRIPTOR,
    managementStrategy =
      ManagementStrategy.Stored(encrypted = false, media = StorageMedia.REMOTE_DISK, ttl = null),
    connectionTypes = setOf(BusinessReader::class.java, BusinessWriter::class.java)
  )

data class HousePerson(val name: String, val age: Int, val pet: HousePet)

data class HousePet(val breed: String)

class PeopleReader : ProcessorNode {
  override val requiredConnectionTypes = setOf(PersonReader::class.java)
}

class PeopleWriter : ProcessorNode {
  override val requiredConnectionTypes = setOf(PersonWriter::class.java)
}

class HouseholdReaderNode : ProcessorNode {
  override val requiredConnectionTypes = setOf(HouseholdReader::class.java)
}

class PetProcessor : ProcessorNode {
  override val requiredConnectionTypes = setOf(PetReader::class.java, PersonWriter::class.java)
}

class CompanyWriter : ProcessorNode {
  override val requiredConnectionTypes = setOf(PersonReader::class.java, BusinessWriter::class.java)
}

class EmptyConnectionProvider : ConnectionProvider {
  override val dataType: DataType =
    ManagedDataType(
      descriptor = dataTypeDescriptor("dummy", Unit::class) {},
      managementStrategy =
        ManagementStrategy.Stored(encrypted = false, media = StorageMedia.REMOTE_DISK, ttl = null),
      connectionTypes = emptySet()
    )

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
    throw UnsupportedOperationException("Not used")
}

class BusinessConnectionProvider : ConnectionProvider {
  override val dataType: DataType = BUSINESS_DATA_TYPE

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
    throw UnsupportedOperationException("Not used")
}

class PersonConnectionProvider : ConnectionProvider {
  override val dataType: DataType = PERSON_DATA_TYPE

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
    throw UnsupportedOperationException("Not used")
}

class PetConnectionProvider : ConnectionProvider {
  override val dataType: DataType = PET_DATA_TYPE

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
    throw UnsupportedOperationException("Not used")
}

class HouseholdConnectionProvider : ConnectionProvider {
  override val dataType: DataType = HOUSEHOLD_DATA_TYPE

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
    throw UnsupportedOperationException("Not used")
}

class UnencryptedPetConnectionProvider : ConnectionProvider {
  override val dataType: DataType = PET_DATA_TYPE_UNENCRYPTED_DISK

  override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
    throw UnsupportedOperationException("Not used")
}
