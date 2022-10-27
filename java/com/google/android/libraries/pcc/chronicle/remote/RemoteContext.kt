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

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer

/**
 * A [RemoteContext] manages all [RemoteServer] instances known to a ChronicleService running in a
 * given app. Its primary role is to help in locating an appropriate [RemoteServer] given the
 * [metadata][RemoteRequestMetadata] associated with a request.
 */
interface RemoteContext {
  /**
   * Given a [requestMetadata], find the appropriate [RemoteServer] to handle that request, if any
   * exists.
   */
  fun findServer(requestMetadata: RemoteRequestMetadata): RemoteServer<*>?

  /** Given a [dtd], find all [RemoteServer] instances known which serve data of that type. */
  fun findServers(dtd: DataTypeDescriptor): Collection<RemoteServer<*>>
}
