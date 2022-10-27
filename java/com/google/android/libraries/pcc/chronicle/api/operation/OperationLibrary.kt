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

/**
 * An [OperationLibrary] is a collection of [Operations][Operation] which is searchable by
 * [Operation.name], [Operation.inputType], and [Operation.outputType].
 */
interface OperationLibrary {
  /**
   * Finds a matching [Operation] within the [OperationLibrary], or returns `null` if none is found.
   */
  fun <Input, Output> findOperation(
    name: String,
    inputType: Class<in Input>,
    outputType: Class<out Output>,
  ): Operation<in Input, out Output>?
}

/**
 * Finds a matching [Operation] within the receiving [OperationLibrary], or returns `null` if none
 * is found.
 */
inline fun <reified T> OperationLibrary.findOperation(name: String): Operation<in T, out T>? =
  findOperation(name, T::class.java, T::class.java)
