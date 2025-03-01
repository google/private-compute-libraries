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

package com.google.android.libraries.pcc.chronicle.codegen.backend.lens

import com.google.android.libraries.pcc.chronicle.codegen.FieldEntry
import com.google.android.libraries.pcc.chronicle.codegen.Type
import com.squareup.kotlinpoet.CodeBlock

/**
 * Constructs [Lens] getters and setters based on a given [Type.Tooling].
 *
 * Alternatively:
 * [Lifts things up, and puts them down.](https://www.youtube.com/watch?v=ZabvU7Lay0s)
 */
interface LensBodyBuilder {
  /**
   * Returns whether or not this [LensBodyBuilder] can build getters and setters for the given
   * [field] of the provided [type].
   */
  fun supportsField(type: Type, field: FieldEntry): Boolean

  /** Builds a [CodeBlock] containing the body of a generated [Lens]'s getter. */
  fun buildGetterBody(type: Type, field: FieldEntry, entityParamName: String): CodeBlock

  /** Builds a [CodeBlock] containing the body of a generated [Lens]'s setter. */
  fun buildSetterBody(
    type: Type,
    field: FieldEntry,
    entityParamName: String,
    newValueParamName: String,
  ): CodeBlock
}
