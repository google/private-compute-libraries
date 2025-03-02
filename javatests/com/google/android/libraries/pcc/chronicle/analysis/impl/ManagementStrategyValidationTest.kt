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

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheck
import com.google.android.libraries.pcc.chronicle.api.policy.builder.deletionTriggers
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.policy.builder.target
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ManagementStrategyValidationTest {
  @Test
  fun policy_verifyManagementStrategies() {
    val policy =
      policy("MyPolicy", "SomeKindaEgress") {
        target(FOO_DTD, maxAge = Duration.ofMillis(Long.MAX_VALUE)) {
          retention(StorageMedium.DISK)
          deletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg")
        }
        target(BAR_DTD, maxAge = Duration.ofMinutes(5)) { retention(StorageMedium.RAM) }
        // Baz won't be used.
        target(BAZ_DTD, maxAge = Duration.ofMillis(1)) {
          retention(StorageMedium.RAM, encryptionRequired = true)
        }
      }

    val connectionProviders =
      listOf(
        makeConnectionProvider(
          ManagedDataType(
            descriptor = dataTypeDescriptor(name = "Foo", Unit::class),
            managementStrategy =
              ManagementStrategy.Stored(
                encrypted = false,
                media = StorageMedia.LOCAL_DISK,
                ttl = null,
                deletionTriggers =
                  setOf(
                    DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg"),
                    DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "optional_trigger"),
                  ),
              ),
            connectionTypes = emptySet(),
          )
        ),
        makeConnectionProvider(
          ManagedDataType(
            descriptor = dataTypeDescriptor(name = "Bar", Unit::class),
            managementStrategy =
              ManagementStrategy.Stored(
                encrypted = false,
                media = StorageMedia.MEMORY,
                ttl = Duration.ofMinutes(5),
              ),
            connectionTypes = emptySet(),
          )
        ),
      )

    assertThat(policy.verifyManagementStrategies(connectionProviders)).isEmpty()
  }

  @Test
  fun policyTarget_verifyRetentionSatisfiedBy_valid() {
    val dataType =
      ManagedDataType(
        descriptor = FOO_DTD,
        managementStrategy =
          ManagementStrategy.Stored(
            encrypted = true,
            media = StorageMedia.MEMORY,
            ttl = Duration.ofMinutes(5),
          ),
        connectionTypes = emptySet(),
      )

    assertThat(FOO_TARGET.verifyRetentionSatisfiedBy(dataType)).isEmpty()
  }

  @Test
  fun policyTarget_verifyRetentionSatisfiedBy_invalid() {
    val dataType =
      ManagedDataType(
        descriptor = FOO_DTD,
        managementStrategy =
          ManagementStrategy.Stored(
            encrypted = true,
            media = StorageMedia.LOCAL_DISK,
            ttl = Duration.ofMinutes(5),
          ),
        connectionTypes = emptySet(),
      )

    assertThat(FOO_TARGET.verifyRetentionSatisfiedBy(dataType)).hasSize(1)
  }

  @Test
  fun satifiesDeletion_whenPassthru_alwaysTrue() {
    val strategy = ManagementStrategy.PassThru

    FOO_TARGET.deletionTriggers().forEach { assertThat(strategy.satisfies(it)).isTrue() }
  }

  @Test
  fun satifiesDeletion_whenStored_passesWithCorrectTriggers() {
    val strategy =
      ManagementStrategy.Stored(
        encrypted = false,
        media = StorageMedia.MEMORY,
        ttl = Duration.ofMinutes(5),
        deletionTriggers =
          setOf(
            DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg1"),
            DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg2"),
            DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "optional_trigger"),
          ),
      )

    FOO_TARGET.deletionTriggers().forEach { assertThat(strategy.satisfies(it)).isTrue() }
  }

  @Test
  fun satifiesDeletion_whenStored_failsWithoutTrigger() {
    val dataType =
      ManagedDataType(
        descriptor = FOO_DTD,
        managementStrategy =
          ManagementStrategy.Stored(
            encrypted = false,
            media = StorageMedia.MEMORY,
            ttl = Duration.ofMinutes(5),
            deletionTriggers =
              setOf(DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "optional_trigger")),
          ),
        connectionTypes = emptySet(),
      )

    FOO_TARGET.deletionTriggers().forEach {
      assertThat(dataType.managementStrategy.satisfies(it)).isFalse()
    }
    assertThat(FOO_TARGET.verifyDeletionTriggersSatisfiedBy(dataType))
      .containsExactly(
        PolicyCheck(
          "h:${FOO_DTD.name} is DeletionTrigger(trigger=PACKAGE_UNINSTALLED, targetField=pkg1)"
        ),
        PolicyCheck(
          "h:${FOO_DTD.name} is DeletionTrigger(trigger=PACKAGE_UNINSTALLED, targetField=pkg2)"
        ),
      )
  }

  @Test
  fun deletionTriggersParsed_Normally() {
    val triggers = FOO_TARGET.deletionTriggers()
    val expectedTriggers =
      setOf(
        DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg1"),
        DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg2"),
      )
    assertThat(triggers).isEqualTo(expectedTriggers)
  }

  private fun makeConnectionProvider(managedDataType: ManagedDataType): ConnectionProvider {
    return object : ConnectionProvider {
      override val dataType: DataType = managedDataType

      override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
        throw UnsupportedOperationException("Not used here")
    }
  }

  companion object {
    val FOO_DTD = dataTypeDescriptor("Foo", Unit::class)
    val BAR_DTD = dataTypeDescriptor("Bar", Unit::class)
    val BAZ_DTD = dataTypeDescriptor("Baz", Unit::class)

    val FOO_TARGET =
      target(FOO_DTD, maxAge = Duration.ofMinutes(5)) {
        retention(StorageMedium.RAM, false)
        deletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg1")
        deletionTrigger(Trigger.PACKAGE_UNINSTALLED, "pkg2")
      }
  }
}
