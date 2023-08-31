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
 * Default implementation of [OperationLibrary] for use in constructing cantrips.
 *
 * The [operations] provided are grouped by [Operation.name], there can be more than one [Operation]
 * for a given name, as long as the [Operation.inputType] / [Operation.outputType] values are
 * unique.
 */
class DefaultOperationLibrary(operations: Set<Operation<*, *>>) : OperationLibrary {
  private val operationsByName: Map<String, List<Operation<*, *>>> = operations.groupBy { it.name }

  @Synchronized
  @Suppress("UNCHECKED_CAST") // It is checked, using inClass/outClass.
  override fun <Input, Output> findOperation(
    name: String,
    inputType: Class<in Input & Any>,
    outputType: Class<out Output & Any>,
  ): Operation<in Input, out Output>? {
    val byName = operationsByName[name] ?: return null

    // First look for an exact match with the classes.
    byName
      .find { it.inputType == inputType && it.outputType == outputType }
      ?.let {
        return it as Operation<in Input, out Output>
      }

    // Next look for an exact match with the input class and for output assignability.
    byName
      .find {
        it.inputType == inputType &&
          (it.outputType == Void::class.java || outputType.isAssignableFrom(it.outputType))
      }
      ?.let {
        return it as Operation<in Input, out Output>
      }

    // Next, look for input/output assignability.
    return byName.find {
      it.inputType.isAssignableFrom(inputType) &&
        (it.outputType == Void::class.java || outputType.isAssignableFrom(it.outputType))
    } as? Operation<in Input, out Output>
  }
}
