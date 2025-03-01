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

package com.google.android.libraries.pcc.chronicle.remote.handler

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity

/**
 * Defines a component capable of handling a [RemoteRequest], given any [input] data, associated
 * [Policy], and a callback to return the results with. [RemoteServerHandler] exists as a
 * translation layer between the raw RemoteRequests/RemoteEntities exchanged between client and
 * server in remote connections - and the data-steward-supplied [RemoteServer] implementations which
 * rely on WrappedEntities and return values (rather than using the [IResponseCallback] themselves).
 *
 * Each implementation of [RemoteServerHandler] should correspond to a given [RemoteServer] type.
 * Part of the contract of [handle] is that only the [IResponseCallback.onData] needs to be called
 * by the implementation. To return an error, the implementation need only throw.
 * [IResponseCallback.onComplete] and [IResponseCallback.provideCancellationSignal] are to be called
 * by the [RemoteRouter].
 */
interface RemoteServerHandler {
  /**
   * Handles a request. This method is a `suspend` method because it's allowed to suspend the
   * current coroutine, switch dispatchers, etc. while it communicates with storage, if necessary.
   */
  suspend fun handle(policy: Policy?, input: List<RemoteEntity>, callback: IResponseCallback)
}
