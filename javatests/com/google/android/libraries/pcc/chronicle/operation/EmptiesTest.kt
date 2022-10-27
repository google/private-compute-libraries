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

package com.google.android.libraries.pcc.chronicle.operation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.operation.DefaultOperationLibrary
import com.google.android.libraries.pcc.chronicle.api.operation.OperationLibrary
import com.google.android.libraries.pcc.chronicle.api.operation.findOperation
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmptiesTest {
  private val operations: OperationLibrary = DefaultOperationLibrary(Empties.provideOperations())

  @Test
  fun nullable_emptied() {
    val result = Empties.NULLABLE(42 to 1337)

    result as Action.Update<Pair<Int, Int>>

    assertThat(result.newValue).isNull()
  }

  @Test
  fun nullable_injected() {
    val found = operations.findOperation<Pair<*, *>?>("EMPTY")
    assertThat(found).isSameInstanceAs(Empties.NULLABLE)
  }

  @Test
  fun string_emptied() {
    val result = Empties.STRING("This is my input")

    result as Action.Update

    assertThat(result.newValue).isEmpty()
  }

  @Test
  fun string_injected() {
    val found = operations.findOperation<String>("EMPTY")
    assertThat(found).isSameInstanceAs(Empties.STRING)
  }

  @Test
  fun int_emptied() {
    val result = Empties.INT(42)

    result as Action.Update

    assertThat(result.newValue).isEqualTo(0)
  }

  @Test
  fun int_injected() {
    val found = operations.findOperation<Int>("EMPTY")
    assertThat(found).isSameInstanceAs(Empties.INT)
  }

  @Test
  fun long_emptied() {
    val result = Empties.LONG(42L)

    result as Action.Update

    assertThat(result.newValue).isEqualTo(0L)
  }

  @Test
  fun long_injected() {
    val found = operations.findOperation<Long>("EMPTY")
    assertThat(found).isSameInstanceAs(Empties.LONG)
  }

  @Test
  fun float_emptied() {
    val result = Empties.FLOAT(42f)

    result as Action.Update

    assertThat(result.newValue).isEqualTo(0f)
  }

  @Test
  fun float_injected() {
    val found = operations.findOperation<Float>("EMPTY")
    assertThat(found).isSameInstanceAs(Empties.FLOAT)
  }

  @Test
  fun double_emptied() {
    val result = Empties.DOUBLE(42.0)

    result as Action.Update

    assertThat(result.newValue).isEqualTo(0.0)
  }

  @Test
  fun double_injected() {
    val found = operations.findOperation<Double>("EMPTY")
    assertThat(found).isSameInstanceAs(Empties.DOUBLE)
  }

  @Test
  fun boolean_emptied() {
    val result = Empties.BOOLEAN(true)

    result as Action.Update

    assertThat(result.newValue).isEqualTo(false)
  }

  @Test
  fun boolean_injected() {
    val found = operations.findOperation<Boolean>("EMPTY")
    assertThat(found).isSameInstanceAs(Empties.BOOLEAN)
  }
}
