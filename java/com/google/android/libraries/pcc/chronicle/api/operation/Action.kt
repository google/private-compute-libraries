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

/** Representation of the result of an [Operation] as an action to be performed by the caller. */
sealed class Action<B> {
  /**
   * An [Operation] which returns [Update] is requesting the caller to use the [newValue] in place
   * the input to the [Operation].
   *
   * TODO(b/205183415): Consider using an object pool to help reduce the number of allocations
   * incurred when Update results are used.
   */
  data class Update<B>(val newValue: B) : Action<B>()

  /**
   * An [Operation] which returns [OmitFromRoot] would like the caller to remove the entity from
   * within which the [OmitFromRoot] was returned from the highest possible level of nesting.
   */
  object OmitFromRoot : Action<Nothing>()

  /**
   * An [Operation] which returns [OmitFromParent] would like the caller to remove the entity from
   * within which the [OmitFromParent] was returned from the nearest collection containing that
   * entity.
   */
  object OmitFromParent : Action<Nothing>()

  /**
   * An [Operation] which returns [Throw] would like the caller to throw the [exception] on its
   * behalf.
   */
  data class Throw(val exception: Throwable) : Action<Nothing>()
}
