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

package com.google.android.libraries.pcc.chronicle.api.integration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.analysis.ChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.DefaultChronicleContext
import com.google.android.libraries.pcc.chronicle.analysis.PolicyEngine
import com.google.android.libraries.pcc.chronicle.analysis.PolicySet
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ConnectionResult
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.error.ConnectionNotDeclared
import com.google.android.libraries.pcc.chronicle.api.error.ConnectionProviderNotFound
import com.google.android.libraries.pcc.chronicle.api.error.DataTypeDescriptorNotFound
import com.google.android.libraries.pcc.chronicle.api.error.Disabled
import com.google.android.libraries.pcc.chronicle.api.error.PolicyNotFound
import com.google.android.libraries.pcc.chronicle.api.error.PolicyViolation
import com.google.android.libraries.pcc.chronicle.api.flags.FakeFlagsReader
import com.google.android.libraries.pcc.chronicle.api.flags.Flags
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyConformanceCheck
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheck
import com.google.android.libraries.pcc.chronicle.api.policy.builder.PolicyCheckResult
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.update
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
class DefaultChronicleTest {
  private val conformanceCheck = mock<PolicyConformanceCheck>()
  private val config =
    DefaultChronicle.Config(DefaultChronicle.Config.PolicyMode.STRICT, conformanceCheck)
  private val flags = FakeFlagsReader(Flags())
  private val policySet =
    mock<PolicySet> {
      on { toSet() } doReturn setOf(POLICY)
      on { contains(eq(POLICY)) } doReturn true
    }
  private val context: ChronicleContext =
    DefaultChronicleContext(
      connectionProviders =
        setOf(FooConnectionProvider, FooStreamConnectionProvider, BarConnectionProvider),
      processorNodes = emptySet(),
      policySet = policySet,
      dataTypeDescriptorSet = DefaultDataTypeDescriptorSet(setOf(FOO_DTD, BAR_DTD)),
    )
  private val policyEngine =
    mock<PolicyEngine> {
      on { checkWriteConnections(eq(context)) } doReturn PolicyCheckResult.Pass
      on { checkPolicy(any(), any(), any(), any()) } doReturn PolicyCheckResult.Pass
    }

  @Test
  fun init_checksPoliciesConform() {
    DefaultChronicle(context, policyEngine, config, flags)

    verify(config.policyConformanceCheck).checkPoliciesConform(eq(setOf(POLICY)))
  }

  @Test
  fun init_checksWriteConnections() {
    DefaultChronicle(context, policyEngine, config, flags)

    verify(policyEngine).checkWriteConnections(eq(context))
  }

  @Test
  fun init_checksWriteConnections_throwsOnFailure() {
    whenever(policyEngine.checkWriteConnections(any()))
      .thenReturn(PolicyCheckResult.Fail(listOf(PolicyCheck("Oops"))))

    val e =
      assertFailsWith<PolicyViolation> { DefaultChronicle(context, policyEngine, config, flags) }

    assertThat(e).hasMessageThat().contains("Oops")
  }

  @Test
  fun checkPolicy_happyPath_returnsNoErrors() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    assertThat(
        chronicle.checkPolicy(
          FOO_DTD.name,
          request.policy,
          request.isReadConnection(),
          request.requester,
        )
      )
      .isEqualTo(Result.success(Unit))
  }

  @Test
  fun checkPolicy_noDataTypeDescriptorInContext_returnsError() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    assertThat(
        chronicle
          .checkPolicy(
            dataTypeName = "Baz",
            request.policy,
            request.isReadConnection(),
            request.requester,
          )
          .exceptionOrNull()
      )
      .isInstanceOf(DataTypeDescriptorNotFound::class.java)
  }

  @Test
  fun getConnection_happyPath_returnsConnectionSuccess() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    assertThat(chronicle.getConnection(request)).isInstanceOf(ConnectionResult.Success::class.java)
  }

  @Test
  fun getConnection_logsDuration() {
    ShadowLog.setLoggable("Chronicle", Log.VERBOSE)

    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    assertThat(chronicle.getConnection(request)).isInstanceOf(ConnectionResult.Success::class.java)

    val lastLogMessage = ShadowLog.getLogs().last().msg
    assertThat(lastLogMessage).contains("ChronicleImpl.getConnection")
    assertThat(lastLogMessage).contains("ms")
  }

  @Test
  fun getConnection_requestReadConnectionWithNonNullPolicy_checksPolicy() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    chronicle.getConnection(request)

    verify(policyEngine).checkPolicy(eq(POLICY), any(), any(), eq(request.requester))
  }

  @Test
  fun getConnection_configFailNewConnections_returnsFailure() {
    flags.config.update { it.copy(failNewConnections = true) }
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    val result = chronicle.getConnection(request) as ConnectionResult.Failure

    assertThat(result.error).isInstanceOf(Disabled::class.java)
  }

  @Test
  fun getConnection_requestConnectionTypeNotMemberOfProcessorNodeRequiredTypes_returnsFailure() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request =
      createConnectionRequest<BogusReadConnection>(
        requester =
          object : ProcessorNode {
            override val requiredConnectionTypes: Set<Class<out Connection>> =
              setOf(FooReader::class.java)
          }
      )

    val result = chronicle.getConnection(request) as ConnectionResult.Failure

    assertThat(result.error).isInstanceOf(ConnectionNotDeclared::class.java)
  }

  @Test
  fun getConnection_noConnectionProviderForRequestedConnection_returnsFailure() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<BogusReadConnection>()

    val result = chronicle.getConnection(request) as ConnectionResult.Failure

    assertThat(result.error).isInstanceOf(ConnectionProviderNotFound::class.java)
  }

  @Test
  fun getConnection_requestPolicyNotFoundInPolicySet_returnsFailure() {
    whenever(policySet.contains(any())).thenReturn(false)

    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    val result = chronicle.getConnection(request) as ConnectionResult.Failure

    assertThat(result.error).isInstanceOf(PolicyNotFound::class.java)
  }

  @Test
  fun getConnection_requestReadConnectionWithNoPolicy_returnsFailure() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>(policy = null)

    val result = chronicle.getConnection(request) as ConnectionResult.Failure

    assertThat(result.error).isInstanceOf(PolicyViolation::class.java)
    assertThat(result.error)
      .hasMessageThat()
      .contains("ConnectionRequest.policy must be non-null for ReadConnection requests")
  }

  @Test
  fun getConnection_requestReadConnectionWithNonNullInvalidPolicy_returnsFailure() {
    val policyResult = PolicyCheckResult.Fail(listOf(PolicyCheck("MyCheck")))
    whenever(policyEngine.checkPolicy(any(), any(), any(), any())).thenReturn(policyResult)

    val chronicle = DefaultChronicle(context, policyEngine, config, flags)
    val request = createConnectionRequest<FooReader>()

    val result = chronicle.getConnection(request) as ConnectionResult.Failure
    val error = result.error as PolicyViolation

    assertThat(error).hasMessageThat().contains(policyResult.message)
  }

  @Test
  fun getAvailableConnectionTypes_kClass() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)

    val availableFooTypes = chronicle.getAvailableConnectionTypes(Foo::class)
    val availableBarTypes = chronicle.getAvailableConnectionTypes(Bar::class)
    val availableUnsupportedTypes = chronicle.getAvailableConnectionTypes(UnsupportedType::class)

    assertThat(availableFooTypes.readConnections)
      .containsExactly(FooReader::class.java, FooStream::class.java)
    assertThat(availableFooTypes.writeConnections).containsExactly(FooWriter::class.java)
    assertThat(availableBarTypes.readConnections).containsExactly(BarReader::class.java)
    assertThat(availableBarTypes.writeConnections).containsExactly(BarWriter::class.java)
    assertThat(availableUnsupportedTypes).isEqualTo(Chronicle.ConnectionTypes.EMPTY)
  }

  @Test
  fun getAvailableConnectionTypes_javaClass() {
    val chronicle = DefaultChronicle(context, policyEngine, config, flags)

    val availableFooTypes = chronicle.getAvailableConnectionTypes(Foo::class.java)
    val availableBarTypes = chronicle.getAvailableConnectionTypes(Bar::class.java)
    val availableUnsupportedTypes =
      chronicle.getAvailableConnectionTypes(UnsupportedType::class.java)

    assertThat(availableFooTypes.readConnections)
      .containsExactly(FooReader::class.java, FooStream::class.java)
    assertThat(availableFooTypes.writeConnections).containsExactly(FooWriter::class.java)
    assertThat(availableBarTypes.readConnections).containsExactly(BarReader::class.java)
    assertThat(availableBarTypes.writeConnections).containsExactly(BarWriter::class.java)
    assertThat(availableUnsupportedTypes).isEqualTo(Chronicle.ConnectionTypes.EMPTY)
  }

  private inline fun <reified T : Connection> createConnectionRequest(
    connectionType: Class<T> = T::class.java,
    requester: ProcessorNode =
      object : ProcessorNode {
        override val requiredConnectionTypes: Set<Class<out Connection>> = setOf(connectionType)
      },
    policy: Policy? = POLICY,
  ): ConnectionRequest<T> = ConnectionRequest(connectionType, requester, policy)

  interface BogusReadConnection : ReadConnection

  data class UnsupportedType(val code: Int)

  data class Foo(val name: String)

  interface FooReader : ReadConnection

  interface FooWriter : WriteConnection

  interface FooStream : ReadConnection

  object FooConnectionProvider : ConnectionProvider {
    override val dataType: DataType =
      ManagedDataType(FOO_DTD, ManagementStrategy.PassThru, FooReader::class, FooWriter::class)

    override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
      return when (connectionRequest.connectionType) {
        FooReader::class.java -> object : FooReader {}
        FooWriter::class.java -> object : FooWriter {}
        else -> throw IllegalArgumentException()
      }
    }
  }

  object FooStreamConnectionProvider : ConnectionProvider {
    override val dataType: DataType =
      ManagedDataType(FOO_DTD, ManagementStrategy.PassThru, FooStream::class)

    override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
      return when (connectionRequest.connectionType) {
        FooStream::class.java -> object : FooStream {}
        else -> throw IllegalArgumentException()
      }
    }
  }

  data class Bar(val name: String, val age: Int)

  interface BarReader : ReadConnection

  interface BarWriter : WriteConnection

  object BarConnectionProvider : ConnectionProvider {
    override val dataType: DataType =
      ManagedDataType(BAR_DTD, ManagementStrategy.PassThru, BarReader::class, BarWriter::class)

    override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection {
      return when (connectionRequest.connectionType) {
        BarReader::class.java -> object : BarReader {}
        BarWriter::class.java -> object : BarWriter {}
        else -> throw IllegalArgumentException()
      }
    }
  }

  companion object {
    private val POLICY =
      policy("TestPolicy", "Testing") {
        description = "This is my test policy: there are many like, it but this is my own."
      }

    private val FOO_DTD = dataTypeDescriptor("Foo", Foo::class)
    private val BAR_DTD = dataTypeDescriptor("Bar", Bar::class)
  }
}
