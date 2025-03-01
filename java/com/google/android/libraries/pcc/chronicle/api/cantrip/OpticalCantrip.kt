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

package com.google.android.libraries.pcc.chronicle.api.cantrip

import com.google.android.libraries.pcc.chronicle.api.operation.Action
import com.google.android.libraries.pcc.chronicle.api.operation.Operation
import com.google.android.libraries.pcc.chronicle.api.optics.Traversal

/**
 * A [Cantrip] composed of an optical [Traversal] and an [Operation].
 *
 * When [invoked][invoke], the operation is applied to the focused value of type [Field] within the
 * [Data]. If the operation returns [Action.OmitFromParent] or [Action.OmitFromRoot], `null` is
 * returned.
 */
class OpticalCantrip<Data, Field>(
  private val optic: Traversal<Data, Data, Field, Field>,
  private val operation: Operation<Field, Field>,
) : Cantrip<Data> {
  override fun invoke(datum: Data): Data? {
    return when (val result = optic.modifyWithAction(datum, operation)) {
      Action.OmitFromParent,
      Action.OmitFromRoot -> null
      is Action.Throw -> throw result.exception
      is Action.Update -> result.newValue
    }
  }
}
