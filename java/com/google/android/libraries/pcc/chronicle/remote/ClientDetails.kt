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

package com.google.android.libraries.pcc.chronicle.remote

/** Encapsulates information about a client of the ChronicleService. */
data class ClientDetails(
  /** The linux User ID for the client of ChronicleService. */
  val userId: Int,
  /** Whether or not the client of the ChronicleService is an isolated process. */
  val isolationType: IsolationType = IsolationType.UNKNOWN,
  /** Any associated packages for APKs under the ownership of the user with [userId]. */
  val associatedPackages: List<String> = emptyList(),
) {
  /** Type of isolation the client is under. */
  enum class IsolationType {
    UNKNOWN,
    ISOLATED_PROCESS,
    DEFAULT_PROCESS,
  }
}
