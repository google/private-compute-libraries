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

package com.google.android.libraries.pcc.chronicle.codegen.processor;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaOpaqueType_GeneratedKt;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaType;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeConnectionProvider;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeGeneratedStorageProviderModule;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeReaderWithDataCache;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeReaderWithDataCacheImpl;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeWriterWithDataCache;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeWriterWithDataCacheImpl;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaType_GeneratedKt;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaType_Generated_ConnectionsKt;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaType_Generated_StorageKt;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleNestedJavaType_GeneratedKt;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleSelfReferentialJavaType_GeneratedKt;
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes.ExpectedJavaGeneratedDtdKt;
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheReader;
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage;
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheWriter;
import com.google.android.libraries.pcc.chronicle.storage.datacache.DefaultManagedDataCacheConnectionProvider;
import com.google.android.libraries.pcc.chronicle.storage.datacache.ManagedDataCache;
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import java.time.Duration;
import java.util.Set;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Tests that Java data classes annotated with @ChronicleData and @DataCacheStore generate expected
 * results that are accessible in other Java files.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
@Config(application = ChronicleDataAnnotationProcessorJavaTest_Application.class)
public class ChronicleDataAnnotationProcessorJavaTest {
  @Rule public final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

  @Inject DataCacheStorage dataCacheStorage;

  @Inject ManagedDataCache<ExampleJavaType> managedDataCache;

  @Inject Set<DataTypeDescriptor> dataTypeDescriptors;

  @Before
  public void setUp() {
    hiltRule.inject();
  }

  @Test
  public void generatedClass_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleJavaType with the manually written, expected DTD.
    assertThat(ExampleJavaType_GeneratedKt.EXAMPLE_JAVA_TYPE_GENERATED_DTD)
        .isEqualTo(ExpectedJavaGeneratedDtdKt.EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_DTD);
  }

  @Test
  public void generatedNestedClass_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleNestedJavaType with the manually written, expected DTD.
    assertThat(ExampleNestedJavaType_GeneratedKt.EXAMPLE_NESTED_JAVA_TYPE_GENERATED_DTD)
        .isEqualTo(ExpectedJavaGeneratedDtdKt.EXPECTED_EXAMPLE_NESTED_JAVA_TYPE_GENERATED_DTD);
  }

  @Test
  public void generatedClassWithIBinder_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleJavaIBinderType with the manually written, expected DTD.
    assertThat(ExampleJavaOpaqueType_GeneratedKt.EXAMPLE_JAVA_OPAQUE_TYPE_GENERATED_DTD)
        .isEqualTo(ExpectedJavaGeneratedDtdKt.EXPECTED_EXAMPLE_JAVA_OPAQUE_TYPE_GENERATED_DTD);
  }

  @Test
  public void generatedSelfReferentialClass_equalsExpectedDTD() {
    // Compare the generated DTD for ExampleSelfReferentialJavaType with the manually written,
    // expected DTD.
    assertThat(
            ExampleSelfReferentialJavaType_GeneratedKt
                .EXAMPLE_SELF_REFERENTIAL_JAVA_TYPE_GENERATED_DTD)
        .isEqualTo(
            ExpectedJavaGeneratedDtdKt.EXPECTED_EXAMPLE_SELF_REFERENTIAL_JAVA_TYPE_GENERATED_DTD);
  }

  @Test
  public void generatedDtds_injectedIntoSet() {
    assertThat(dataTypeDescriptors)
        .containsAtLeast(
            ExampleJavaType_GeneratedKt.EXAMPLE_JAVA_TYPE_GENERATED_DTD,
            ExampleNestedJavaType_GeneratedKt.EXAMPLE_NESTED_JAVA_TYPE_GENERATED_DTD,
            ExampleJavaOpaqueType_GeneratedKt.EXAMPLE_JAVA_OPAQUE_TYPE_GENERATED_DTD,
            ExampleSelfReferentialJavaType_GeneratedKt
                .EXAMPLE_SELF_REFERENTIAL_JAVA_TYPE_GENERATED_DTD);
  }

  @Test
  public void generatedSet_equalsExpectedConnections() {
    // Compare the generated connections for ExampleJavaType with the manually written, expeced
    // connections.
    assertThat(ExampleJavaType_Generated_ConnectionsKt.EXAMPLE_JAVA_TYPE_GENERATED_CONNECTIONS)
        .isEqualTo(ExpectedJavaGeneratedDtdKt.EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_CONNECTIONS);
  }

  @Test
  public void generatedClass_equalsExpectedManagementStrategy() {
    // Compare the generated ManagementStrategy for ExampleJavaType with the manually written,
    // expected ManagementStrategy.
    assertThat(
            ExampleJavaType_Generated_StorageKt
                .EXAMPLE_JAVA_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY)
        .isEqualTo(
            ExpectedJavaGeneratedDtdKt
                .EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY);
  }

  @Test
  public void generatedInt_equalsExpectedMaxItems() {
    // Compare the generated maxItems for ExampleJavaType with the manually written,
    // expected maxItems.
    assertThat(ExampleJavaType_Generated_StorageKt.EXAMPLE_JAVA_TYPE_GENERATED_MAX_ITEMS)
        .isEqualTo(ExpectedJavaGeneratedDtdKt.EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_MAX_ITEMS);
  }

  @Test
  public void generatedModuleFunctionCall_returnsExpectedStorage() {
    // Compare the storage obtained by calling the provider function in the generated module for
    // ExampleKotlinType with the manually written, expected storage.

    ManagedDataCache<ExampleJavaType> expectedStorage =
        new ManagedDataCache<>(
            ExampleJavaType.class,
            dataCacheStorage,
            1000,
            Duration.ofDays(2),
            ExampleJavaType_GeneratedKt.EXAMPLE_JAVA_TYPE_GENERATED_DTD);

    ManagedDataCache<ExampleJavaType> generatedStorage =
        ExampleJavaTypeGeneratedStorageProviderModule.provideExampleJavaTypeManagedDataCacheStorage(
            dataCacheStorage);

    assertThat(TestUtilsKt.configEquals(generatedStorage, expectedStorage)).isTrue();
  }

  @Test
  public void generatedModule_providesExpectedStorageViaHilt() {
    ManagedDataCache<ExampleJavaType> expectedStorage =
        new ManagedDataCache<>(
            ExampleJavaType.class,
            dataCacheStorage,
            1000,
            Duration.ofDays(2),
            ExampleJavaType_GeneratedKt.EXAMPLE_JAVA_TYPE_GENERATED_DTD);

    assertThat(TestUtilsKt.configEquals(managedDataCache, expectedStorage)).isTrue();
  }

  @Test
  public void generatedReader_isComposedOfReadConnectionAndDataCacheReader() {
    Class<ExampleJavaTypeReaderWithDataCacheImpl> cls =
        ExampleJavaTypeReaderWithDataCacheImpl.class;
    assertThat(cls).isAssignableTo(ExampleJavaTypeReaderWithDataCache.class);
    assertThat(cls).isAssignableTo(DataCacheReader.class);
  }

  @Test
  public void generatedWriter_isComposedOfWriteConnectionAndDataCacheWriter() {
    Class<ExampleJavaTypeWriterWithDataCacheImpl> cls =
        ExampleJavaTypeWriterWithDataCacheImpl.class;
    assertThat(cls).isAssignableTo(ExampleJavaTypeWriterWithDataCache.class);
    assertThat(cls).isAssignableTo(DataCacheWriter.class);
  }

  @Test
  public void generatedConnectionProvider_extendsDefaultManagedDataCacheConnectionProvider() {
    Class<ExampleJavaTypeConnectionProvider> cls = ExampleJavaTypeConnectionProvider.class;
    assertThat(cls).isAssignableTo(DefaultManagedDataCacheConnectionProvider.class);
  }
}
