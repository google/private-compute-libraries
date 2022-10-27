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

package com.google.android.libraries.pcc.chronicle.codegen.frontend

/**
 * Configuration entry for [ForeignReference] support in AutoChronicle.
 *
 * @property fieldName the field name to be treated as a reference.
 * @property schemaName if non-empty, this is the schema name for a foreign reference. This schema
 * must be already defined somewhere else.
 * @property hard if [true], the field will be treated as a hard reference.
 * @property additionalNamedField if empty, the existing field will be replaced with a foreign
 * reference. If non-empty, a new field will be added for the foreign reference using the provided
 * name, leaving the original field as-is.
 */
data class ForeignReference(
  val fieldName: String,
  val schemaName: String,
  val hard: Boolean,
  val additionalNamedField: String = ""
)
