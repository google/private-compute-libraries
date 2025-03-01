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
import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.protobuf.Descriptors.FieldDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type as JavaType
import java.time.Duration
import java.time.Instant

/**
 * Class for converting POJOs/Protos into a list of Type objects representing their contents for
 * further conversion into arcs manifests
 *
 * There are two variants of a type here: a [Type] and a [FieldCategory].
 * * A [Type] is something that will need to be reflected as a `schema` in an Arcs manifest.
 * * A [FieldCategory] represents the Arcs type of a particular field in a schema. It may be a
 *   nested [Type], but it might also be a type that has a representation in the Arcs type
 *   (primitives, Lists, etc).
 */
class ClassToTypeConverter(
  val config: Configuration = Configuration(),
  private val alternateConverters: List<TypeConverter<JavaType>> = emptyList(),
) : TypeConverter<JavaType> {
  /** Internal memo of already-converted [Type]s. */
  private lateinit var primary: Type
  private val converted = mutableMapOf<JavaType, TypeSet>()

  /**
   * Track types that we've *started* converting, but haven't necessarily finished, so we don't keep
   * converting in an endless loop if there are cycles.
   */
  private val startedConverting = mutableSetOf<JavaType>()

  override fun convertToTypes(initialElement: JavaType): TypeSet =
    convertToTypes(initialElement, true)

  /**
   * Converts a java object into a set of Types describing the data inside the class by traversing
   * the fields and converting any non-primitive fields as well.
   */
  fun convertToTypes(initialElement: JavaType, isTopLevel: Boolean): TypeSet {
    convertToTypeInternal(initialElement, isTopLevel)
    return TypeSet(primary, (converted.values.flatten() - primary).toSet())
  }

  /**
   * If run on a [Class] type, all non-static fields declared on the class will be mapped to a
   * [FieldCategory] type. Non-class types don't have fields, and so will return an empty map.
   */
  private fun JavaType.convertTypeFields(): List<FieldEntry> {
    return when (this) {
      is Class<*> -> this.declaredFields.filter { it.isSupported() }.flatMap { it.toFieldEntries() }
      else -> emptyList()
    }
  }

  /**
   * Returns [true] if we should attempt to convert this field.
   *
   * Right now this is all non-static fields, but this will likely evolve over time.
   */
  private fun Field.isSupported() =
    !Modifier.isStatic(modifiers) &&
      !config.ignoredFieldNames.contains(name) &&
      !config.ignoredType.contains(this.type.getRawType())

  /** Simple helper to create [TypeLocation] from info in a [Class]. */
  private fun Class<*>.asTypeLocation() =
    TypeLocation(
      simpleName,
      enclosingNames(),
      // TODO: go/nullness-caller-updates-lsc - Avoid dereferencing possibly null value?
      `package`!!.name,
    )

  /**
   * Implements a recursive depth-first type conversion strategy.
   *
   * If a type has already been visited, the memoized [Type] will be returned. Otherwise, a new type
   * will be created. The fields will be converted to [FieldCategory] instances. This function will
   * be re-entered if a field type is encountered.
   *
   * It's important that the type generation strategy not rely on this actually returning the
   * generated type, so that cyclic references don't put us in an endless loop.
   */
  private fun convertToTypeInternal(inType: JavaType, topLevel: Boolean = false) {
    val rawType = inType.getRawType()

    require(rawType is Class<*>) { "Can not convert non-class types" }

    // Somewhere upstream we're already converting this type, don't try again.
    if (inType in startedConverting) return
    startedConverting += rawType

    val result =
      try {
        alternateConverters.map { it.convertToTypes(rawType) }.firstOrNull { it != null }
          ?: TypeSet(Type(rawType.asTypeLocation(), rawType.convertTypeFields()))
      } catch (e: Exception) {
        throw Exception("failed to convert ${inType.typeName}", e)
      }
    converted[rawType] = result
    if (topLevel) primary = result.primary
  }

  /**
   * Converts a [JavaType] into a [FieldCategory]. Supports parameterized and non- parameterized
   * classes. Nested types will recursively call [convertToTypeInternal]. Types with type parameters
   * will recursively call [mapToFieldCategory] on the type parameters.
   */
  private fun JavaType.mapToFieldCategory(): FieldCategory {
    return when (val rawType = getRawType()) {
      String::class.java,
      String::class.javaObjectType -> FieldCategory.StringValue
      Byte::class.java,
      Byte::class.javaObjectType,
      Byte::class.javaPrimitiveType -> FieldCategory.ByteValue
      Short::class.java,
      Short::class.javaObjectType,
      Short::class.javaPrimitiveType -> FieldCategory.ShortValue
      Char::class.java,
      Char::class.javaObjectType,
      Char::class.javaPrimitiveType -> FieldCategory.CharValue
      Integer::class.java,
      Integer::class.javaObjectType,
      Integer::class.javaPrimitiveType -> FieldCategory.IntValue
      Long::class.java,
      Long::class.javaObjectType,
      Long::class.javaPrimitiveType -> FieldCategory.LongValue
      Float::class.java,
      Float::class.javaObjectType,
      Float::class.javaPrimitiveType -> FieldCategory.FloatValue
      Double::class.java,
      Double::class.javaObjectType,
      Double::class.javaPrimitiveType -> FieldCategory.DoubleValue
      Boolean::class.java,
      Boolean::class.javaObjectType,
      Boolean::class.javaPrimitiveType -> FieldCategory.BooleanValue
      List::class.java,
      List::class.javaObjectType ->
        FieldCategory.ListValue(LIST_TYPE_LOCATION, getParameterFieldCategory(0))
      Set::class.java,
      Set::class.javaObjectType ->
        FieldCategory.SetValue(SET_TYPE_LOCATION, getParameterFieldCategory(0))
      Map::class.java,
      Map::class.javaObjectType ->
        FieldCategory.MapValue(
          location = MAP_TYPE_LOCATION,
          keyType = getParameterFieldCategory(0),
          valueType = getParameterFieldCategory(1),
        )
      Instant::class.java,
      Instant::class.javaObjectType -> FieldCategory.InstantValue
      Duration::class.java,
      Duration::class.javaObjectType -> FieldCategory.DurationValue
      ByteArray::class.java -> FieldCategory.ByteArrayValue
      is Class<*> -> {
        if (rawType.isEnum) {
          FieldCategory.EnumValue(
            rawType.asTypeLocation(),
            rawType.enumConstants?.map { it.toString() }.orEmpty(),
          )
        } else {
          convertToTypeInternal(this)
          FieldCategory.NestedTypeValue(rawType.asTypeLocation())
        }
      }
      else -> throw FieldConversionException("Not convertible: $this")
    }
  }

  /**
   * Return the [rawType] if the type is a parameterized class, otheriwse just return the same type.
   */
  private fun JavaType.getRawType(): JavaType =
    when (this) {
      is ParameterizedType -> this.rawType
      else -> this
    }

  /**
   * Converts the [FieldDescriptor] to the appropriate [FieldEntry]. If there is a
   * [ForeignReference] configuration, this will represent this using
   * [FieldCategory.ForeignReference]. If the [additionalNamedField] value is non-empty, the method
   * will return two field entries: the original, non-foreign-reference entry, and the
   * foreign-reference entry, using the alternate name provided.
   */
  private fun Field.toFieldEntries(): List<FieldEntry> {
    val foreignReference = config.foreignReferences.firstOrNull { it.fieldName == name }
    val includeNormalField =
      foreignReference == null || foreignReference.additionalNamedField.isNotEmpty()

    val foreignReferenceFieldEntry =
      foreignReference?.let {
        FieldEntry(
          if (it.additionalNamedField.isEmpty()) name else it.additionalNamedField,
          FieldCategory.ForeignReference(foreignReference.schemaName, foreignReference.hard),
          name,
        )
      }

    return listOfNotNull(
      if (includeNormalField) FieldEntry(name, toFieldCategory()) else null,
      foreignReferenceFieldEntry,
    )
  }

  /** Return a [FieldCategory] based on the [genericType] of a [Field]. */
  private fun Field.toFieldCategory() = this.genericType.mapToFieldCategory()

  /** Convenience method to return the FieldCategory for a type parameter at [index]. */
  private fun JavaType.getParameterFieldCategory(index: Int): FieldCategory {
    return try {
      (this as ParameterizedType).actualTypeArguments[index].mapToFieldCategory()
    } catch (e: Exception) {
      throw FieldConversionException("Failed to get parameterized type for $typeName", e)
    }
  }

  /** An exception thrown when there was an underlying issue converting a type. */
  class FieldConversionException(msg: String, cause: Throwable? = null) : Exception(msg, cause)

  /** Holds a set of configuration options that can be passed [ClassToTypeConverter]. */
  data class Configuration(
    /** Any field of this type encountered during processing will not be included in the output. */
    val ignoredType: Set<Class<*>> = emptySet(),

    /** Any field of this name encountered during processing will not be included in the output. */
    val ignoredFieldNames: Set<String> = emptySet(),

    /** Enable support for generating nullable fields from oneOfs. */
    val enableNullableSupport: Boolean = false,

    /** Indicates that a field should be represented using a foreign reference. */
    val foreignReferences: Set<ForeignReference> = emptySet(),
  )

  companion object {
    private val LIST_TYPE_LOCATION = TypeLocation("List", pkg = "java.util")
    private val SET_TYPE_LOCATION = TypeLocation("Set", pkg = "java.util")
    private val MAP_TYPE_LOCATION = TypeLocation("Map", pkg = "java.util")
  }
}

/**
 * A helper to get a list of strings representing an enclosing types for a class.
 *
 * The enclosing types will be returned in list ordered innermost-first.
 *
 * For example, `java.util.Map.Entry` will return ["Map"].
 * `some.package.OuterMost.Middle.Inner.ClassName` will return ["Inner", "Middle", "OuterMost"]
 */
fun Class<*>.enclosingNames() =
  generateSequence(enclosingClass) { it.enclosingClass }.map { it.simpleName }.toList()
