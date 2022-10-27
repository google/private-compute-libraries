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

package com.google.android.libraries.pcc.chronicle.codegen.frontend.element

import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory
import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.util.ProcessingEnvHelpers
import com.google.android.libraries.pcc.chronicle.codegen.util.processingEnvHelpers
import java.util.LinkedList
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

/**
 * Generate a set of Arcs Chronicle types needed for code generation based on type information
 * available at annotation processing time.
 */
class ElementToTypeConverter(
  override val processingEnv: ProcessingEnvironment,
  val fieldEnumerators: List<FieldEnumerator> =
    listOf(AutoValueFieldEnumerator(processingEnv), DataClassFieldEnumerator(processingEnv))
) : ProcessingEnvHelpers by processingEnvHelpers(processingEnv) {
  fun convertElement(initialElement: Element): TypeSet {
    var primary: Type? = null
    val types = mutableSetOf<Type>()

    // Do a breadth-first traversal of the nested fields.
    val visited = mutableSetOf(initialElement)
    val queue = LinkedList<Element>().apply { push(initialElement) }
    while (queue.isNotEmpty()) {
      val current = queue.poll() ?: break

      // Convert the current element to a type, then update the primary and types variables.
      val (type, nested) = convertElementInternal(current)
      primary = primary ?: type
      types += type

      // For each nested type within the type, update our queue and visited tracking variables.
      nested.forEach {
        if (it in visited) return@forEach
        visited.add(it)
        queue.push(it)
      }
    }

    return TypeSet(
      primary = requireNotNull(primary) { "No type found" },
      additional = types - primary
    )
  }

  /**
   * Converts the [inType] into its [Type] representation, and returns that along with a set of
   * nested [Elements] which will need to be converted to [Types][Type] as well.
   */
  private fun convertElementInternal(inType: Element): Pair<Type, Set<Element>> {
    val location = inType.asTypeLocation()
    val (fieldTypes, nestedElements) = inType.convertTypeFields()
    // TODO(b/202333655): Determine if the element is AutoValue or a data class, and set the
    //  tooling property accordingly.
    val type = Type(location, fieldTypes)
    return type to nestedElements
  }

  /**
   * Use the available [FieldEnumerators][FieldEnumerator] in order until one of them returns a list
   * of [FieldDecls][FieldDecl], and then convert that list into [FieldEntry] items.
   *
   * The returned [Pair] contains those [FieldEntry] items as well as a set of [Elements]
   * representing nested types that will need to be converted as well.
   *
   * If none of the [FieldEnumerators][FieldEnumerator] returns a non-null result, an empty list
   * will be returned.
   */
  private fun Element.convertTypeFields(): Pair<List<FieldEntry>, Set<Element>> {
    val types =
      fieldEnumerators.asSequence().mapNotNull { it.fieldsInType(this) }.firstOrNull()
        ?: emptyList()

    val fields = mutableListOf<FieldEntry>()
    val nestedTypes = mutableSetOf<Element>()
    types.forEach {
      val (entry, nested) = it.toFieldEntry()
      fields += entry
      nestedTypes += nested
    }
    return fields to nestedTypes
  }

  /** Create a [FieldEntry] for the [Element] and return any nested types along with it. */
  private fun FieldDecl.toFieldEntry(): Pair<FieldEntry, Set<Element>> {
    val (category, nested) = type.toFieldCategory()
    return FieldEntry(name, category) to nested
  }

  private data class FieldCategoryResult(
    val category: FieldCategory,
    val nestedElementTypes: Set<Element>
  )

  private fun FieldCategory.toResult(nestedTypes: Set<Element> = emptySet()): FieldCategoryResult =
    FieldCategoryResult(this, nestedTypes)

  /**
   * Return the [FieldCategory] that the provided [TypeMirror] should map to and any nested types
   * (as [Elements][Element]) as a [FieldCategoryResult].
   *
   * If the [TypeKind] is [TypeKind.DECLARED], some subsequent digging needs to happen, that's moved
   * to a separate [DeclaredType.toFieldCategory] helper below.
   */
  private fun TypeMirror.toFieldCategory(): FieldCategoryResult {
    return when (this.kind) {
      TypeKind.BOOLEAN -> FieldCategory.BooleanValue.toResult()
      TypeKind.BYTE -> FieldCategory.ByteValue.toResult()
      TypeKind.CHAR -> FieldCategory.CharValue.toResult()
      TypeKind.DOUBLE -> FieldCategory.DoubleValue.toResult()
      TypeKind.FLOAT -> FieldCategory.FloatValue.toResult()
      TypeKind.INT -> FieldCategory.IntValue.toResult()
      TypeKind.LONG -> FieldCategory.LongValue.toResult()
      TypeKind.SHORT -> FieldCategory.ShortValue.toResult()
      TypeKind.DECLARED -> (this as DeclaredType).toFieldCategory()
      TypeKind.ARRAY -> {
        if ((this as ArrayType).componentType.kind == TypeKind.BYTE) {
          FieldCategory.ByteArrayValue.toResult()
        } else {
          throw ElementConverterException(
            "Unable to convert type kind $kind of ${componentType.kind} to FieldCategory"
          )
        }
      }
      else ->
        throw ElementConverterException("Unable to convert type kind ${this.kind} to FieldCategory")
    }
  }

  /**
   * Return the [FieldCategory] that best represents a [TypeMirror] of [TypeKind.DECLARED] along
   * with any nested types discovered while examining the [DeclaredType] as a [FieldCategoryResult].
   *
   * This will handle boxed primitives, enums, collections, and nested types.
   */
  private fun DeclaredType.toFieldCategory(): FieldCategoryResult {
    val rawType = this.rawType()
    return when (rawType.comparableType()) {
      booleanType -> FieldCategory.BooleanValue.toResult()
      byteType -> FieldCategory.ByteValue.toResult()
      charType -> FieldCategory.CharValue.toResult()
      doubleType -> FieldCategory.DoubleValue.toResult()
      floatType -> FieldCategory.FloatValue.toResult()
      integerType -> FieldCategory.IntValue.toResult()
      longType -> FieldCategory.LongValue.toResult()
      shortType -> FieldCategory.ShortValue.toResult()
      stringType -> FieldCategory.StringValue.toResult()
      instantType -> FieldCategory.InstantValue.toResult()
      durationType -> FieldCategory.DurationValue.toResult()
      immutableListType, listType -> {
        val (nestedTypeCategory, nestedTypeNestedTypes) = typeParameter(0).toFieldCategory()
        FieldCategory.ListValue(rawType.asTypeLocation(), nestedTypeCategory)
          .toResult(nestedTypeNestedTypes)
      }
      immutableSetType, setType -> {
        val (nestedTypeCategory, nestedTypeNestedTypes) = typeParameter(0).toFieldCategory()
        FieldCategory.SetValue(rawType.asTypeLocation(), nestedTypeCategory)
          .toResult(nestedTypeNestedTypes)
      }
      immutableMapType, mapType -> {
        val (keyCategory, keyNestedTypes) = typeParameter(0).toFieldCategory()
        val (valueCategory, valueNestedTypes) = typeParameter(1).toFieldCategory()
        FieldCategory.MapValue(rawType.asTypeLocation(), keyCategory, valueCategory)
          .toResult(keyNestedTypes + valueNestedTypes)
      }
      optionalType -> {
        val (category, nestedTypes) = typeParameter(0).toFieldCategory()
        FieldCategory.NullableValue(category).toResult(nestedTypes)
      }
      in pairTypes -> {
        val (firstCategory, firstNestedTypes) = typeParameter(0).toFieldCategory()
        val (secondCategory, secondNestedTypes) = typeParameter(1).toFieldCategory()
        FieldCategory.TupleValue(listOf(firstCategory, secondCategory))
          .toResult(firstNestedTypes + secondNestedTypes)
      }
      tripleType -> {
        val (firstCategory, firstNestedTypes) = typeParameter(0).toFieldCategory()
        val (secondCategory, secondNestedTypes) = typeParameter(1).toFieldCategory()
        val (thirdCategory, thirdNestedTypes) = typeParameter(2).toFieldCategory()
        FieldCategory.TupleValue(listOf(firstCategory, secondCategory, thirdCategory))
          .toResult(firstNestedTypes + secondNestedTypes + thirdNestedTypes)
      }
      in opaqueTypes -> FieldCategory.OpaqueTypeValue(asElement().asTypeLocation()).toResult()
      else -> {
        // Otherwise, we assume it's a nested class. For enums, we can use the enum category,
        // otherwise we can just return a nested type.
        val element = this.asElement()
        when (element.kind) {
          ElementKind.ENUM -> element.toEnumFieldCategory().toResult()
          else -> FieldCategory.NestedTypeValue(element.asTypeLocation()).toResult(setOf(element))
        }
      }
    }
  }

  /**
   * Simple helper to create a [FieldCategory.EnumValue] from an [Element]. Doesn't do any checking
   * to validate that the provided [Element] is actually an enum.
   */
  private fun Element.toEnumFieldCategory() =
    FieldCategory.EnumValue(asTypeLocation(), enumValues())

  /** Declared types that we know how to handle without nesting structures. */
  private val booleanType = "java.lang.Boolean".asNamedRawType()
  private val byteType = "java.lang.Byte".asNamedRawType()
  private val charType = "java.lang.Character".asNamedRawType()
  private val doubleType = "java.lang.Double".asNamedRawType()
  private val floatType = "java.lang.Float".asNamedRawType()
  private val integerType = "java.lang.Integer".asNamedRawType()
  private val longType = "java.lang.Long".asNamedRawType()
  private val shortType = "java.lang.Short".asNamedRawType()
  private val stringType = "java.lang.String".asNamedRawType()
  private val listType = "java.util.List".asNamedRawType()
  private val setType = "java.util.Set".asNamedRawType()
  private val mapType = "java.util.Map".asNamedRawType()
  private val instantType = "java.time.Instant".asNamedRawType()
  private val durationType = "java.time.Duration".asNamedRawType()

  private val immutableSetType = "com.google.common.collect.ImmutableSet".asNamedRawTypeOrNull()
  private val immutableListType = "com.google.common.collect.ImmutableList".asNamedRawTypeOrNull()
  private val immutableMapType = "com.google.common.collect.ImmutableMap".asNamedRawTypeOrNull()
  private val optionalType = "java.util.Optional".asNamedRawType()

  private val kotlinPairType = "kotlin.Pair".asNamedRawType()
  private val androidPairType = "android.util.Pair".asNamedRawTypeOrNull()
  private val pairTypes = listOf(kotlinPairType, androidPairType).filterNotNull()

  private val tripleType = "kotlin.Triple".asNamedRawType()

  // Some types, like [IBinder], and [ActivityId], may not be found if the target is not being built
  // for Android.
  private val iBinderType = "android.os.IBinder".asNamedRawTypeOrNull()
  private val activityIdType = "android.app.assist.ActivityId".asNamedRawTypeOrNull()

  // All opaque types are converted to [FieldCategory.OpaqueValueType].
  private val opaqueTypes = listOf(iBinderType, activityIdType).filterNotNull()

  class ElementConverterException(msg: String) : UnsupportedOperationException(msg)
}
