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

package com.google.android.libraries.pcc.chronicle.codegen.backend

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeLocation
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.typeconversion.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asClassName

/**
 * Generate a Chronicle [DataTypeDescriptor] using the provided set of types.
 *
 * If the `forSubType` parameter is provided, the types for the [Type] matching the specified
 * location will be generated. Otherwise, the first type encountered with `isTopLevelType` set to
 * true will be selected.
 *
 * The set of [Types][Type] should include all types needed to represent structured type fields.
 * This method will call itself recursively to generate inner types.
 */
fun TypeSet.dataTypeDescriptor(
  forSubType: TypeLocation? = null,
  outerTypes: MutableSet<TypeLocation> = mutableSetOf()
): CodeBlock {
  val typeToGenerate =
    forSubType?.let { subtype -> this.firstOrNull { it.location == subtype } } ?: primary
  val typeToGenerateLocation = typeToGenerate.location
  val typeToGenerateJvmLocation = typeToGenerate.jvmLocation
  // If we encounter a type already processed in the outer layers, just refer to its location in a
  // Reference type.
  if (typeToGenerateLocation in outerTypes) {
    return CodeBlock.builder()
      .add("%S", typeToGenerateLocation.toString())
      .build()
      .wrapIn(FieldType.Reference::class.asClassName())
  }

  val codeBuilder =
    CodeBlock.builder()
      .add(
        """
    %T(name = %S, cls = %T::class) {
    ⇥
    """.trimIndent(),
        dataTypeConstructorName,
        typeToGenerateLocation.toString(),
        typeToGenerateJvmLocation.asClassName()
      )

  outerTypes.add(typeToGenerateLocation)
  typeToGenerate.fields.forEach {
    codeBuilder.add("%S to %L\n", it.name, generateCodeForField(it.category, outerTypes))
  }
  outerTypes.remove(typeToGenerateLocation)

  return codeBuilder.add("⇤}").build()
}

private fun TypeSet.mapEntryDtd(
  keyType: FieldCategory,
  valueType: FieldCategory,
  outerTypes: MutableSet<TypeLocation> = mutableSetOf()
): CodeBlock {
  // Though simpleName loses some information (e.g. Set<String> has simpleName SetValue), the
  // scoping of the DTD definition prevents collision between between map DTDs with matching
  // simpleName parameters.
  val mapDTDName = "Map${keyType::class.java.simpleName}To${valueType::class.java.simpleName}"

  val codeBuilder =
    CodeBlock.builder()
      .add(
        """
        dataTypeDescriptor(name = %S, cls = %T::class) {
        ⇥
  """.trimIndent(),
        mapDTDName,
        Map.Entry::class.asClassName()
      )

  codeBuilder.add("\"key\" to %L\n", generateCodeForField(keyType, outerTypes))
  codeBuilder.add("\"value\" to %L\n", generateCodeForField(valueType, outerTypes))
  codeBuilder.add("⇤}")

  return codeBuilder.build()
}

private fun TypeSet.tuple(
  types: List<FieldCategory>,
  outerTypes: MutableSet<TypeLocation> = mutableSetOf()
): CodeBlock {
  val codeBuilder = CodeBlock.builder().add("listOf(")
  types.forEach { codeBuilder.add("%L,", generateCodeForField(it, outerTypes)) }
  codeBuilder.add(")")

  return codeBuilder.build().wrapIn(FieldType.Tuple::class.asClassName())
}

// Generate the proper DTD FieldType for a [FieldCategory]. This may involve recursive calls to
// dataTypeDescriptor or this method.
private fun TypeSet.generateCodeForField(
  fieldCategory: FieldCategory,
  outerTypes: MutableSet<TypeLocation> = mutableSetOf()
): CodeBlock {
  return when (fieldCategory) {
    // For a list or set or optional, the type of interest is the type parameter and List as the
    // container.
    is FieldCategory.ListValue ->
      generateCodeForField(fieldCategory.listType, outerTypes)
        .wrapIn(FieldType.List::class.asClassName())
    is FieldCategory.SetValue ->
      generateCodeForField(fieldCategory.setType, outerTypes)
        .wrapIn(FieldType.List::class.asClassName())
    is FieldCategory.NullableValue ->
      generateCodeForField(fieldCategory.innerType, outerTypes)
        .wrapIn(FieldType.Nullable::class.asClassName())

    // For a map, define a list of an inner DTD that captures the key and value parameters.
    is FieldCategory.MapValue ->
      mapEntryDtd(fieldCategory.keyType, fieldCategory.valueType, outerTypes)
        .wrapIn(FieldType.List::class.asClassName())

    // For an enum, wrap a list of possible values, in [FieldType.Enum].
    is FieldCategory.EnumValue -> {
      val codeBuilder =
        CodeBlock.builder().add("%S, ", fieldCategory.location.toString()).add("listOf(")
      fieldCategory.possibleValues.forEachIndexed { idx, enumValue ->
        codeBuilder.add("%S", enumValue)
        if (idx < fieldCategory.possibleValues.size - 1) codeBuilder.add(", ")
      }
      codeBuilder.add(")")
      codeBuilder.build().wrapIn(FieldType.Enum::class.asClassName())
    }

    // For a nested type, recursively generate the inner DTD.
    is FieldCategory.NestedTypeValue -> dataTypeDescriptor(fieldCategory.location, outerTypes)

    // For an opaque type, wrap the fully qualified typename in a [FieldType.Opaque].
    is FieldCategory.OpaqueTypeValue ->
      CodeBlock.of("%S", fieldCategory.location.toString())
        .wrapIn(FieldType.Opaque::class.asClassName())

    // For pairs, wrap the list of types in a FieldType.Tuple.
    is FieldCategory.TupleValue -> tuple(fieldCategory.types, outerTypes)

    // For FieldType supported primitives, use the corresponding field type.
    FieldCategory.BooleanValue -> CodeBlock.of("%T", FieldType.Boolean::class.asClassName())
    FieldCategory.ByteValue -> CodeBlock.of("%T", FieldType.Byte::class.asClassName())
    FieldCategory.ByteArrayValue -> CodeBlock.of("%T", FieldType.ByteArray::class.asClassName())
    FieldCategory.CharValue -> CodeBlock.of("%T", FieldType.Char::class.asClassName())
    FieldCategory.DoubleValue -> CodeBlock.of("%T", FieldType.Double::class.asClassName())
    FieldCategory.FloatValue -> CodeBlock.of("%T", FieldType.Float::class.asClassName())
    FieldCategory.IntValue -> CodeBlock.of("%T", FieldType.Integer::class.asClassName())
    FieldCategory.LongValue -> CodeBlock.of("%T", FieldType.Long::class.asClassName())
    FieldCategory.ShortValue -> CodeBlock.of("%T", FieldType.Short::class.asClassName())
    FieldCategory.StringValue -> CodeBlock.of("%T", FieldType.String::class.asClassName())
    FieldCategory.InstantValue -> CodeBlock.of("%T", FieldType.Instant::class.asClassName())
    FieldCategory.DurationValue -> CodeBlock.of("%T", FieldType.Duration::class.asClassName())

    // Everything else will be considered a ByteArray.
    else -> CodeBlock.of("%T", FieldType.ByteArray::class.asClassName())
  }
}

// Get the fully qualified name of the `dataType` constructor method.
// I couldn't find a dynamic approach to getting the fully qualified name of the method reference,
// but this does the trick for now.
private val dataTypeConstructorName =
  ClassName("com.google.android.libraries.pcc.chronicle.api", "dataTypeDescriptor")

/** Wraps the receiving [CodeBlock] in a constructor for the given [wrappingClassName]. */
private fun CodeBlock.wrapIn(wrappingClassName: ClassName): CodeBlock {
  return CodeBlock.builder().add("%T(", wrappingClassName).add(this).add(")").build()
}
