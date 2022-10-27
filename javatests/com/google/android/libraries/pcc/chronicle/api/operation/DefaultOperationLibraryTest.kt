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

package com.google.android.libraries.pcc.chronicle.api.operation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DefaultOperationLibraryTest {
  @Test
  fun findOperation_unique() {
    val op = Operation.create<String>("blah") { Action.OmitFromParent }
    val library = DefaultOperationLibrary(setOf(op))

    assertThat(library.findOperation("blah", String::class.java, String::class.java))
      .isSameInstanceAs(op)
  }

  @Test
  fun findOperation_assignable() {
    val op = Operation.createNonUpdating<Any>("blah") { Action.OmitFromRoot }
    val library = DefaultOperationLibrary(setOf(op))

    assertThat(library.findOperation("blah", String::class.java, String::class.java))
      .isSameInstanceAs(op)
    assertThat(library.findOperation("blah", Pair::class.java, Pair::class.java))
      .isSameInstanceAs(op)
  }

  @Test
  fun findOperation_mostAppropriate() {
    val stringOp = Operation.createNonUpdating<String>("blah") { Action.OmitFromParent }
    val objectOp = Operation.createNonUpdating<Any>("blah") { Action.OmitFromParent }
    val intOp = Operation.createNonUpdating<Int>("blah") { Action.OmitFromParent }
    val longOp = Operation.createNonUpdating<Long>("blah") { Action.OmitFromParent }
    val library = DefaultOperationLibrary(setOf(longOp, stringOp, objectOp, intOp))

    assertThat(library.findOperation("blah", Int::class.javaObjectType, Int::class.javaObjectType))
      .isEqualTo(intOp)
    assertThat(
        library.findOperation("blah", Long::class.javaObjectType, Long::class.javaObjectType)
      )
      .isEqualTo(longOp)
    assertThat(library.findOperation("blah", String::class.java, String::class.java))
      .isEqualTo(stringOp)
    assertThat(library.findOperation("blah", Pair::class.java, Pair::class.java))
      .isEqualTo(objectOp)
    assertThat(library.findOperation("blah", Any::class.java, Any::class.java)).isEqualTo(objectOp)
  }

  @Test
  fun findOperation_mostAppropriate_withHelper() {
    val stringOp = Operation.create<String>("blah") { Action.OmitFromParent }
    val objectOp = Operation.create<Any>("blah") { Action.OmitFromParent }
    val omittingObject = Operation.createNonUpdating<Any>("blah") { Action.OmitFromParent }
    val intOp = Operation.create<Int>("blah") { Action.OmitFromParent }
    val longOp = Operation.create<Long>("blah") { Action.OmitFromParent }
    val library = DefaultOperationLibrary(setOf(longOp, stringOp, objectOp, omittingObject, intOp))

    assertThat(library.findOperation<Int>("blah")).isEqualTo(intOp)
    assertThat(library.findOperation<Long>("blah")).isEqualTo(longOp)
    assertThat(library.findOperation<String>("blah")).isEqualTo(stringOp)
    assertThat(library.findOperation<Pair<*, *>>("blah")).isEqualTo(omittingObject)
    assertThat(library.findOperation<Any>("blah")).isEqualTo(objectOp)
  }
}
