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
import com.google.android.libraries.pcc.chronicle.api.ConnectionNameForRemoteConnections
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.Name
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.SandboxProcessorNode
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.error.PolicyViolation
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.StreamRequest
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemotePolicyCheckerImplTest {
  private val chronicle = mock<Chronicle>()
  private val policySet =
    mock<PolicySet> {
      on { findByName(eq("WillNotBeFound")) } doReturn null
      on { findByName(eq("WillBeFound")) } doReturn policy("WillBeFound", "Testing")
    }
  private val server =
    mock<RemoteServer<Foo>> {
      on { readConnection } doReturn FooReader
      on { writeConnection } doReturn FooWriter
    }
  private val checker = RemotePolicyCheckerImpl(chronicle, policySet)

  @Test
  fun checkAndGetPolicyOrThrow_defaultClientDetails_doesNotUseSandboxProcessorNode() {
    val requestCaptor = argumentCaptor<ConnectionRequest<FooReader>>()
    whenever(chronicle.getConnectionOrThrow(requestCaptor.capture())) doReturn FooReader

    checker.checkAndGetPolicyOrThrow(FOUND_POLICY_READ_REQUEST_METADATA, server, DEFAULT_DETAILS)

    val connectionRequest = requestCaptor.firstValue
    assertThat(connectionRequest.connectionName)
      .isEqualTo(ConnectionNameForRemoteConnections.Reader<Connection>(Name(DTD_TYPE_NAME)))
    assertThat(connectionRequest.requester).isNotInstanceOf(SandboxProcessorNode::class.java)
    assertThat(connectionRequest.policy).isNotNull()
  }

  @Test
  fun checkAndGetPolicyOrThrow_isolatedClientDetails_usesSandboxProcessorNode() {
    val requestCaptor = argumentCaptor<ConnectionRequest<FooReader>>()
    whenever(chronicle.getConnectionOrThrow(requestCaptor.capture())) doReturn FooReader

    checker.checkAndGetPolicyOrThrow(
      FOUND_POLICY_READ_REQUEST_METADATA,
      server,
      ClientDetails(
        userId = 42,
        isolationType = ClientDetails.IsolationType.ISOLATED_PROCESS,
        associatedPackages = listOf("com.google.android.as")
      )
    )

    val connectionRequest = requestCaptor.firstValue
    assertThat(connectionRequest.connectionName)
      .isEqualTo(ConnectionNameForRemoteConnections.Reader<Connection>(Name(DTD_TYPE_NAME)))
    assertThat(connectionRequest.requester).isInstanceOf(SandboxProcessorNode::class.java)
    assertThat(connectionRequest.policy).isNotNull()
  }

  @Test
  fun checkAndGetPolicyOrThrow_differentClientDetails_usesDifferentProcessorNodes() {
    val requestCaptor = argumentCaptor<ConnectionRequest<FooReader>>()
    whenever(chronicle.getConnectionOrThrow(requestCaptor.capture())) doReturn FooReader

    checker.checkAndGetPolicyOrThrow(
      FOUND_POLICY_READ_REQUEST_METADATA,
      server,
      ClientDetails(
        userId = 42,
        isolationType = ClientDetails.IsolationType.ISOLATED_PROCESS,
        associatedPackages = listOf("com.google.android.as")
      )
    )

    checker.checkAndGetPolicyOrThrow(
      FOUND_POLICY_READ_REQUEST_METADATA,
      server,
      ClientDetails(
        userId = 43,
        isolationType = ClientDetails.IsolationType.ISOLATED_PROCESS,
        associatedPackages = listOf("com.google.android.as")
      )
    )

    val firstRequest = requestCaptor.firstValue
    val secondRequest = requestCaptor.secondValue

    assertThat(firstRequest.requester).isNotSameInstanceAs(secondRequest.requester)
  }

  @Test
  fun checkAndGetPolicyOrThrow_sameClientDetails_reusesSameProcessorNode() {
    val requestCaptor = argumentCaptor<ConnectionRequest<*>>()
    whenever(chronicle.getConnectionOrThrow(requestCaptor.capture())) doReturn FooReader

    checker.checkAndGetPolicyOrThrow(FOUND_POLICY_READ_REQUEST_METADATA, server, DEFAULT_DETAILS)
    checker.checkAndGetPolicyOrThrow(FOUND_POLICY_WRITE_REQUEST_METADATA, server, DEFAULT_DETAILS)

    val firstRequest = requestCaptor.firstValue
    val secondRequest = requestCaptor.secondValue

    assertThat(firstRequest.requester).isSameInstanceAs(secondRequest.requester)
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
    whenever(chronicle.getConnectionOrThrow<FooReader>(any())).then { throw exception }

    val e =
      assertFailsWith<PolicyViolation> {
        checker.checkAndGetPolicyOrThrow(
          FOUND_POLICY_READ_REQUEST_METADATA,
          server,
          DEFAULT_DETAILS
        )
      }

    assertThat(e).isSameInstanceAs(exception)
  }

  data class Foo(val name: String)
  object FooReader : ReadConnection
  object FooWriter : WriteConnection

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
        associatedPackages = listOf("com.google.android.as")
      )
  }
}
