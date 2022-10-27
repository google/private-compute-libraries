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

package com.google.android.libraries.pcc.chronicle.api.remote.client

import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction of the transportation layer used to communicate from client to server.
 *
 * **Note:** Transports shouldn't ever need to be used directly by Data Stewards or Feature
 * Developers. They are an internal concept for Chronicle and typically need only be instantiated
 * and provided to RemoteClients as dependencies.
 */
interface Transport {
  /**
   * Issues the [request] to the server and returns a flow of any pages of response information as
   * [RemoteResponse] objects.
   */
  fun serve(request: RemoteRequest): Flow<RemoteResponse>
}
