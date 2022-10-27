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

package com.google.android.libraries.pcc.chronicle.codegen.util

import com.google.common.base.CaseFormat
import java.util.Locale

private val INNER_TITLE_CASE_PATTERN = Regex("([a-z0-9])([A-Z])")

/** Get the capitalized Snake case version of the receiving [CharSequence]. */
fun CharSequence.upperSnake(): String =
  replace(INNER_TITLE_CASE_PATTERN) { "${it.groupValues[1]}_${it.groupValues[2]}" }
    .uppercase(Locale.ROOT)

/** Convert proto "field_name" to Chronicle "fieldName". */
fun String.toChronicleFieldName() = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this)

/** Capitalizes the string by ensuring the first character is capital. */
fun String.capitalize(): String = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

/** Decapitalizes the string by ensuring the first character is lowercase. */
fun String.decapitalize(): String = replaceFirstChar { it.lowercase(Locale.getDefault()) }
