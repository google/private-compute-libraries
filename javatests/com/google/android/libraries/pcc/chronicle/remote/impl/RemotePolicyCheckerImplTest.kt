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

package com.google.android.libraries.pcc.chronicle.remote.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.analysis.PolicySet
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ConnectionResult
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.SandboxProcessorNode
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.error.PolicyViolation
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails
import com.google.common.truth.Truth.assertThat
import kotlin.reflect.KClass
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.notNull
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RemotePolicyCheckerImplTest {
  private val chronicle = spy(FakeChronicle { Result.success(Unit) })
  private val policySet =
    mock<PolicySet> {
      on { findByName(eq("WillNotBeFound")) } doReturn null
      on { findByName(eq("WillBeFound")) } doReturn policy("WillBeFound", "Testing")
    }
  private val server = mock<RemoteServer<Foo>>()
  private val checker = RemotePolicyCheckerImpl(chronicle, policySet)

  @Test
  fun checkAndGetPolicyOrThrow_defaultClientDetails_doesNotUseSandboxProcessorNode() {
    val dataTypeNameCaptor = argumentCaptor<String>()
    val processorNodeCaptor = argumentCaptor<ProcessorNode>()
    doCallRealMethod()
      .whenever(chronicle)
      .checkPolicy(
        dataTypeName = dataTypeNameCaptor.capture(),
        policy = notNull(),
        isForReading = eq(true),
        requester = processorNodeCaptor.capture(),
      )

    checker.checkAndGetPolicyOrThrow(FOUND_POLICY_READ_REQUEST_METADATA, server, DEFAULT_DETAILS)

    assertThat(processorNodeCaptor.firstValue).isNotInstanceOf(SandboxProcessorNode::class.java)
    assertThat(dataTypeNameCaptor.firstValue).isEqualTo(DTD_TYPE_NAME)
  }

  @Test
  fun checkAndGetPolicyOrThrow_isolatedClientDetails_usesSandboxProcessorNode() {
    val dataTypeNameCaptor = argumentCaptor<String>()
    val processorNodeCaptor = argumentCaptor<ProcessorNode>()
    doCallRealMethod()
      .whenever(chronicle)
      .checkPolicy(
        dataTypeName = dataTypeNameCaptor.capture(),
        policy = notNull(),
        isForReading = eq(true),
        requester = processorNodeCaptor.capture(),
      )

    checker.checkAndGetPolicyOrThrow(
      FOUND_POLICY_READ_REQUEST_METADATA,
      server,
      ClientDetails(
        userId = 42,
        isolationType = ClientDetails.IsolationType.ISOLATED_PROCESS,
        associatedPackages = listOf("com.google.android.as"),
      ),
    )

    assertThat(processorNodeCaptor.firstValue).isInstanceOf(SandboxProcessorNode::class.java)
    assertThat(dataTypeNameCaptor.firstValue).isEqualTo(DTD_TYPE_NAME)
  }

  @Test
  fun checkAndGetPolicyOrThrow_differentClientDetails_usesDifferentProcessorNodes() {
    val processorNodeCaptor = argumentCaptor<ProcessorNode>()
    doCallRealMethod()
      .whenever(chronicle)
      .checkPolicy(any(), anyOrNull(), any(), processorNodeCaptor.capture())

    checker.checkAndGetPolicyOrThrow(
      FOUND_POLICY_READ_REQUEST_METADATA,
      server,
      ClientDetails(
        userId = 42,
        isolationType = ClientDetails.IsolationType.ISOLATED_PROCESS,
        associatedPackages = listOf("com.google.android.as"),
      ),
    )

    checker.checkAndGetPolicyOrThrow(
      FOUND_POLICY_READ_REQUEST_METADATA,
      server,
      ClientDetails(
        userId = 43,
        isolationType = ClientDetails.IsolationType.ISOLATED_PROCESS,
        associatedPackages = listOf("com.google.android.as"),
      ),
    )

    with(processorNodeCaptor) { assertThat(this.firstValue).isNotSameInstanceAs(this.secondValue) }
  }

  @Test
  fun checkAndGetPolicyOrThrow_sameClientDetails_reusesSameProcessorNode() {
    val processorNodeCaptor = argumentCaptor<ProcessorNode>()
    doCallRealMethod()
      .whenever(chronicle)
      .checkPolicy(any(), anyOrNull(), any(), processorNodeCaptor.capture())

    checker.checkAndGetPolicyOrThrow(FOUND_POLICY_READ_REQUEST_METADATA, server, DEFAULT_DETAILS)
    checker.checkAndGetPolicyOrThrow(FOUND_POLICY_WRITE_REQUEST_METADATA, server, DEFAULT_DETAILS)

    with(processorNodeCaptor) { assertThat(this.firstValue).isSameInstanceAs(this.secondValue) }
  }

  @Test
  fun checkAndGetPolicyOrThrow_policyNotFound_throwsRemoteError() {
    val e =
      assertFailsWith<RemoteError> {
        checker.checkAndGetPolicyOrThrow(NOT_FOUND_POLICY_REQUEST_METADATA, server, DEFAULT_DETAILS)
      }

    assertThat(e.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.POLICY_NOT_FOUND)
    assertThat(e).hasMessageThat().contains("No policy found with id/usageType [WillNotBeFound]")
  }

  @Test
  fun checkAndGetPolicyOrThrow_chronicleThrows_throws() {
    val exception = PolicyViolation("Boo")
    chronicle.onCheckPolicy = { Result.failure(exception) }

    val e =
      assertFailsWith<PolicyViolation> {
        checker.checkAndGetPolicyOrThrow(
          FOUND_POLICY_READ_REQUEST_METADATA,
          server,
          DEFAULT_DETAILS,
        )
      }

    assertThat(e).isSameInstanceAs(exception)
  }

  data class Foo(val name: String)

  object FooReader : ReadConnection

  object FooWriter : WriteConnection

  open class FakeChronicle(var onCheckPolicy: () -> Result<Unit>) : Chronicle {
    override fun checkPolicy(
      dataTypeName: String,
      policy: Policy?,
      isForReading: Boolean,
      requester: ProcessorNode,
    ): Result<Unit> = onCheckPolicy()

    override fun getAvailableConnectionTypes(dataTypeClass: KClass<*>): Chronicle.ConnectionTypes =
      throw NotImplementedError("Not implemented for RemotePolicyCheckerImplTest")

    override fun <T : Connection> getConnection(
      request: ConnectionRequest<T>
    ): ConnectionResult<T> =
      throw NotImplementedError("Not implemented for RemotePolicyCheckerImplTest")
  }

  companion object {
    private const val DTD_TYPE_NAME = "Foo"

    private val FOUND_POLICY_READ_REQUEST_METADATA =
      RemoteRequestMetadata.newBuilder()
        .apply {
          usageType = "WillBeFound"
          stream =
            StreamRequest.newBuilder()
              .apply {
                dataTypeName = DTD_TYPE_NAME
                operation = StreamRequest.Operation.SUBSCRIBE
              }
              .build()
        }
        .build()
    private val FOUND_POLICY_WRITE_REQUEST_METADATA =
      RemoteRequestMetadata.newBuilder()
        .apply {
          stream =
            StreamRequest.newBuilder()
              .apply {
                dataTypeName = DTD_TYPE_NAME
                operation = StreamRequest.Operation.PUBLISH
              }
              .build()
        }
        .build()

    private val NOT_FOUND_POLICY_REQUEST_METADATA =
      FOUND_POLICY_READ_REQUEST_METADATA.toBuilder().setUsageType("WillNotBeFound").build()

    private val DEFAULT_DETAILS =
      ClientDetails(
        userId = 42,
        isolationType = ClientDetails.IsolationType.DEFAULT_PROCESS,
        associatedPackages = listOf("com.google.android.as"),
      )
  }
}
