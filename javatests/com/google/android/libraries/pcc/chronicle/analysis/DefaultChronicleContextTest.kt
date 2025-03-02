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

package com.google.android.libraries.pcc.chronicle.analysis

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode
import com.google.android.libraries.pcc.chronicle.api.ReadConnection
import com.google.android.libraries.pcc.chronicle.api.WriteConnection
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.error.ConnectionTypeAmbiguity
import com.google.android.libraries.pcc.chronicle.util.Key
import com.google.android.libraries.pcc.chronicle.util.MutableTypedMap
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.mock

@RunWith(JUnit4::class)
class DefaultChronicleContextTest {
  @Test
  fun moreThanOneConnectibleByConnectionType_throws() {
    val node1 =
      object : ConnectionProvider {
        override val dataType: DataType =
          ManagedDataType(
            descriptor = dataTypeDescriptor("Foo", Unit::class),
            managementStrategy = ManagementStrategy.PassThru,
            connectionTypes = setOf(FooReader::class.java),
          )

        override fun getConnection(
          connectionRequest: ConnectionRequest<out Connection>
        ): Connection = FooReader()
      }
    // Same connection type should cause the error to be thrown. The dataType being different is
    // not relevant to the check.
    val node2 =
      object : ConnectionProvider {
        override val dataType: DataType =
          ManagedDataType(
            descriptor = dataTypeDescriptor("Bar", Unit::class),
            managementStrategy = ManagementStrategy.PassThru,
            connectionTypes = setOf(FooReader::class.java),
          )

        override fun getConnection(
          connectionRequest: ConnectionRequest<out Connection>
        ): Connection = FooReader()
      }

    assertFailsWith<ConnectionTypeAmbiguity> {
      DefaultChronicleContext(setOf(node1, node2), emptySet(), DefaultPolicySet(emptySet()), mock())
    }
  }

  @Test
  fun findConnectionProvider() {
    val readerConnectionProvider = FooReaderConnectionProvider()
    val writerConnectionProvider = FooWriterConnectionProvider()
    val context =
      DefaultChronicleContext(
        setOf(readerConnectionProvider, writerConnectionProvider),
        emptySet(),
        DefaultPolicySet(emptySet()),
        mock(),
      )

    assertThat(context.findConnectionProvider(FooReader::class.java))
      .isSameInstanceAs(readerConnectionProvider)
    assertThat(context.findConnectionProvider(FooWriter::class.java))
      .isSameInstanceAs(writerConnectionProvider)
  }

  @Test
  fun findDataType() {
    val fooReaderConnectionProvider = FooReaderConnectionProvider()
    val fooWriterConnectionProvider = FooWriterConnectionProvider()
    val barReaderWriterConnectionProvider = BarReaderWriterConnectionProvider()
    val context =
      DefaultChronicleContext(
        setOf(
          fooReaderConnectionProvider,
          fooWriterConnectionProvider,
          barReaderWriterConnectionProvider,
        ),
        emptySet(),
        DefaultPolicySet(emptySet()),
        mock(),
      )

    assertThat(context.findDataType(FooReader::class.java))
      .isEqualTo(dataTypeDescriptor("Foo", Unit::class))
    assertThat(context.findDataType(FooWriter::class.java))
      .isEqualTo(dataTypeDescriptor("Foo", Unit::class))
    assertThat(context.findDataType(BarReader::class.java))
      .isEqualTo(dataTypeDescriptor("Bar", Unit::class))
    assertThat(context.findDataType(BarWriter::class.java))
      .isEqualTo(dataTypeDescriptor("Bar", Unit::class))
  }

  @Test
  fun withNode_processor_leavesExistingUnchanged() {
    val context =
      DefaultChronicleContext(
        setOf(FooReaderConnectionProvider()),
        emptySet(),
        DefaultPolicySet(emptySet()),
        mock(),
      )

    val updated = context.withNode(FooBarProcessor())

    assertThat(context)
      .isEqualTo(
        DefaultChronicleContext(
          setOf(FooReaderConnectionProvider()),
          emptySet(),
          DefaultPolicySet(emptySet()),
          mock(),
        )
      )
    assertThat(updated).isNotEqualTo(context)
    assertThat(updated)
      .isEqualTo(
        DefaultChronicleContext(
          setOf(FooReaderConnectionProvider()),
          setOf(FooBarProcessor()),
          DefaultPolicySet(emptySet()),
          mock(),
        )
      )
  }

  @Test
  fun withConnectionContext_processor_leavesExistingUnchanged() {
    val mutableTypedMap = MutableTypedMap()
    val connectionContext = mutableTypedMap.toTypedMap()
    val context =
      DefaultChronicleContext(
        setOf(FooReaderConnectionProvider()),
        emptySet(),
        DefaultPolicySet(emptySet()),
        mock(),
        connectionContext,
      )

    // Update the `connectionContext`
    mutableTypedMap[Name] = "test"
    val otherConnectionContext = mutableTypedMap.toTypedMap()
    val updated = context.withConnectionContext(otherConnectionContext)

    // Initial context should not have the Name key
    assertThat(context.connectionContext[Name]).isNull()
    assertThat(context)
      .isEqualTo(
        DefaultChronicleContext(
          setOf(FooReaderConnectionProvider()),
          emptySet(),
          DefaultPolicySet(emptySet()),
          mock(),
          connectionContext,
        )
      )

    // Updated context has the Name key
    assertThat(updated.connectionContext[Name]).isEqualTo("test")
    assertThat(updated).isNotEqualTo(context)
    assertThat(updated)
      .isEqualTo(
        DefaultChronicleContext(
          setOf(FooReaderConnectionProvider()),
          emptySet(),
          DefaultPolicySet(emptySet()),
          mock(),
          otherConnectionContext,
        )
      )
  }

  // Test key for `connectionContext`
  private object Name : Key<String>

  class FooReader : ReadConnection

  class FooWriter : WriteConnection

  class BarReader : ReadConnection

  class BarWriter : WriteConnection

  class FooReaderConnectionProvider : ConnectionProvider {
    override val dataType: DataType =
      ManagedDataType(
        descriptor = dataTypeDescriptor("Foo", Unit::class),
        managementStrategy = ManagementStrategy.PassThru,
        connectionTypes = setOf(FooReader::class.java),
      )

    override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
      FooReader()

    override fun equals(other: Any?): Boolean = other is FooReaderConnectionProvider

    override fun hashCode(): Int {
      return javaClass.hashCode()
    }
  }

  class FooWriterConnectionProvider : ConnectionProvider {
    override val dataType: DataType =
      ManagedDataType(
        descriptor = dataTypeDescriptor("Foo", Unit::class),
        managementStrategy = ManagementStrategy.PassThru,
        connectionTypes = setOf(FooWriter::class.java),
      )

    override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
      FooWriter()

    override fun equals(other: Any?): Boolean = other is FooWriterConnectionProvider

    override fun hashCode(): Int {
      return javaClass.hashCode()
    }
  }

  class BarReaderWriterConnectionProvider : ConnectionProvider {
    override val dataType: DataType =
      ManagedDataType(
        descriptor = dataTypeDescriptor("Bar", Unit::class),
        managementStrategy = ManagementStrategy.PassThru,
        connectionTypes = setOf(BarReader::class.java, BarWriter::class.java),
      )

    override fun getConnection(connectionRequest: ConnectionRequest<out Connection>): Connection =
      BarWriter()

    override fun equals(other: Any?): Boolean = other is BarReaderWriterConnectionProvider

    override fun hashCode(): Int {
      return javaClass.hashCode()
    }
  }

  class FooBarProcessor : ProcessorNode {
    override val requiredConnectionTypes =
      setOf(
        FooReader::class.java,
        FooWriter::class.java,
        BarReader::class.java,
        BarWriter::class.java,
      )

    override fun equals(other: Any?): Boolean = other is FooBarProcessor

    override fun hashCode(): Int {
      return javaClass.hashCode()
    }
  }
}
