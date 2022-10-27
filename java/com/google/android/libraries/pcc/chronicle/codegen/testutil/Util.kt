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

package com.google.android.libraries.pcc.chronicle.codegen.testutil

import com.google.android.libraries.pcc.chronicle.codegen.FieldCategory
import com.google.android.libraries.pcc.chronicle.codegen.Type

fun Type.withoutFields(vararg exclude: String) =
  copy(fields = this.fields.filter { it.name !in exclude })

fun Type.withForeignReference(
  foreignRef: String,
  schemaName: String,
  hard: Boolean,
  additionalName: String? = null
) =
  copy(
    fields =
      this.fields.flatMap {
        if (it.name == foreignRef) {
          if (additionalName != null) {
            listOf(
              it,
              it.copy(
                name = additionalName,
                sourceName = it.name,
                category = FieldCategory.ForeignReference(schemaName, hard)
              ),
            )
          } else {
            listOf(it.copy(category = FieldCategory.ForeignReference(schemaName, hard)))
          }
        } else {
          listOf(it)
        }
      }
  )
