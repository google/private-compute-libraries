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

package com.google.android.libraries.pcc.chronicle.api.integration

import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.common.truth.Truth.assertThat
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DefaultDataTypeDescriptorSetTest {
  @Test
  fun getOrNull_returnsDtdWhenFound() {
    val set = DefaultDataTypeDescriptorSet(setOf(NESTED_DTD, UNNESTED_DTD))
    assertThat(set.getOrNull(NESTED_DTD.name)).isEqualTo(NESTED_DTD)
    assertThat(set.getOrNull(INNER_DTD.name)).isEqualTo(INNER_DTD)
    assertThat(set.getOrNull(UNNESTED_DTD.name)).isEqualTo(UNNESTED_DTD)
  }

  @Test
  fun getOrNull_returnsNullWhenNotFound() {
    val set = DefaultDataTypeDescriptorSet(setOf(UNNESTED_DTD))
    assertThat(set.getOrNull(INNER_DTD.name)).isNull()
    assertThat(set.getOrNull("blahblah")).isNull()
  }

  @Test
  fun getOrNull_returnsDoublyNestedDtd() {
    val set = DefaultDataTypeDescriptorSet(setOf(DOUBLY_NESTED_DTD))
    assertThat(set.getOrNull(INNER_DTD.name)).isEqualTo(INNER_DTD)
  }

  @Test
  fun findFieldType_throwsWhenAccessPathIsEmpty() {
    val set = DefaultDataTypeDescriptorSet(setOf(NESTED_DTD, UNNESTED_DTD))
    val e =
      assertFailsWith<IllegalArgumentException> {
        set.findFieldTypeOrThrow(NESTED_DTD, emptyList())
      }

    assertThat(e).hasMessageThat().contains("Cannot find field type for empty access path.")
  }

  @Test
  fun findFieldType_throwsWhenFieldNotFound() {
    val set = DefaultDataTypeDescriptorSet(setOf(NESTED_DTD, UNNESTED_DTD))
    var e =
      assertFailsWith<IllegalArgumentException> {
        set.findFieldTypeOrThrow(NESTED_DTD, listOf("inner", "bar"))
      }

    assertThat(e).hasMessageThat().contains("Field \"bar\" not found in ${INNER_DTD.name}")

    e = assertFailsWith { set.findFieldTypeOrThrow(NESTED_DTD, listOf("something", "foo")) }

    assertThat(e).hasMessageThat().contains("Field \"something\" not found in ${NESTED_DTD.name}")
  }

  @Test
  fun findFieldType_happyPath() {
    val set = DefaultDataTypeDescriptorSet(setOf(NESTED_DTD, UNNESTED_DTD))

    assertThat(set.findFieldTypeOrThrow(NESTED_DTD, listOf("inner", "foo")))
      .isEqualTo(FieldType.Integer)
    assertThat(set.findFieldTypeOrThrow(NESTED_DTD, listOf("inner")))
      .isEqualTo(FieldType.Nested(INNER_DTD.name))
    assertThat(set.findFieldTypeOrThrow(UNNESTED_DTD, listOf("string"))).isEqualTo(FieldType.String)
  }

  @Test
  fun findDataTypeDescriptor_findsItemFieldTypeDescriptor_forArrayFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(FieldType.Array(FieldType.String))).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Array(FieldType.Nested(INNER_DTD.name))))
      .isEqualTo(INNER_DTD)
    assertThat(
        set.findDataTypeDescriptor(
          FieldType.Array(FieldType.Array(FieldType.Nested(INNER_DTD.name)))
        )
      )
      .isEqualTo(INNER_DTD)
  }

  @Test
  fun findDataTypeDescriptor_findsItemFieldTypeDescriptor_forListFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(FieldType.List(FieldType.String))).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.List(FieldType.Nested(INNER_DTD.name))))
      .isEqualTo(INNER_DTD)
    assertThat(
        set.findDataTypeDescriptor(FieldType.List(FieldType.List(FieldType.Nested(INNER_DTD.name))))
      )
      .isEqualTo(INNER_DTD)
  }

  @Test
  fun findDataTypeDescriptor_findsItemFieldTypeDescriptor_forNullableFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(FieldType.Nullable(FieldType.String))).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Nullable(FieldType.Nested(INNER_DTD.name))))
      .isEqualTo(INNER_DTD)
  }

  @Test
  fun findDataTypeDescriptor_findsDescriptorByName_forNestedFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(FieldType.Nested(INNER_DTD.name))).isEqualTo(INNER_DTD)
  }

  @Test
  fun findDataTypeDescriptor_findsDescriptorByName_forReferenceFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(FieldType.Reference(INNER_DTD.name))).isEqualTo(INNER_DTD)
  }

  @Test
  fun findDataTypeDescriptor_returnsNull_forPrimitiveFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(FieldType.Boolean)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Byte)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.ByteArray)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Char)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Double)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Duration)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Float)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Instant)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Long)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.Short)).isNull()
    assertThat(set.findDataTypeDescriptor(FieldType.String)).isNull()
  }

  @Test
  fun findDataTypeDescriptor_returnsNull_forOpaqueFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(FieldType.Opaque("java.io.InputStream"))).isNull()
  }

  @Test
  fun findDataTypeDescriptor_returnsNull_forTupleFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(
        set.findDataTypeDescriptor(FieldType.Tuple(listOf(FieldType.String, FieldType.Byte)))
      )
      .isNull()
  }

  @Test
  fun fieldTypeAsClass_returnsStdLibClass_forPrimitiveFieldTypes() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.fieldTypeAsClass(FieldType.Boolean)).isEqualTo(Boolean::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.Byte)).isEqualTo(Byte::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.ByteArray)).isEqualTo(ByteArray::class.java)
    assertThat(set.fieldTypeAsClass(FieldType.Char)).isEqualTo(Char::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.Double)).isEqualTo(Double::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.Duration)).isEqualTo(Duration::class.java)
    assertThat(set.fieldTypeAsClass(FieldType.Float)).isEqualTo(Float::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.Instant)).isEqualTo(Instant::class.java)
    assertThat(set.fieldTypeAsClass(FieldType.Integer)).isEqualTo(Int::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.Long)).isEqualTo(Long::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.Short)).isEqualTo(Short::class.javaObjectType)
    assertThat(set.fieldTypeAsClass(FieldType.String)).isEqualTo(String::class.java)
    assertThat(set.fieldTypeAsClass(FieldType.Array(FieldType.Char))).isEqualTo(Array::class.java)
    assertThat(set.fieldTypeAsClass(FieldType.List(FieldType.Char))).isEqualTo(List::class.java)
  }

  @Test
  fun fieldTypeAsClass_returnsDtdClass_forReferenceFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(NESTED_DTD))

    assertThat(set.fieldTypeAsClass(FieldType.Reference(INNER_DTD.name)))
      .isEqualTo(INNER_DTD.cls.java)
  }

  @Test
  fun fieldTypeAsClass_returnsDtdClass_forNestedFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(NESTED_DTD))

    assertThat(set.fieldTypeAsClass(FieldType.Nested(INNER_DTD.name))).isEqualTo(INNER_DTD.cls.java)
  }

  @Test
  fun fieldTypeAsClass_looksUpClass_forOpaqueFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(UNNESTED_DTD))

    assertThat(set.fieldTypeAsClass(FieldType.Opaque("java.io.InputStream")))
      .isEqualTo(InputStream::class.java)
  }

  @Test
  fun fieldTypeAsClass_looksUpItemFieldTypeClass_forNullableFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.fieldTypeAsClass(FieldType.Nullable(FieldType.String)))
      .isEqualTo(String::class.java)
    assertThat(set.fieldTypeAsClass(FieldType.Nullable(FieldType.Nested(INNER_DTD.name))))
      .isEqualTo(INNER_DTD.cls.java)
  }

  @Test
  fun fieldTypeAsClass_throws_forTupleFieldType() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    val e =
      assertFailsWith<IllegalArgumentException> {
        set.fieldTypeAsClass(FieldType.Tuple(listOf(FieldType.String, FieldType.Integer)))
      }

    assertThat(e).hasMessageThat().contains("Tuple is too ambiguous to return a field type.")
  }

  @Test
  fun findDataTypeDescriptor() {
    val set = DefaultDataTypeDescriptorSet(setOf(DOUBLY_NESTED_DTD))

    assertThat(set.findDataTypeDescriptor(DoublyNestedDtdClass::class)).isEqualTo(DOUBLY_NESTED_DTD)
    assertThat(set.findDataTypeDescriptor(NestedDtdClass::class))
      .isEqualTo(DOUBLY_NESTED_DTD.innerTypes.find { it.cls == NestedDtdClass::class })
    assertThat(set.findDataTypeDescriptor(InnerDtdClass::class)).isEqualTo(INNER_DTD)
  }

  @Test
  fun findDataTypeDescriptor_nonexistent() {
    val set = DefaultDataTypeDescriptorSet(setOf(INNER_DTD))

    assertThat(set.findDataTypeDescriptor(UnnestedDtdClass::class)).isNull()
  }

  @Test
  fun toSet_returnsSetOfDtds() {
    val expected = setOf(INNER_DTD, DOUBLY_NESTED_DTD, NESTED_DTD, UNNESTED_DTD)
    val dtdSet = DefaultDataTypeDescriptorSet(expected)

    assertThat(dtdSet.toSet()).containsExactlyElementsIn(expected)
  }

  // Fields aren't needed for these, we only care about the DTDs.
  class InnerDtdClass
  class NestedDtdClass
  class DoublyNestedDtdClass
  class UnnestedDtdClass
  class OpaqueClass

  companion object {
    private val INNER_DTD =
      dataTypeDescriptor("com.google.Inner", InnerDtdClass::class) { "foo" to FieldType.Integer }

    private val NESTED_DTD =
      dataTypeDescriptor("com.google.Nested", NestedDtdClass::class) {
        "inner" to
          dataTypeDescriptor("com.google.Inner", InnerDtdClass::class) {
            "foo" to FieldType.Integer
          }
      }

    private val DOUBLY_NESTED_DTD =
      dataTypeDescriptor("com.google.DoublyNested", DoublyNestedDtdClass::class) {
        "outermost" to
          dataTypeDescriptor("com.google.Nested", NestedDtdClass::class) {
            "outer" to
              dataTypeDescriptor("com.google.Inner", InnerDtdClass::class) {
                "foo" to FieldType.Integer
              }
          }
      }

    private val UNNESTED_DTD =
      dataTypeDescriptor("com.google.Unnested", UnnestedDtdClass::class) {
        "array" to FieldType.Array(FieldType.String)
        "twoDArray" to FieldType.Array(FieldType.Array(FieldType.String))
        "list" to FieldType.List(FieldType.String)
        "twoDList" to FieldType.List(FieldType.List(FieldType.String))
        "nullable" to FieldType.Nullable(FieldType.String)
        "boolean" to FieldType.Boolean
        "byte" to FieldType.Byte
        "byteArray" to FieldType.ByteArray
        "char" to FieldType.Char
        "double" to FieldType.Double
        "duration" to FieldType.Duration
        "float" to FieldType.Float
        "instant" to FieldType.Instant
        "integer" to FieldType.Integer
        "long" to FieldType.Long
        "short" to FieldType.Short
        "string" to FieldType.String
        "opaque" to FieldType.Opaque(OpaqueClass::class.java.name)
        "tuple" to FieldType.Tuple(listOf(FieldType.String, FieldType.Integer))
      }
  }
}
