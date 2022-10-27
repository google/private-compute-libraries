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

import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest

/**
 * Implies that the requested [Connection] was not listed as required by the requesting
 * [ProcessorNode].
 */
class ConnectionNotDeclared(request: ConnectionRequest<*>) :
  ChronicleError(constructMessage(request)) {
  companion object {
    private fun constructMessage(request: ConnectionRequest<*>): String {
      return "Requested connection is not declared as required by requester: $request"
    }
  }
}
