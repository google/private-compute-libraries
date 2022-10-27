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

import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import kotlinx.coroutines.flow.SharedFlow

/**
 * Implementations of ChronicleServiceConnector are responsible for providing a [SharedFlow] which
 * can be collected to monitor binding/connection state between a client and a ChronicleService
 * instance.
 */
interface ChronicleServiceConnector {
  /**
   * A [SharedFlow] which will emit the [State] of connection with a ChronicleService.
   *
   * This flow is lazily started and is shared. Meaning: it will wait to begin connecting to the
   * [ChronicleService] until the first time it is collected, and it will only maintain a single
   * connection - even if multiple clients are collecting the flow.
   */
  val connectionState: SharedFlow<State>

  /** Represents the state of a service binding connection with the ChronicleService. */
  sealed class State {
    /**
     * The service is not currently connected-to. Either the service has never been bound-to, or it
     * has been disconnected.
     */
    object Disconnected : State()

    /**
     * The service is currently being connected-to. This corresponds with having used
     * [Context.bindService] but not having had a call back to [ServiceConnector.onConnected] yet.
     */
    object Connecting : State()

    /**
     * The service has been connected-to successfully, and an [IRemote] [binder] has been received.
     */
    data class Connected(val binder: IRemote) : State()

    /**
     * The service cannot be connected-to, or the service connector has given up retrying.
     */
    data class Unavailable(val cause: Throwable) : State()
  }
}
