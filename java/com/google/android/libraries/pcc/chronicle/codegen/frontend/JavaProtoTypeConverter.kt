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

package com.google.android.libraries.pcc.chronicle.codegen.frontend

import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.protobuf.Descriptors
import com.google.protobuf.GeneratedMessage

/**
 * This [TypeConverter] implementation is a wrapper around a [DescriptorToTypeConverter] that will
 * attempt to treat the provided type as a protobuf.
 *
 * If the provided [java.lang.reflect.Type] is a [GeneratedMessage] and its [getDescriptorForType]
 * method returns a valid value, then the provided [DescriptorToTypeConverter] converter will be
 * used to create a set of [Type] items for Chronicle Codegen.
 *
 * If it can't be interpreted as a protobuf, an empty set is returned.
 */
class JavaProtoTypeConverter(
  private val descriptorConfig: DescriptorToTypeConverter.Configuration =
    DescriptorToTypeConverter.Configuration()
) : TypeConverter<java.lang.reflect.Type> {
  override fun convertToTypes(
    initialElement: java.lang.reflect.Type,
  ): TypeSet? {
    val descriptor = initialElement.getProtobufDescriptor() ?: return null

    return DescriptorToTypeConverter(descriptorConfig).convertToTypes(descriptor)
  }

  private fun java.lang.reflect.Type.getProtobufDescriptor(): Descriptors.Descriptor? {
    if (this !is Class<*>) return null

    // Create a new instance of the type using ProtoClass.newBuilder().getDescriptorForType() via
    // reflection.
    return try {
      val builderMethod = getDeclaredMethod("newBuilder")
      val builder = builderMethod.invoke(null)
      val descriptorMethod = builder.javaClass.getDeclaredMethod("getDescriptorForType")

      // Return the descriptor on the new instance.
      descriptorMethod.invoke(builder) as Descriptors.Descriptor
    } catch (e: NoSuchMethodException) {
      // The class must not have been a Java proto class.
      null
    }
  }
}
