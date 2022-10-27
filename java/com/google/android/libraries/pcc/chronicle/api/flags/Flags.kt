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

/**
 * Flags are simple remote configuration used to mitigate issues in production by enabling/disabling
 * functionality at a granular level without requiring a release.
 */
data class Flags(
  /**
   * When true, all new connection requests will fail. This means that processors can't retrieve
   * data from any other components.
   */
  val failNewConnections: Boolean = false,
  /**
   * When true, all new connection requests will fail (ignoring `failNewConnections` state).
   *
   */
  val emergencyDisable: Boolean = false,
  /** When true, the DataCacheStorageDataRemovalRequestListener::onDataRemovalRequest will no-op. */
  val disableChronicleDataRemovalRequestListener: Boolean = false,
)
