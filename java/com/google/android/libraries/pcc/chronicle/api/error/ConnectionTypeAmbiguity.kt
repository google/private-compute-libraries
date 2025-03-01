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

package com.google.android.libraries.pcc.chronicle.api.error

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider

/**
 * Thrown if a particular [Connection] name/type is supported more than once by the
 * [ConnectionProvider]s registered with Chronicle.
 *
 * **Why is this an error?**
 *
 * Connection type is used to uniquely identify a way to access a particular type of data, to
 * support precise data flow structure and policy enforcement.
 */
class ConnectionTypeAmbiguity(
  connectionAsString: String,
  connectionProviders: Set<ConnectionProvider>,
) : ChronicleError(constructMessage(connectionAsString, connectionProviders)) {
  companion object {
    private fun constructMessage(
      connectionAsString: String,
      connectionProviders: Set<ConnectionProvider>,
    ): String {
      return "Connection name/type $connectionAsString supported more than once by " +
        "connectionProviders(s): $connectionProviders"
    }
  }
}
