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

import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory
import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory.ListValue
import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory.MapValue
import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.util.presenceConditional
import com.google.android.libraries.pcc.chronicle.codegen.util.toChronicleFieldName
import com.google.android.libraries.pcc.chronicle.codegen.util.toProtoFieldName
import com.google.android.libraries.pcc.chronicle.codegen.util.toTypeOneOfs
import com.google.android.libraries.pcc.chronicle.codegen.util.typeLocation
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import java.util.LinkedList

/**
 * Class for converting Protos into a list of [Type] objects representing their contents for further
 * conversion into arcs manifests
 */
class DescriptorToTypeConverter(private val config: Configuration = Configuration()) :
  TypeConverter<Descriptor> {
  override fun convertToTypes(initialElement: Descriptor): TypeSet {
    val typesByDescriptor = mutableMapOf<Descriptor, Type>()
    var primaryType: Type? = null

    val queue = LinkedList<Descriptor>().apply { push(initialElement) }
    val visited = mutableSetOf(initialElement)
    while (queue.isNotEmpty()) {
      val current = queue.poll() ?: break
      val (type, nested) = convertToTypeInternal(current)

      primaryType = primaryType ?: type
      typesByDescriptor[current] = type

      nested.forEach {
        if (it in visited) return@forEach
        queue.push(it)
        visited += it
      }
    }

    return TypeSet(
      primary = requireNotNull(primaryType) { "Type not found" },
      additional = typesByDescriptor.values.toSet() - primaryType
    )
  }

  private data class ConvertedDescriptor(val type: Type, val nestedNeedingEval: Set<Descriptor>)

  private data class ConvertedFieldDescriptor(
    val fields: List<FieldEntry>,
    val nestedTypes: Set<Descriptor>
  )

  private data class ConvertedFieldCategory(
    val category: FieldCategory,
    val nestedTypes: Set<Descriptor>
  )

  /**
   * Create a [Type] for the provided [Descriptor], unless somewhere up the type graph has already
   * started such a conversion.
   *
   * It's important that the type generation strategy not rely on this actually returning the
   * generated type, so that cyclic references don't put us in an endless loop.
   */
  private fun convertToTypeInternal(descriptor: Descriptor): ConvertedDescriptor {
    val oneOfs = descriptor.oneofs.toTypeOneOfs()
    val nested = mutableSetOf<Descriptor>()
    val fields =
      descriptor.fields.filter { !it.isIgnored() }.flatMap {
        // If the field is part of a oneOf, make it nullable (if nullable support is on)
        val isNullable =
          it.name.toChronicleFieldName() in oneOfs.oneOfForField && config.enableNullableSupport

        val (fieldEntries, nestedTypes) = it.toFieldEntries(isNullable = isNullable)

        nested += nestedTypes
        fieldEntries
      }

    val result =
      Type(
        descriptor.typeLocation(config.useJavaPackageInTypeLocations),
        jvmLocation = descriptor.typeLocation(useJavaPackage = true),
        fields = fields,
        oneOfs = oneOfs,
        tooling = Type.Tooling.PROTO
      )
    return ConvertedDescriptor(result, nested)
  }

  private fun FieldDescriptor.isIgnored() =
    config.ignoredFieldNames.contains(name) ||
      config.ignoredMessageType.contains(
        when (type) {
          FieldDescriptor.Type.MESSAGE -> messageType.name
          else -> type.name
        }
      )

  /**
   * Converts the [FieldDescriptor] to the appropriate [FieldEntry]. If there's [ForeignReference]
   * configuration, this will represent this using [FieldCategory.ForeignReference]. If the
   * [additionalNamedField] value is non-empty, the method will return two field entries: the
   * original, non-foreign-reference entry, and the foreign-reference entry, using the alternate
   * name provided.
   */
  private fun FieldDescriptor.toFieldEntries(isNullable: Boolean): ConvertedFieldDescriptor {
    val foreignReference = config.foreignReferences.firstOrNull { it.fieldName == name }
    val includeNormalField = foreignReference == null || foreignReference.additionalNamedField != ""

    val foreignReferenceFieldEntry =
      foreignReference?.let {
        val foreignReferenceName =
          if (it.additionalNamedField.isEmpty()) name else it.additionalNamedField
        FieldEntry(
          foreignReferenceName.toChronicleFieldName(),
          FieldCategory.ForeignReference(it.schemaName, it.hard),
          name.toChronicleFieldName(),
          presenceConditional()
        )
      }

    return if (includeNormalField) {
      val (normal, nested) = toFieldEntry(isNullable)
      ConvertedFieldDescriptor(normal + listOfNotNull(foreignReferenceFieldEntry), nested)
    } else {
      ConvertedFieldDescriptor(listOfNotNull(foreignReferenceFieldEntry), emptySet())
    }
  }

  /** Create a field entry, including the presence conditional name if applicable. */
  private fun FieldDescriptor.toFieldEntry(isNullable: Boolean = false): ConvertedFieldDescriptor {
    val (category, nested) = toFieldCategory()

    return ConvertedFieldDescriptor(
      listOf(
        FieldEntry(
          name = name.toChronicleFieldName(),
          category = if (isNullable) FieldCategory.NullableValue(category) else category,
          sourceName = toProtoFieldName(config.convertBytesFieldsToStrings),
          presenceCondition = presenceConditional()
        )
      ),
      nested
    )
  }

  /**
   * Main entry point to field conversion. It will take care of maps & repeated fields. Otherwise it
   * just calls out to [baseTypeToFieldCategory].
   */
  private fun FieldDescriptor.toFieldCategory(): ConvertedFieldCategory {
    return when {
      // Note: map fields can not be repeated.
      this.isMapField -> mapFieldToFieldCategory()
      this.isRepeated -> {
        val (listTypeCategory, nestedDescriptors) = this.baseTypeToFieldCategory()
        ConvertedFieldCategory(ListValue(LIST_TYPE_LOCATION, listTypeCategory), nestedDescriptors)
      }
      else -> this.baseTypeToFieldCategory()
    }
  }

  /**
   * Conversion handler for most fields. It also deals with recursive fields for nested field types.
   *
   * It does not handle [isMapField] or [isRepeated]
   * - it's intended to be called by the method above which does handle those.
   */
  private fun FieldDescriptor.baseTypeToFieldCategory(): ConvertedFieldCategory {
    val (category, nested) =
      when (this.type) {
        FieldDescriptor.Type.STRING -> FieldCategory.StringValue to emptySet()
        FieldDescriptor.Type.BYTES -> {
          if (config.convertBytesFieldsToStrings) {
            FieldCategory.StringValue to emptySet()
          } else {
            FieldCategory.ByteArrayValue to emptySet()
          }
        }
        FieldDescriptor.Type.INT32,
        FieldDescriptor.Type.UINT32,
        FieldDescriptor.Type.SINT32,
        FieldDescriptor.Type.FIXED32,
        FieldDescriptor.Type.SFIXED32 -> FieldCategory.IntValue to emptySet()
        FieldDescriptor.Type.INT64,
        FieldDescriptor.Type.UINT64,
        FieldDescriptor.Type.SINT64,
        FieldDescriptor.Type.FIXED64,
        FieldDescriptor.Type.SFIXED64 -> FieldCategory.LongValue to emptySet()
        FieldDescriptor.Type.FLOAT -> FieldCategory.FloatValue to emptySet()
        FieldDescriptor.Type.DOUBLE -> FieldCategory.DoubleValue to emptySet()
        FieldDescriptor.Type.BOOL -> FieldCategory.BooleanValue to emptySet()
        FieldDescriptor.Type.ENUM ->
          FieldCategory.EnumValue(
            location = enumType.typeLocation(config.useJavaPackageInTypeLocations),
            jvmLocation = enumType.typeLocation(useJavaPackage = true),
            possibleValues = enumType.values.map { it.name }
          ) to emptySet()
        FieldDescriptor.Type.MESSAGE, FieldDescriptor.Type.GROUP -> {
          FieldCategory.NestedTypeValue(
            location = messageType.typeLocation(config.useJavaPackageInTypeLocations),
            jvmLocation = messageType.typeLocation(useJavaPackage = true)
          ) to setOf(messageType)
        }
        else -> throw DescriptorConversionException("Unable to handle FieldDescriptor type $type")
      }

    return ConvertedFieldCategory(category, nested)
  }

  /**
   * Helper for handling map fields by generating field conversion for their key and value types
   * (including any necessary nested type conversions), and then returning a [MapValue].
   */
  private fun FieldDescriptor.mapFieldToFieldCategory(): ConvertedFieldCategory {
    val (keyFieldCategory, keyDescriptors) =
      this.messageType.findFieldByName("key").toFieldCategory()
    val (valueFieldCategory, valueDescriptors) =
      this.messageType.findFieldByName("value").toFieldCategory()
    return ConvertedFieldCategory(
      category = MapValue(MAP_TYPE_LOCATION, keyFieldCategory, valueFieldCategory),
      nestedTypes = keyDescriptors + valueDescriptors
    )
  }

  /** Holds a set of configuration options that can be passed [ClassToTypeConverter]. */
  data class Configuration(
    /** Any field of this messageType will not be included in the output. */
    val ignoredMessageType: Set<String> = emptySet(),

    /** Any field of this name encountered during processing will not be included in the output. */
    val ignoredFieldNames: Set<String> = emptySet(),

    /** Enable support for generating nullable fields from oneOfs. */
    val enableNullableSupport: Boolean = false,

    /** Indicates that a field should be represented using a foreign reference. */
    val foreignReferences: Set<ForeignReference> = emptySet(),

    /**
     * When building type locations, use the option.java_package value instead of the proto package.
     */
    val useJavaPackageInTypeLocations: Boolean = true,

    /** whether or not to convert `bytes` fields to strings. */
    val convertBytesFieldsToStrings: Boolean = true,
  )

  class DescriptorConversionException(msg: String) : UnsupportedOperationException(msg)

  companion object {
    private val LIST_TYPE_LOCATION = TypeLocation("List", pkg = "java.util")
    private val MAP_TYPE_LOCATION = TypeLocation("Map", pkg = "java.util")
  }
}
