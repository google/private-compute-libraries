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

package com.google.android.libraries.pcc.chronicle.util

import java.time.Instant

/**
 * A source for retrieving the current [Instant], an abstraction that allows for the user to be
 * given predictable/configurable fake implementations in test scenarios.
 *
 */
fun interface TimeSource {
  /** Fetch the current time as an [Instant]. */
  fun now(): Instant

  companion object {
    private val SYSTEM = TimeSource { Instant.now() }

    @JvmStatic fun system(): TimeSource = SYSTEM
  }
}
