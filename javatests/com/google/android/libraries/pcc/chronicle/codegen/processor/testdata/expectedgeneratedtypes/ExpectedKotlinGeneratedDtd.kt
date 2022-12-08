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
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinOpaqueType
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinType
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeReadWriter
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeReader
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeReaderWithDataCache
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeWriter
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleKotlinTypeWriterWithDataCache
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleNestedKotlinType
import com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes.ExampleSelfReferentialKotlinType
import java.time.Duration

@JvmField
val EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_DTD =
  dataTypeDescriptor(ExampleKotlinType::class.qualifiedName!!, cls = ExampleKotlinType::class) {
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
    "threeThings" to FieldType.Tuple(listOf(FieldType.String, FieldType.Double, FieldType.Boolean))
    "status" to
      FieldType.Enum(
        "com.google.android.libraries.pcc.chronicle.codegen.processor.testdata." +
          "annotatedtypes.ExampleKotlinType.Status",
        listOf("ON", "OFF", "UNKNOWN")
      )
  }

@JvmField
val EXPECTED_EXAMPLE_NESTED_KOTLIN_TYPE_GENERATED_DTD =
  dataTypeDescriptor(
    ExampleNestedKotlinType::class.qualifiedName!!,
    cls = ExampleNestedKotlinType::class
  ) {
    "name" to FieldType.String
    "nested" to
      dataTypeDescriptor(ExampleKotlinType::class.qualifiedName!!, cls = ExampleKotlinType::class) {
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
        "threeThings" to
          FieldType.Tuple(listOf(FieldType.String, FieldType.Double, FieldType.Boolean))
        "status" to
          FieldType.Enum(
            "com.google.android.libraries.pcc.chronicle.codegen.processor.testdata." +
              "annotatedtypes.ExampleKotlinType.Status",
            listOf("ON", "OFF", "UNKNOWN")
          )
      }
  }

@JvmField
val EXPECTED_EXAMPLE_KOTLIN_OPAQUE_TYPE_GENERATED_DTD =
  dataTypeDescriptor(
    ExampleKotlinOpaqueType::class.qualifiedName!!,
    cls = ExampleKotlinOpaqueType::class
  ) {
    "name" to FieldType.String
    "iBinder" to FieldType.Opaque("android.os.IBinder")
  }

@JvmField
val EXPECTED_EXAMPLE_SELF_REFERENTIAL_KOTLIN_TYPE_GENERATED_DTD =
  dataTypeDescriptor(
    ExampleSelfReferentialKotlinType::class.qualifiedName!!,
    cls = ExampleSelfReferentialKotlinType::class
  ) {
    "children" to
      FieldType.List(FieldType.Reference(ExampleSelfReferentialKotlinType::class.qualifiedName!!))
  }

val EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_CONNECTIONS =
  setOf(
    ExampleKotlinTypeReader::class.java,
    ExampleKotlinTypeWriter::class.java,
    ExampleKotlinTypeReadWriter::class.java,
    ExampleKotlinTypeReaderWithDataCache::class.java,
    ExampleKotlinTypeWriterWithDataCache::class.java,
  )

val EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_MAX_ITEMS = 1000

val EXPECTED_EXAMPLE_KOTLIN_TYPE_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY =
  ManagementStrategy.Stored(
    encrypted = false,
    media = StorageMedia.MEMORY,
    ttl = Duration.ofDays(2),
    deletionTriggers = setOf(DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "packageName")),
  )
