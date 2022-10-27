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

import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.operation.Operation

/** Provides a few operations for testing purposes only. */
object TestOps {
  val REVERSE_STRING: Operation<String, String> =
    Operation.create("Reversed") { Action.Update(it.reversed()) }

  val REVERSE_INT: Operation<Int, Int> =
    Operation.create("Reversed") { Action.Update(it.toString(10).reversed().toInt(10)) }

  val REVERSE_LONG: Operation<Long, Long> =
    Operation.create("Reversed") { Action.Update(it.toString(10).reversed().toLong(10)) }

  /**
   * Provides a set of the test operations which can be included into a collection when constructing
   * an [OperationLibrary].
   */
  fun provideOperations(): Set<Operation<*, *>> = setOf(REVERSE_STRING, REVERSE_INT, REVERSE_LONG)
}
