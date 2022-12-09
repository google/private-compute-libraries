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
import com.google.android.libraries.pcc.chronicle.analysis.CantripFactory
import com.google.android.libraries.pcc.chronicle.analysis.impl.CantripFactoryImplIntegrationTest.Dependencies.POLICY_ONE
import com.google.android.libraries.pcc.chronicle.analysis.impl.CantripFactoryImplIntegrationTest.Dependencies.PROCESSOR_NODE
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.cantrip.invoke
import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.operation.DefaultOperationLibrary
import com.google.android.libraries.pcc.chronicle.api.operation.Operation
import com.google.android.libraries.pcc.chronicle.api.operation.OperationLibrary
import com.google.android.libraries.pcc.chronicle.api.optics.DefaultOpticsManifest
import com.google.android.libraries.pcc.chronicle.api.optics.Lens
import com.google.android.libraries.pcc.chronicle.api.optics.OpticsManifest
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_CITY_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity_Lenses
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestLocation
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestLocation_Lenses
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPerson_Lenses
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestPet_Lenses
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.operation.Empties
import com.google.android.libraries.pcc.chronicle.operation.TestOps
import com.google.common.truth.Truth.assertThat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The purpose of this test class is to exercise the full cantrip stack as provided by dagger,
 * including:
 *
 * * OpticsManifest
 * * OperationLibrary
 * * DataTypeDescriptorSet
 * * CantripFactoryImpl
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = CantripFactoryImplIntegrationTest_Application::class)
class CantripFactoryImplIntegrationTest {
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Inject lateinit var cantripFactory: CantripFactory

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Test
  fun buildCantrip_noFilters() {
    val cantrip =
      cantripFactory.buildCantrip<TestCity>(
        TEST_CITY_GENERATED_DTD,
        PROCESSOR_NODE,
        POLICY_ONE,
        UsageType.SANDBOX
      )

    val output = cantrip(CITIES)

    assertThat(output).containsExactlyElementsIn(CITIES)
  }

  @Test
  fun buildCantrip_withFilters() {
    val cantrip =
      cantripFactory.buildCantrip<TestCity>(
        TEST_CITY_GENERATED_DTD,
        PROCESSOR_NODE,
        POLICY_ONE,
        UsageType.EGRESS
      )

    val output = cantrip(CITIES)

    assertThat(output)
      .containsExactly(
        TAR_VALON.copy(
          mayor = TAR_VALON.mayor.copy(pet = null),
          location =
            TAR_VALON.location.copy(
              latitude = TAR_VALON.location.latitude.roundToInt().toFloat(),
              longitude = TAR_VALON.location.longitude.roundToInt().toFloat()
            ),
          populace =
            listOf(
              TAR_VALON.populace[0].copy(name = "**Redacted**", age = 40, pet = null),
              TAR_VALON.populace[1].copy(name = "**Redacted**", age = 30, pet = null)
            )
        )
      )
  }

  interface CityReader : ReadConnection

  companion object {
    private val AMOS = TestPerson(name = "Amos Burton", age = 31, pet = null)
    private val LYDIA = TestPerson(name = "Lydia", age = 75, pet = null)
    private val NED = TestPerson(name = "Ned Stark", age = 55, pet = null)
    private val SANSA =
      TestPerson(name = "Sansa Stark", age = 18, pet = TestPet.Dog(name = "Lady", age = 3))
    private val RICKON =
      TestPerson(name = "Rickon Stark", age = 10, pet = TestPet.Dog(name = "Shaggy Dog", age = 3))
    private val CADSUANE = TestPerson(name = "Cadsuane Melaidhrin", age = 41, pet = null)
    private val MOIRAINE =
      TestPerson(name = "Moiraine Damodred", age = 33, pet = TestPet.Dog(name = "Lan", age = 12))
    private val RAND = TestPerson(name = "Rand al'Thor", age = 16, pet = null)
    private val EGWENE =
      TestPerson(name = "Egwene al'Vere", age = 15, pet = TestPet.Cat(name = "Bela", age = 5))

    private val BALTIMORE =
      TestCity(
        name = "Baltimore",
        mayor = AMOS,
        location = TestLocation(latitude = 39.2904f, longitude = -76.6122f),
        populace = listOf(AMOS, LYDIA)
      )

    private val WINTERFELL =
      TestCity(
        name = "Winterfell",
        mayor = SANSA,
        location = TestLocation(latitude = 53.96f, longitude = -1.0873f),
        populace = listOf(NED, SANSA, RICKON)
      )

    private val TAR_VALON =
      TestCity(
        name = "Tar Valon",
        mayor = CADSUANE,
        location = TestLocation(latitude = 48.8566f, longitude = 2.3522f),
        populace = listOf(CADSUANE, MOIRAINE, RAND, EGWENE)
      )

    private val CITIES = listOf(BALTIMORE, WINTERFELL, TAR_VALON)
  }

  @Module
  @InstallIn(SingletonComponent::class)
  internal object Dependencies {
    val POLICY_ONE =
      policy("PolicyOne", "Testing") {
        target(TEST_CITY_GENERATED_DTD, Duration.ofDays(365)) {
          retention(StorageMedium.RAM)

          "name" { rawUsage(UsageType.ANY) }
          "mayor" {
            "name" { rawUsage(UsageType.ANY) }
            "age" { rawUsage(UsageType.ANY) }
            "pet" {
              // TODO(b/206867552): Determine how to handle conditional usage at varying depth in
              //  structures.
              conditionalUsage("EMPTY", UsageType.EGRESS)

              "name" { rawUsage(UsageType.ANY) }
              "age" { rawUsage(UsageType.ANY) }
              "favoriteToy" { rawUsage(UsageType.ANY) }
              "likesMilk" { rawUsage(UsageType.ANY) }
            }
          }
          "location" {
            conditionalUsage("EasternHemisphere", UsageType.EGRESS)

            "latitude" {
              conditionalUsage("Rounded", UsageType.EGRESS)
              rawUsage(UsageType.SANDBOX)
            }
            "longitude" {
              conditionalUsage("Rounded", UsageType.EGRESS)
              rawUsage(UsageType.SANDBOX)
            }
          }
          "populace" {
            "name" {
              conditionalUsage("Redact", UsageType.EGRESS)
              rawUsage(UsageType.SANDBOX)
            }
            "age" {
              conditionalUsage("OnlyIfOlderThan18", UsageType.EGRESS)
              conditionalUsage("TruncateToTens", UsageType.EGRESS)
              rawUsage(UsageType.SANDBOX)
            }
            "pet" {
              // TODO(b/206867552): Determine how to handle conditional usage at varying depth in
              //  structures.
              conditionalUsage("EMPTY", UsageType.EGRESS)
              "name" { rawUsage(UsageType.ANY) }
              "age" to { rawUsage(UsageType.ANY) }
              "favoriteToy" to { rawUsage(UsageType.ANY) }
              "likesMilk" to { rawUsage(UsageType.ANY) }
            }
          }
        }
      }

    val PROCESSOR_NODE =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> = emptySet()
      }

    private val CITY_CONNECTION_PROVIDER =
      object : ConnectionProvider {
        override val dataType: DataType =
          ManagedDataType(TEST_CITY_GENERATED_DTD, ManagementStrategy.PassThru, CityReader::class)

        override fun getConnection(
          connectionRequest: ConnectionRequest<out Connection>,
        ): Connection {
          return object : CityReader {}
        }
      }

    @Provides
    @Singleton
    fun provideCantripFactory(
      optics: OpticsManifest,
      operations: OperationLibrary,
      dtds: DataTypeDescriptorSet,
    ): CantripFactory = CantripFactoryImpl(optics, operations, dtds)

    @Provides
    @Singleton
    fun provideOpticsManifest(
      lenses: Set<@JvmSuppressWildcards Lens<*, *, *, *>>,
      dtds: DataTypeDescriptorSet,
    ): OpticsManifest = DefaultOpticsManifest(lenses, dtds)

    @Provides
    @Singleton
    @ElementsIntoSet
    fun provideLenses(): Set<@JvmSuppressWildcards Lens<*, *, *, *>> =
      TestCity_Lenses + TestPerson_Lenses + TestLocation_Lenses + TestPet_Lenses

    @Provides @Singleton @IntoSet fun provideDataTypeDescriptor() = TEST_CITY_GENERATED_DTD

    @Provides
    @Singleton
    fun provideOperationLibrary(ops: Set<@JvmSuppressWildcards Operation<*, *>>): OperationLibrary =
      DefaultOperationLibrary(ops + TestOps.provideOperations() + Empties.provideOperations())

    @Provides
    @ElementsIntoSet
    fun provideOtherOperations(): Set<@JvmSuppressWildcards Operation<*, *>> {
      return setOf(
        Operation.create<Int>(name = "OnlyIfOlderThan18") {
          if (it >= 18) Action.Update(it) else Action.OmitFromParent
        },
        Operation.create<Int>(name = "TruncateToTens") { Action.Update(it / 10 * 10) },
        Operation.create<TestLocation>(name = "WesternHemisphere") {
          if (it.longitude <= 0f) Action.Update(it) else Action.OmitFromRoot
        },
        Operation.create<TestLocation>(name = "EasternHemisphere") {
          if (it.longitude > 0f) Action.Update(it) else Action.OmitFromRoot
        },
        Operation.create<String>(name = "Redact") { Action.Update("**Redacted**") },
        Operation.create<Float>(name = "Rounded") { Action.Update(it.roundToInt().toFloat()) }
      )
    }
  }
}
