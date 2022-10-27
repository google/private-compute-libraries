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

import com.google.android.libraries.pcc.chronicle.codegen.ExpectedTypes
import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.google.android.libraries.pcc.chronicle.codegen.TypeSet
import com.google.android.libraries.pcc.chronicle.codegen.testutil.withForeignReference
import com.google.android.libraries.pcc.chronicle.codegen.testutil.withoutFields
import com.google.common.truth.Truth.assertThat
import kotlin.reflect.KClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ClassToTypeConverterConfigTest {

  @Test
  fun ignoreFieldNames() {
    classToTypeConverterConfigTest(
      ClassToTypeConverter.Configuration(ignoredFieldNames = setOf("field1", "field7")),
      SourceClasses.Thing::class,
      ExpectedTypes.thing.withoutFields("field1", "field7")
    )
  }

  @Test
  fun ignoreSimpleTypes() {
    classToTypeConverterConfigTest(
      ClassToTypeConverter.Configuration(ignoredType = setOf(String::class.java, Long::class.java)),
      SourceClasses.Thing::class,
      ExpectedTypes.thing.withoutFields("field1", "field6")
    )
  }

  @Test
  fun ignoreNestedFieldTypes() {
    classToTypeConverterConfigTest(
      ClassToTypeConverter.Configuration(
        ignoredType = setOf(SourceClasses.SimpleThing::class.java)
      ),
      SourceClasses.NestedThing::class,
      ExpectedTypes.nestedThing.withoutFields("field2")
    )
  }

  @Test
  fun foreignReference() {
    classToTypeConverterConfigTest(
      ClassToTypeConverter.Configuration(
        foreignReferences = setOf(ForeignReference("field1", "PackageName", true, ""))
      ),
      SourceClasses.Thing::class,
      ExpectedTypes.thing.withForeignReference("field1", "PackageName", true)
    )
  }

  @Test
  fun foreignReference_additional() {
    classToTypeConverterConfigTest(
      ClassToTypeConverter.Configuration(
        foreignReferences = setOf(ForeignReference("field1", "PackageName", true, "otherName"))
      ),
      SourceClasses.Thing::class,
      ExpectedTypes.thing.withForeignReference("field1", "PackageName", true, "otherName")
    )
  }

  private fun classToTypeConverterConfigTest(
    config: ClassToTypeConverter.Configuration,
    cls: KClass<*>,
    vararg expect: Type
  ) {
    val converter = ClassToTypeConverter(config)
    assertThat(converter.convertToTypes(cls.java)).isEqualTo(TypeSet(*expect))
  }
}
