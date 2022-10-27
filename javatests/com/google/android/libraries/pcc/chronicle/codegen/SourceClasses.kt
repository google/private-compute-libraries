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

package com.google.android.libraries.pcc.chronicle.codegen

import com.google.android.libraries.pcc.chronicle.codegen.SourceClasses.ThingEnum.UNKNOWN
import java.time.Duration
import java.time.Instant

/**
 * Helper to read in a test data file in this package.
 *
 * Assumes that test data is in a `testdata` directory at the same level as the class calling this
 * method, and that the test data it contains is organized into groups as subdirectories of that
 * directory.
 *
 * @param group The top-level subdirectory of the `testdata` directory.
 * @param file The name of the file in the provided `group` directory with the desired data.
 */
fun readTestData(group: String, file: String) =
    java.io.File(
      System.getProperty("user.dir"),
      "javatests/com/google/android/libraries/pcc/chronicle/codegen/testdata/$group/$file"
    )
    .readText(Charsets.UTF_8)

data class SimpleUnenclosedType(val field1: String, val field2: Int)

class SourceClasses {
  enum class ThingEnum {
    UNKNOWN,
    FIRST,
    SECOND
  }

  class Thing {
    var field1: String = ""
    var field2: Byte = 0
    var field3: Short = 0
    var field4: Char = 'a'
    var field5 = 0
    var field6: Long = 0
    var field7 = 0f
    var field8 = 0.0
    var field9 = false
    var field10: List<String> = emptyList()
    var field11: Set<Int> = emptySet()
    var field12: Map<String, Boolean> = emptyMap()
    var field13: ThingEnum = UNKNOWN
    var field14: Instant = Instant.ofEpochMilli(123456)
    var field15: Duration = Duration.ofMillis(123456)
    // [field16] and [field12] have the same map type. This tests that the map handling code does
    // not cause collisions when repeated.
    var field16: Map<String, Boolean> = emptyMap()
    var field17: ByteArray = byteArrayOf()
  }

  class NestedThing {
    var field1: List<String> = emptyList()
    var field2: SimpleThing = SimpleThing()
  }

  data class NestedType(val field1: InnerType) {
    data class InnerType(
      val field1: String,
      val field2: InnerInnerType,
    ) {
      data class InnerInnerType(val field1: String)
    }
  }

  data class SimpleThing(val field1: String = "")

  class Thing1 {
    var field1: Thing2 = Thing2()
  }

  class Thing2 {
    var field1: Thing3 = Thing3()
  }

  class Thing3 {
    var field1: String = ""
  }

  data class ListOfEntity(val listOfEntity: List<SimpleThing>)

  data class SetOfEntity(val setOfEntity: Set<SimpleThing>)

  data class ListOfListOfEntity(val listOfListOfEntity: List<List<SimpleThing>>)

  data class MapOfEntity(val mapOfEntity: Map<String, SimpleThing>)

  data class MapOfListOfEntity(val mapOfListOfEntity: Map<String, List<SimpleThing>>)

  data class MapWithEnumKey(val mapWithEnumKey: Map<ThingEnum, String>)

  data class RecursiveRefA(val other: RecursiveRefB)

  data class RecursiveRefB(val other: RecursiveRefA)

  data class RecursiveListRefA(val others: List<RecursiveListRefB>)

  data class RecursiveListRefB(val others: List<RecursiveListRefA>)

  data class RecursiveMapRefA(val others: Map<String, RecursiveMapRefB>)

  data class RecursiveMapRefB(val others: Map<String, RecursiveMapRefA>)

  abstract class BaseType(open val baseValue: String) {
    abstract val name: String
    abstract val isBaseFlag: Boolean
  }

  data class SubType(
    override val name: String,
    override val baseValue: String,
    override val isBaseFlag: Boolean,
    val subField: String,
    val isFlag: Boolean,
  ) : BaseType(baseValue)
}
