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

package com.google.android.libraries.pcc.chronicle.api.remote.testutil

import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor

/** [DataTypeDescriptor] for [SimpleProtoMessage]. */
val SIMPLE_PROTO_MESSAGE_DTD =
  dataTypeDescriptor(
    "chronicle.api.remote.testutil.SimpleProtoMessage",
    SimpleProtoMessage::class
  ) {
    "double_field" to FieldType.Double
    "float_field" to FieldType.Float
    "int_field" to FieldType.Integer
    "unsigned_int_field" to FieldType.Integer
    "signed_int_field" to FieldType.Integer
    "fixed_width_int_field" to FieldType.Integer
    "signed_fixed_width_int_field" to FieldType.Integer
    "long_field" to FieldType.Long
    "unsigned_long_field" to FieldType.Long
    "signed_long_field" to FieldType.Long
    "fixed_width_long_field" to FieldType.Long
    "signed_fixed_width_long_field" to FieldType.Long
    "bool_field" to FieldType.Boolean
    "string_field" to FieldType.String
    "enum_field" to FieldType.Integer
  }

/** [DataTypeDescriptor] for [RepeatedProtoMessage]. */
val REPEATED_PROTO_MESSAGE_DTD =
  dataTypeDescriptor(
    "chronicle.serializer.testutil.RepeatedProtoMessage",
    RepeatedProtoMessage::class
  ) { "int_values" to FieldType.List(FieldType.Integer) }

/** [DataTypeDescriptor] for [TreeProtoMessage]. */
val TREE_PROTO_MESSAGE_DTD =
  dataTypeDescriptor("chronicle.serializer.testutil.TreeProtoMessage", TreeProtoMessage::class) {
    "value" to FieldType.Integer
    "children" to FieldType.List(FieldType.Nested("chronicle.serializer.testutil.TreeProtoMessage"))
  }

/** [DataTypeDescriptor] for [NestedProtoMessage]. */
val NESTED_PROTO_MESSAGE_DTD =
  dataTypeDescriptor(
    "chronicle.serializer.testutil.NestedProtoMessage",
    NestedProtoMessage::class
  ) {
    "simple_message" to FieldType.Nested(SIMPLE_PROTO_MESSAGE_DTD.name)
    "repeated_message" to FieldType.Nested(REPEATED_PROTO_MESSAGE_DTD.name)
    "tree_message" to FieldType.Nested(TREE_PROTO_MESSAGE_DTD.name)
  }
