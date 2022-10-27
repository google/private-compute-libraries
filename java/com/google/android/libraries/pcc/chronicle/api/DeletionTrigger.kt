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

package com.google.android.libraries.pcc.chronicle.api

/**
 * A deletion trigger is an an event tied to a [targetField], such as clearing data stored about an
 * installed package when the package is uninstalled.
 */
data class DeletionTrigger(
  val trigger: Trigger,
  /**
   * The field containing the triggering values. Use `.` to reference nested fields. For example, a
   * [Trigger.PACKAGE_UNINSTALLED] attached `event.srcPkg`.
   */
  val targetField: String
)

/** Supported root trigger types. */
enum class Trigger {
  PACKAGE_UNINSTALLED
}
