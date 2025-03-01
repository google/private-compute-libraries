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

/**
 * Provides operations which always return an [Action.Update] of the "empty" value for their
 * corresponding types.
 *
 * Emptying operations can be used to perform a limited type of "type slicing", or removal of fields
 * unnecessary to the user of the data.
 *
 * The following are the empty values supported:
 * * `Any?` -> `null`
 * * `String` -> `""`
 * * `Int` -> `0`
 * * `Long` -> `0L`
 * * `Float` -> `0.0f`
 * * `Double` -> `0.0`
 * * `Boolean` -> `false`
 */
object Empties {
  /**
   * Provides a set of all emptying-operations which can be included into a collection when
   * constructing an [OperationLibrary].
   */
  fun provideOperations(): Set<Operation<*, *>> =
    setOf(NULLABLE, STRING, INT, LONG, FLOAT, DOUBLE, BOOLEAN)

  private const val OP_NAME = "EMPTY"

  internal val NULLABLE =
    object : Operation<Any?, Nothing>(OP_NAME, Any::class.java, Nothing::class.java) {
      @Suppress("UNCHECKED_CAST") // It's null, so it's fine.
      override fun invoke(value: Any?): Action<out Nothing> =
        Action.Update(null) as Action<out Nothing>
    }
  internal val STRING = emptyOp(Action.Update(""))
  internal val INT = emptyOp(Action.Update(0))
  internal val LONG = emptyOp(Action.Update(0L))
  internal val FLOAT = emptyOp(Action.Update(0f))
  internal val DOUBLE = emptyOp(Action.Update(0.0))
  internal val BOOLEAN = emptyOp(Action.Update(false))

  private inline fun <reified T> emptyOp(value: Action.Update<T>): Operation<T, T> =
    Operation.create(OP_NAME) { value }
}
