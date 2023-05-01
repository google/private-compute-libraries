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

package com.google.android.libraries.pcc.chronicle.analysis.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.analysis.DefaultChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.DefaultPolicySet
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.BusinessConnectionProvider
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.HOUSEHOLD_DESCRIPTOR
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.HouseholdConnectionProvider
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.HouseholdReaderNode
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PERSON_DESCRIPTOR
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PET_DESCRIPTOR
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PeopleReader
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PersonConnectionProvider
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PersonReader
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PersonWriter
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PetConnectionProvider
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PetProcessor
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PetReader
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.PetWriter
import com.google.android.libraries.pcc.chronicle.analysis.impl.testdata.UnencryptedPetConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.SandboxProcessorNode
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.integration.DefaultDataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheckResult
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import com.google.android.libraries.pcc.chronicle.util.Key
import com.google.android.libraries.pcc.chronicle.util.MutableTypedMap
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class ChroniclePolicyEngineTest {
  private val engine = ChroniclePolicyEngine()

  object NameKey : Key<String>
  object NameRule : PolicyContextRule {
    override val name: String = "NameRule"
    override val operands: List<PolicyContextRule> = emptyList()
    override fun invoke(context: TypedMap): Boolean {
      return "test" == context[NameKey]
    }
  }

  @Test
  fun checkPolicy_succeeds() {
    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = EVERYTHING_PERSON_ALLOWED_POLICY
      )

    assertThat(result).isEqualTo(PolicyCheckResult.Pass)
  }

  @Test
  fun checkPolicy_nonNullDataTypeName_succeeds() {
    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = EVERYTHING_PERSON_ALLOWED_POLICY,
        dataTypeDescriptor = PERSON_DESCRIPTOR
      )

    assertThat(result).isEqualTo(PolicyCheckResult.Pass)
  }

  @Test
  fun checkPolicy_ageMissingPolicy_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = AGE_MISSING_POLICY,
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
    result as PolicyCheckResult.Fail
    assertThat(result.message).contains("Person.age is not allowed for egress")
  }

  @Test
  fun checkPolicy_emptyPolicy_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = EMPTY_POLICY,
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
  }

  @Test
  fun checkPolicy_ageJoinPolicy_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = AGE_JOIN_POLICY,
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
    result as PolicyCheckResult.Fail
    assertThat(result.message).contains("Person.age is not allowed for egress")
  }

  @Test
  fun checkPolicy_incorrectPolicy_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = PET_VALID_POLICY,
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
    result as PolicyCheckResult.Fail
    assertThat(result.message).contains("Person is not found in the given policy")
  }

  @Test
  fun checkPolicy_sandboxPolicy_shouldPass() {
    val result =
      checkPolicyOnRead(
        processorNode = SandboxProcessorNode(PetProcessor()),
        connectionProviders = setOf(PetConnectionProvider()),
        policy = PET_SANDBOX_POLICY,
      )

    assertThat(result).isEqualTo(PolicyCheckResult.Pass)
  }

  @Test
  fun checkPolicy_sandboxPolicy_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = PetProcessor(),
        connectionProviders = setOf(PetConnectionProvider()),
        policy = PET_SANDBOX_POLICY,
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
  }
  @Test
  fun checkPolicy_householdAllAllowedPolicy_succeeds() {
    val result =
      checkPolicyOnRead(
        processorNode = HouseholdReaderNode(),
        connectionProviders = setOf(HouseholdConnectionProvider()),
        policy = HOUSEHOLD_ALL_ALLOWED_POLICY,
      )

    assertThat(result).isEqualTo(PolicyCheckResult.Pass)
  }

  @Test
  fun checkPolicy_householdRestrictedPolicy_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = HouseholdReaderNode(),
        connectionProviders = setOf(HouseholdConnectionProvider()),
        policy = HOUSEHOLD_RESTRICTED_POLICY,
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
    result as PolicyCheckResult.Fail
    assertThat(result.message).contains("HouseHold.address is not allowed for egress")
    assertThat(result.message).contains("HouseHold.person.age is not allowed for egress")
    assertThat(result.message).contains("HouseHold.person.pet.breed is not allowed for egress")
  }

  @Test
  fun checkPolicy_validRetention_shouldPass() {
    val result =
      checkPolicyOnRead(
        processorNode = PetProcessor(),
        connectionProviders =
          setOf(BusinessConnectionProvider(), PetConnectionProvider(), PersonConnectionProvider()),
        policy = PET_VALID_POLICY,
        dataTypeDescriptor = PET_DESCRIPTOR
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Pass::class.java)
  }

  @Test
  fun checkPolicy_invalidTTL_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = PetProcessor(),
        connectionProviders = setOf(PetConnectionProvider(), PersonConnectionProvider()),
        policy = PET_INVALID_POLICY_MAX_AGE_TOO_SHORT,
        dataTypeDescriptor = PET_DESCRIPTOR
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
  }

  @Test
  fun checkPolicy_unencryptedConnectionProvider_whenEncryptionRequired_shouldFail() {
    val result =
      checkPolicyOnRead(
        processorNode = PetProcessor(),
        connectionProviders = setOf(UnencryptedPetConnectionProvider(), PersonConnectionProvider()),
        policy = PET_POLICY_DISK_ENCRYPTION_REQUIRED,
        dataTypeDescriptor = PET_DESCRIPTOR
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
  }

  @Test
  fun checkPolicy_withInvalidContext_shouldFail() {
    val testContext = MutableTypedMap()
    testContext[NameKey] = "other test"

    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = NAME_CONTEXT_POLICY,
        connectionContext = TypedMap(testContext)
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
  }

  @Test
  fun checkPolicy_withValidContext_shouldPass() {
    val testContext = MutableTypedMap()
    testContext[NameKey] = "test"

    val result =
      checkPolicyOnRead(
        processorNode = PeopleReader(),
        connectionProviders = setOf(PersonConnectionProvider()),
        policy = NAME_CONTEXT_POLICY,
        connectionContext = TypedMap(testContext)
      )

    assertThat(result).isInstanceOf(PolicyCheckResult.Pass::class.java)
  }

  @Test
  fun checkWriteConnections_passThru_withEncryptedMemoryPolicy_passes() {
    val policy = EVERYTHING_PERSON_ALLOWED_POLICY
    val connectionProvider =
      connectionProvider(
        ManagedDataType(PERSON_DESCRIPTOR, ManagementStrategy.PassThru, PersonWriter::class),
      )
    val context =
      DefaultChronicleContext(
        setOf(connectionProvider),
        emptySet(),
        DefaultPolicySet(setOf(policy)),
        mock()
      )

    val result = engine.checkWriteConnections(context)

    assertThat(result).isEqualTo(PolicyCheckResult.Pass)
  }

  @Test
  fun checkWriteConnections_decryptedMemory_withEncryptedDiskPolicy_passes() {
    val policy = PET_VALID_POLICY
    val connectionProvider =
      connectionProvider(
        ManagedDataType(
          PET_DESCRIPTOR,
          ManagementStrategy.Stored(
            encrypted = false,
            media = StorageMedia.MEMORY,
            ttl = Duration.ofMillis(10)
          ),
          PetWriter::class
        ),
      )
    val context =
      DefaultChronicleContext(
        setOf(connectionProvider),
        emptySet(),
        DefaultPolicySet(setOf(policy)),
        mock()
      )

    val result = engine.checkWriteConnections(context)

    assertThat(result).isEqualTo(PolicyCheckResult.Pass)
  }

  @Test
  fun checkWriteConnections_noWriteConnections_effectivelyNoOp() {
    val policy = EVERYTHING_PERSON_ALLOWED_POLICY
    val personConnectionProvider =
      connectionProvider(
        ManagedDataType(PERSON_DESCRIPTOR, ManagementStrategy.PassThru, PersonReader::class)
      )
    val petConnectionProvider =
      connectionProvider(
        ManagedDataType(PET_DESCRIPTOR, ManagementStrategy.PassThru, PetReader::class)
      )
    val context =
      DefaultChronicleContext(
        setOf(personConnectionProvider, petConnectionProvider),
        emptySet(),
        DefaultPolicySet(setOf(policy)),
        mock()
      )
    val result = engine.checkWriteConnections(context)

    assertThat(result).isEqualTo(PolicyCheckResult.Pass)
  }

  @Test
  fun checkWriteConnections_noMatchingTargets_failsWithPolicyMessage() {
    val policy = EVERYTHING_PERSON_ALLOWED_POLICY
    val personConnectionProvider =
      connectionProvider(
        ManagedDataType(PERSON_DESCRIPTOR, ManagementStrategy.PassThru, PersonWriter::class)
      )
    val petConnectionProvider =
      connectionProvider(
        ManagedDataType(PET_DESCRIPTOR, ManagementStrategy.PassThru, PetWriter::class)
      )
    val context =
      DefaultChronicleContext(
        setOf(personConnectionProvider, petConnectionProvider),
        emptySet(),
        DefaultPolicySet(setOf(policy)),
        mock()
      )

    val result = engine.checkWriteConnections(context)

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
    result as PolicyCheckResult.Fail
    assertThat(result.message).contains("must have a corresponding policy")
  }

  @Test
  fun checkWriteConnections_invalidRetention_fails() {
    val policy = PET_POLICY_DISK_ENCRYPTION_REQUIRED
    val connectionProvider =
      connectionProvider(
        ManagedDataType(
          PET_DESCRIPTOR,
          ManagementStrategy.Stored(
            encrypted = false,
            media = StorageMedia.LOCAL_DISK,
            ttl = Duration.ofMillis(10)
          ),
          PetWriter::class
        ),
      )
    val context =
      DefaultChronicleContext(
        setOf(connectionProvider),
        emptySet(),
        DefaultPolicySet(setOf(policy)),
        mock()
      )

    val result = engine.checkWriteConnections(context)

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
  }

  @Test
  fun checkWriteConnections_invalidTtl_fails() {
    val policy = PET_VALID_POLICY
    val connectionProvider =
      connectionProvider(
        ManagedDataType(
          PET_DESCRIPTOR,
          ManagementStrategy.Stored(
            encrypted = true,
            media = StorageMedia.LOCAL_DISK,
            ttl = Duration.ofDays(10)
          ),
          PetWriter::class
        ),
      )
    val context =
      DefaultChronicleContext(
        setOf(connectionProvider),
        emptySet(),
        DefaultPolicySet(setOf(policy)),
        mock()
      )

    val result = engine.checkWriteConnections(context)

    assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
  }

  @Test
  fun checkWriteConnections_multipleStrategies_invalidTtl_fails() {
    val connectionProvider =
      connectionProvider(
        ManagedDataType(
          PET_DESCRIPTOR,
          ManagementStrategy.Stored(
            encrypted = true,
            media = StorageMedia.LOCAL_DISK,
            ttl = Duration.ofDays(10)
          ),
          PetWriter::class
        ),
      )

    listOf(
        setOf(PET_MULTIPLE_RETENTION_POLICY),
        setOf(PET_VALID_POLICY, PET_MEMORY_POLICY),
      )
      .forEach { policySet ->
        val context =
          DefaultChronicleContext(
            setOf(connectionProvider),
            emptySet(),
            DefaultPolicySet(policySet),
            mock()
          )

        val result = engine.checkWriteConnections(context)

        assertThat(result).isInstanceOf(PolicyCheckResult.Fail::class.java)
      }
  }

  @Test
  fun checkWriteConnections_multipleStrategies_MultipleMedium_passes() {
    val connectionProvider =
      connectionProvider(
        ManagedDataType(
          PET_DESCRIPTOR,
          ManagementStrategy.Stored(
            encrypted = true,
            media = StorageMedia.LOCAL_DISK,
            ttl = Duration.ofMinutes(10)
          ),
          PetWriter::class
        ),
      )

    listOf(
        setOf(PET_MULTIPLE_RETENTION_POLICY),
        setOf(PET_VALID_POLICY, PET_MEMORY_POLICY),
      )
      .forEach { policySet ->
        val context =
          DefaultChronicleContext(
            setOf(connectionProvider),
            emptySet(),
            DefaultPolicySet(policySet),
            mock()
          )

        val result = engine.checkWriteConnections(context)

        assertThat(result).isInstanceOf(PolicyCheckResult.Pass::class.java)
      }
  }

  private fun connectionProvider(managedDataType: ManagedDataType): ConnectionProvider {
    return object : ConnectionProvider {
      override val dataType: DataType = managedDataType

      override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
        throw NotImplementedError("Not implemented for policy checking")
      }
    }
  }
  /**
   * Get a connection with the given [processorNode], [connectionProviders], and [policy] as part of
   * the [ChronicleContext].
   */
  private fun checkPolicyOnRead(
    processorNode: ProcessorNode,
    connectionProviders: Set<ConnectionProvider>,
    policy: Policy,
    connectionContext: TypedMap = TypedMap(),
    dataTypeDescriptor: DataTypeDescriptor =
      connectionProviders.map { it.dataType.descriptor }.first()
  ): PolicyCheckResult {
    val context =
      DefaultChronicleContext(
        connectionProviders,
        setOf(processorNode),
        DefaultPolicySet(emptySet()),
        DefaultDataTypeDescriptorSet(connectionProviders.map { it.dataType.descriptor }.toSet()),
        connectionContext
      )
    return engine.checkPolicy(policy, context, dataTypeDescriptor, processorNode)
  }

  private companion object {
    val EVERYTHING_PERSON_ALLOWED_POLICY =
      policy("EverythingGoes", "TestingEgress") {
        target(PERSON_DESCRIPTOR, maxAge = Duration.ZERO) {
          retention(StorageMedium.RAM, true)

          "name" { rawUsage(UsageType.EGRESS) }
          "age" { rawUsage(UsageType.EGRESS) }
        }
      }

    val NAME_CONTEXT_POLICY =
      policy(
        "NameContextRule",
        "TestingEgress",
      ) {
        allowedContext = NameRule
        target(PERSON_DESCRIPTOR, maxAge = Duration.ZERO) {
          retention(StorageMedium.RAM, true)

          "name" { rawUsage(UsageType.EGRESS) }
          "age" { rawUsage(UsageType.EGRESS) }
        }
      }

    val EMPTY_POLICY = policy("Empty", "TestingEgress") {}

    val AGE_MISSING_POLICY =
      policy("AgeMissing", "TestingEgress") {
        target(PERSON_DESCRIPTOR, maxAge = Duration.ZERO) {
          retention(StorageMedium.RAM, false)

          "name" { rawUsage(UsageType.EGRESS) }
        }
      }

    val AGE_JOIN_POLICY =
      policy("AgeJoin", "TestingEgress") {
        target(PERSON_DESCRIPTOR, maxAge = Duration.ZERO) {
          retention(StorageMedium.RAM, false)

          "name" { rawUsage(UsageType.EGRESS) }
          "age" { rawUsage(UsageType.JOIN) }
        }
      }

    val PET_VALID_POLICY =
      policy("Pets", "Testing") {
        target(PET_DESCRIPTOR, maxAge = Duration.ofMinutes(10)) {
          retention(StorageMedium.DISK, true)

          "name" { rawUsage(UsageType.EGRESS) }
          "species" { rawUsage(UsageType.EGRESS) }
          "breed" { rawUsage(UsageType.EGRESS) }
        }
      }

    val PET_MEMORY_POLICY =
      policy("Pets", "Testing") {
        target(PET_DESCRIPTOR, maxAge = Duration.ofMinutes(10)) {
          retention(StorageMedium.RAM, true)

          "name" { rawUsage(UsageType.EGRESS) }
          "species" { rawUsage(UsageType.EGRESS) }
          "breed" { rawUsage(UsageType.EGRESS) }
        }
      }

    val PET_MULTIPLE_RETENTION_POLICY =
      policy("Pets", "Testing") {
        target(PET_DESCRIPTOR, maxAge = Duration.ofMinutes(10)) {
          retention(StorageMedium.RAM, false)
          retention(StorageMedium.DISK, true)

          "name" { rawUsage(UsageType.EGRESS) }
          "species" { rawUsage(UsageType.EGRESS) }
          "breed" { rawUsage(UsageType.EGRESS) }
        }
      }

    val PET_SANDBOX_POLICY =
      policy("Pets", "Testing") {
        target(PET_DESCRIPTOR, maxAge = Duration.ofMinutes(10)) {
          retention(StorageMedium.DISK, true)

          "name" { rawUsage(UsageType.SANDBOX) }
          "species" { rawUsage(UsageType.SANDBOX) }
          "breed" { rawUsage(UsageType.SANDBOX) }
        }
      }

    val HOUSEHOLD_ALL_ALLOWED_POLICY =
      policy("HouseholdAllowed", "Testing") {
        target(HOUSEHOLD_DESCRIPTOR, maxAge = Duration.ofMinutes(10)) {
          retention(StorageMedium.RAM, true)

          "address" { rawUsage(UsageType.EGRESS) }
          "person" {
            "name" { rawUsage(UsageType.EGRESS) }
            "age" { rawUsage(UsageType.EGRESS) }
            "pet" { "breed" { rawUsage(UsageType.EGRESS) } }
          }
        }
      }

    val HOUSEHOLD_RESTRICTED_POLICY =
      policy("HouseholdRestricted", "Testing") {
        target(HOUSEHOLD_DESCRIPTOR, maxAge = Duration.ofMinutes(10)) {
          retention(StorageMedium.RAM, true)

          "address" { rawUsage(UsageType.JOIN) }
          "person" {
            "name" { rawUsage(UsageType.EGRESS) }
            "pet" { "breed" { rawUsage(UsageType.JOIN) } }
          }
        }
      }

    val PET_INVALID_POLICY_MAX_AGE_TOO_SHORT =
      policy("Pets", "Testing") {
        target(PET_DESCRIPTOR, maxAge = Duration.ofMinutes(1)) {
          retention(StorageMedium.DISK, true)

          "name" { rawUsage(UsageType.EGRESS) }
          "species" { rawUsage(UsageType.EGRESS) }
          "breed" { rawUsage(UsageType.EGRESS) }
        }
      }

    val PET_POLICY_DISK_ENCRYPTION_REQUIRED =
      policy("Pets", "Testing") {
        target(PET_DESCRIPTOR, maxAge = Duration.ofMinutes(10)) {
          retention(StorageMedium.DISK, true)

          "name" { rawUsage(UsageType.EGRESS) }
          "species" { rawUsage(UsageType.EGRESS) }
          "breed" { rawUsage(UsageType.EGRESS) }
        }
      }
  }
}
