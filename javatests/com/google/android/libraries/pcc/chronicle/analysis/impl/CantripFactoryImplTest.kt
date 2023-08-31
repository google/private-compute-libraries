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

import com.google.android.libraries.pcc.chronicle.analysis.ChronicleContext
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.cantrip.MultiCantrip
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.operation.Operation
import com.google.android.libraries.pcc.chronicle.api.operation.OperationLibrary
import com.google.android.libraries.pcc.chronicle.api.optics.OpticalAccessPath
import com.google.android.libraries.pcc.chronicle.api.optics.OpticsManifest
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_CITY_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_LOCATION_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TEST_PET_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestCity
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestLocation
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestLocation_Latitude
import com.google.android.libraries.pcc.chronicle.api.optics.testdata.TestLocation_Longitude
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import kotlin.math.roundToInt
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class CantripFactoryImplTest {
  private val context = mock<ChronicleContext>()
  private val dtds = mock<DataTypeDescriptorSet>()
  private val optics =
    mock<OpticsManifest> {
      on {
        composeTraversal(
          accessPath = eq(OpticalAccessPath(TEST_LOCATION_GENERATED_DTD, "latitude")),
          sourceEntityType = eq(TestLocation::class.java),
          targetEntityType = eq(TestLocation::class.java),
          sourceFieldType = eq(Float::class.java),
          targetFieldType = eq(Float::class.java)
        )
      } doReturn (TestLocation_Latitude.asTraversal())
      on {
        composeTraversal(
          accessPath = eq(OpticalAccessPath(TEST_LOCATION_GENERATED_DTD, "longitude")),
          sourceEntityType = eq(TestLocation::class.java),
          targetEntityType = eq(TestLocation::class.java),
          sourceFieldType = eq(Float::class.java),
          targetFieldType = eq(Float::class.java)
        )
      } doReturn (TestLocation_Longitude.asTraversal())
    }

  private val operations =
    mock<OperationLibrary> {
      on {
        findOperation<Float, Float>(eq("round"), eq(Float::class.java), eq(Float::class.java))
      } doReturn (Operation.create("round") { Action.Update(it.roundToInt().toFloat()) })
      on {
        findOperation(eq("notGonnaFindMe"), eq(Float::class.java), eq(Float::class.java))
      } doReturn (null)
    }

  private val factory = CantripFactoryImpl(optics, operations, dtds)

  @Test
  fun buildCantrip_noPolicyInConnectionRequest_returnsNoOp() {
    val result =
      factory.buildCantrip<TestCity>(
        TEST_CITY_GENERATED_DTD,
        MyProcessorNode,
        null,
        UsageType.EGRESS
      )

    result as MultiCantrip
    assertThat(result.isNoOp()).isTrue()
  }

  @Test
  fun buildCantrip_noDtdAssociatedWithConnectionRequest_returnsNoOp() {
    whenever(context.findDataType(eq(MyReadConnection::class.java))).thenReturn(null)

    val result =
      factory.buildCantrip<TestCity>(
        TEST_CITY_GENERATED_DTD,
        MyProcessorNode,
        POLICY,
        UsageType.EGRESS
      )

    result as MultiCantrip
    assertThat(result.isNoOp()).isTrue()
  }

  @Test
  fun buildCantrip_policyDoesNotContainTarget_returnsNoOp() {
    val nonExistentDtd = dataTypeDescriptor("IDontLiveInAnyPolicy", this::class) {}
    whenever(context.findDataType(eq(MyReadConnection::class.java))).thenReturn(nonExistentDtd)

    val result =
      factory.buildCantrip<TestCity>(
        TEST_CITY_GENERATED_DTD,
        MyProcessorNode,
        POLICY,
        UsageType.EGRESS
      )

    result as MultiCantrip
    assertThat(result.isNoOp()).isTrue()
  }

  @Test
  fun buildCantrip_noConditionalUsages_returnsNoOp() {
    whenever(context.findDataType(eq(MyReadConnection::class.java)))
      .thenReturn(TEST_PET_GENERATED_DTD)

    val result =
      factory.buildCantrip<TestCity>(
        TEST_CITY_GENERATED_DTD,
        MyProcessorNode,
        POLICY,
        UsageType.SANDBOX
      )

    result as MultiCantrip
    assertThat(result.isNoOp()).isTrue()
  }

  @Test
  fun buildCantrip_noOperationFound_throws() {
    whenever(context.findDataType(eq(MyReadConnection::class.java)))
      .thenReturn(TEST_LOCATION_GENERATED_DTD)
    whenever(dtds.findFieldTypeAsClass(eq(TEST_LOCATION_GENERATED_DTD), any()))
      .thenReturn(Float::class.java)

    val e =
      assertFailsWith<IllegalArgumentException> {
        factory.buildCantrip<TestLocation>(
          TEST_LOCATION_GENERATED_DTD,
          MyProcessorNode,
          POLICY,
          UsageType.EGRESS
        )
      }

    assertThat(e)
      .hasMessageThat()
      .contains("No Operation found with name: notGonnaFindMe for type: ${Float::class.java}")
  }

  @Test
  fun buildCantrip_happyPath() {
    whenever(context.findDataType(eq(MyReadConnection::class.java)))
      .thenReturn(TEST_LOCATION_GENERATED_DTD)
    whenever(dtds.findFieldTypeAsClass(eq(TEST_LOCATION_GENERATED_DTD), any()))
      .thenReturn(Float::class.java)

    val result =
      factory.buildCantrip<TestLocation>(
        TEST_LOCATION_GENERATED_DTD,
        MyProcessorNode,
        POLICY,
        UsageType.SANDBOX
      )

    val inputLocation = TestLocation(42.1f, 133.7f)
    assertThat(result(inputLocation)).isEqualTo(TestLocation(42.0f, 134.0f))
  }

  interface MyReadConnection : ReadConnection

  object MyProcessorNode : ProcessorNode {
    override val requiredConnectionTypes: Set<Class<out Connection>> = emptySet()
  }

  companion object {
    private val POLICY =
      policy("TestPolicy", "TestEgress") {
        target(TEST_PET_GENERATED_DTD, Duration.ofDays(24)) {
          retention(StorageMedium.RAM)

          "name" { rawUsage(UsageType.SANDBOX) }
          "age" { rawUsage(UsageType.SANDBOX) }
          "favoriteToy" { rawUsage(UsageType.SANDBOX) }
          "likesMilk" { rawUsage(UsageType.SANDBOX) }
        }

        // Used for the no-operation-found test
        target(TEST_LOCATION_GENERATED_DTD, Duration.ofDays(24)) {
          retention(StorageMedium.RAM)

          "latitude" {
            conditionalUsage("round", UsageType.EGRESS)
            conditionalUsage("round", UsageType.SANDBOX)
          }
          "longitude" {
            conditionalUsage("notGonnaFindMe", UsageType.EGRESS)
            conditionalUsage("round", UsageType.SANDBOX)
          }
        }
      }
  }
}
