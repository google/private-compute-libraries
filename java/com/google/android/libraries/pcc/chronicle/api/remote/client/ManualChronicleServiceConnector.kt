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
import com.google.android.libraries.pcc.chronicle.api.remote.client.ChronicleServiceConnector.State
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of [ChronicleServiceConnector] which exposes the ability to manually configure
 * the [IRemote] binder to be used downstream by [AidlTransport].
 *
 * This class is an alternative to using [DefaultChronicleServiceConnector], intended primarily for
 * situations where a client is unable to directly bind to the [ChronicleService]. For example:
 * within an isolated sandbox process.
 */
class ManualChronicleServiceConnector : ChronicleServiceConnector {
  override val connectionState = MutableStateFlow<State>(State.Disconnected)

  /** Property referencing the [IRemote] binder provided by a [ChronicleService]. */
  var binder: IRemote?
    get() = (connectionState.value as? State.Connected)?.binder
    set(value) {
      connectionState.value = value?.let { State.Connected(it) } ?: State.Disconnected
    }
}
