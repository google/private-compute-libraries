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

package com.google.android.libraries.pcc.chronicle.api.flags

import kotlinx.coroutines.flow.StateFlow

/**
 * Entrypoint of configuration [Flags] for Chronicle: an abstraction layer between Chronicle and the
 * underlying application's configuration system.
 *
 * Each application integrating Chronicle will need to supply a [FlagsReader] to the dagger graph.
 * Most applications will populate the [Flags] for their [FlagsReader] using DeviceConfig. For
 * simplicity, it's required that [Flags] instances be identical for an entire application - even
 * if the application has multiple processes.
 */
interface FlagsReader {
  /** The current value of the [Flags] for Chronicle. */
  val config: StateFlow<Flags>
}
