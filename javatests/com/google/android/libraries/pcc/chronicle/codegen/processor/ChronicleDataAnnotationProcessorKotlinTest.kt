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

package com.google.android.libraries.pcc.chronicle.codegen.processor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.EXAMPLE_KOTLIN_OPAQUE_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.EXAMPLE_KOTLIN_TYPE_GENERATED_CONNECTIONS
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.EXAMPLE_KOTLIN_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.EXAMPLE_KOTLIN_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.EXAMPLE_KOTLIN_TYPE_GENERATED_MAX_ITEMS
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.EXAMPLE_NESTED_KOTLIN_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.EXAMPLE_SELF_REFERENTIAL_KOTLIN_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeConnectionProvider
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeReaderWithDataCache
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeReaderWithDataCacheImpl
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeWriterWithDataCache
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeWriterWithDataCacheImpl
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.EXPECTED_EXAMPLE_KOTLIN_OPAQUE_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_CONNECTIONS
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_MAX_ITEMS
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.EXPECTED_EXAMPLE_NESTED_KOTLIN_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.EXPECTED_EXAMPLE_SELF_REFERENTIAL_KOTLIN_TYPE_GENERATED_DTD
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheReader
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheWriter
import com.google.android.libraries.pcc.chronicle.storage.datacache.DefaultManagedDataCacheConnectionProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that Kotlin data classes annotated with @ChronicleData, and @DataCacheStore generate
 * expected results that are accessible in other Kotlin files.
 */
@RunWith(AndroidJUnit4::class)
class ChronicleDataAnnotationProcessorKotlinTest {
  @Test
  fun generatedClass_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleKotlinType with the manually written, expected DTD.
    assertThat(EXAMPLE_KOTLIN_TYPE_GENERATED_DTD)
      .isEqualTo(EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_DTD)
  }

  @Test
  fun generatedNestedClass_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleNestedKotlinType with the manually written, expected
    // DTD.
    assertThat(EXAMPLE_NESTED_KOTLIN_TYPE_GENERATED_DTD)
      .isEqualTo(EXPECTED_EXAMPLE_NESTED_KOTLIN_TYPE_GENERATED_DTD)
  }

  @Test
  fun generatedClassWithIBinder_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleKotlinIBinderType with the manually written, expected
    // DTD.
    assertThat(EXAMPLE_KOTLIN_OPAQUE_TYPE_GENERATED_DTD)
      .isEqualTo(EXPECTED_EXAMPLE_KOTLIN_OPAQUE_TYPE_GENERATED_DTD)
  }

  @Test
  fun generatedSelfReferentialClass_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleSelfReferentialKotlinType with the manually written,
    // expected DTD.
    assertThat(EXAMPLE_SELF_REFERENTIAL_KOTLIN_TYPE_GENERATED_DTD)
      .isEqualTo(EXPECTED_EXAMPLE_SELF_REFERENTIAL_KOTLIN_TYPE_GENERATED_DTD)
  }

  @Test
  fun generatedSet_equalsExpectedConnections() {
    // Compare the generated connections for ExampleKotlinType and with the manually written,
    // expected connections.
    assertThat(EXAMPLE_KOTLIN_TYPE_GENERATED_CONNECTIONS)
      .isEqualTo(EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_CONNECTIONS)
  }

  @Test
  fun generatedClass_equalsExpectedManagementStrategy() {
    // Compare the generated ManagementStrategy for ExampleKotlinType with the manually written,
    // expected ManagementStrategy.
    assertThat(EXAMPLE_KOTLIN_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY)
      .isEqualTo(EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY)
  }

  @Test
  fun generatedInt_equalsExpectedMaxItems() {
    // Compare the generated maxItems for ExampleKotlinType with the manually written,
    // expected maxItems.
    assertThat(EXAMPLE_KOTLIN_TYPE_GENERATED_MAX_ITEMS)
      .isEqualTo(EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_MAX_ITEMS)
  }

  @Test
  fun generatedReader_isComposedOfReadConnectionAndDataCacheReader() {
    val cls = ExampleKotlinTypeReaderWithDataCacheImpl::class.java
    assertThat(cls).isAssignableTo(ExampleKotlinTypeReaderWithDataCache::class.java)
    assertThat(cls).isAssignableTo(DataCacheReader::class.java)
  }

  @Test
  fun generatedWriter_isComposedOfWriteConnectionAndDataCacheWriter() {
    val cls = ExampleKotlinTypeWriterWithDataCacheImpl::class.java
    assertThat(cls).isAssignableTo(ExampleKotlinTypeWriterWithDataCache::class.java)
    assertThat(cls).isAssignableTo(DataCacheWriter::class.java)
  }

  @Test
  fun generatedConnectionProvider_extendsDefaultManagedDataCacheConnectionProvider() {
    val cls = ExampleKotlinTypeConnectionProvider::class.java
    assertThat(cls).isAssignableTo(DefaultManagedDataCacheConnectionProvider::class.java)
  }
}
