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

package com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.expectedgeneratedtypes

import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaOpaqueType
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaType
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeReadWriter
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeReader
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeReaderWithDataCache
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeWriter
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleJavaTypeWriterWithDataCache
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleNestedJavaType
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleSelfReferentialJavaType
import java.time.Duration

@JvmField
val EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_DTD =
  dataTypeDescriptor(ExampleJavaType::class.qualifiedName!!, cls = ExampleJavaType::class) {
    "name" to FieldType.String
    "amount" to FieldType.Integer
    "others" to FieldType.List(FieldType.String)
    "updateTime" to FieldType.Instant
    "timeSinceLastOpen" to FieldType.Duration
    "featuresEnabled" to
      FieldType.List(
        dataTypeDescriptor(name = "MapStringValueToBooleanValue", Map.Entry::class) {
          "key" to FieldType.String
          "value" to FieldType.Boolean
        }
      )
    "nickName" to FieldType.Nullable(FieldType.String)
    "categoryAndScore" to FieldType.Tuple(listOf(FieldType.String, FieldType.Double))
    "status" to
      FieldType.Enum(
        "com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes" +
          ".ExampleJavaType.Status",
        listOf("ON", "OFF", "UNKNOWN")
      )
  }

@JvmField
val EXPECTED_EXAMPLE_NESTED_JAVA_TYPE_GENERATED_DTD =
  dataTypeDescriptor(
    ExampleNestedJavaType::class.qualifiedName!!,
    cls = ExampleNestedJavaType::class
  ) {
    "name" to FieldType.String
    "nested" to
      dataTypeDescriptor(ExampleJavaType::class.qualifiedName!!, cls = ExampleJavaType::class) {
        "name" to FieldType.String
        "amount" to FieldType.Integer
        "others" to FieldType.List(FieldType.String)
        "updateTime" to FieldType.Instant
        "timeSinceLastOpen" to FieldType.Duration
        "featuresEnabled" to
          FieldType.List(
            dataTypeDescriptor(name = "MapStringValueToBooleanValue", Map.Entry::class) {
              "key" to FieldType.String
              "value" to FieldType.Boolean
            }
          )
        "nickName" to FieldType.Nullable(FieldType.String)
        "categoryAndScore" to FieldType.Tuple(listOf(FieldType.String, FieldType.Double))
        "status" to
          FieldType.Enum(
            "com.google.android.libraries.pcc.chronicle.codegen.processor.testdata." +
              "annotatedtypes.ExampleJavaType.Status",
            listOf("ON", "OFF", "UNKNOWN")
          )
      }
  }

@JvmField
val EXPECTED_EXAMPLE_JAVA_OPAQUE_TYPE_GENERATED_DTD =
  dataTypeDescriptor(
    ExampleJavaOpaqueType::class.qualifiedName!!,
    cls = ExampleJavaOpaqueType::class
  ) {
    "name" to FieldType.String
    "iBinder" to FieldType.Opaque("android.os.IBinder")
  }

@JvmField
val EXPECTED_EXAMPLE_SELF_REFERENTIAL_JAVA_TYPE_GENERATED_DTD =
  dataTypeDescriptor(
    ExampleSelfReferentialJavaType::class.qualifiedName!!,
    cls = ExampleSelfReferentialJavaType::class
  ) {
    "children" to
      FieldType.List(FieldType.Reference(ExampleSelfReferentialJavaType::class.qualifiedName!!))
  }

@JvmField
val EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_CONNECTIONS =
  setOf(
    ExampleJavaTypeReader::class.java,
    ExampleJavaTypeWriter::class.java,
    ExampleJavaTypeReadWriter::class.java,
    ExampleJavaTypeReaderWithDataCache::class.java,
    ExampleJavaTypeWriterWithDataCache::class.java,
  )

@JvmField val EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_MAX_ITEMS = 1000

@JvmField
val EXPECTED_EXAMPLE_JAVA_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY =
  ManagementStrategy.Stored(
    encrypted = false,
    media = StorageMedia.MEMORY,
    ttl = Duration.ofDays(2),
    deletionTriggers = setOf(DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "packageName")),
  )
