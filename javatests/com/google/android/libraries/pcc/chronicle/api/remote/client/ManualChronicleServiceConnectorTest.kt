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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualChronicleServiceConnectorTest {
  @Test
  fun newInstance_connectionStateIsDisconnected() = runBlocking {
    val connector = ManualChronicleServiceConnector()

    assertThat(connector.binder).isNull()
    assertThat(connector.connectionState.first())
      .isEqualTo(ChronicleServiceConnector.State.Disconnected)
  }

  @Test
  fun setBinder_nonNullValue_triggersStateConnected() = runBlocking {
    val connector = ManualChronicleServiceConnector()
    val connectedState = async {
      connector.connectionState.first { it is ChronicleServiceConnector.State.Connected }
    }

    val binder = IRemoteImpl()
    connector.binder = binder
    val receivedState = connectedState.await() as ChronicleServiceConnector.State.Connected

    assertThat(receivedState.binder).isSameInstanceAs(binder)
  }

  @Test
  fun setBinder_nullValue_triggersStateDisconnected() = runBlocking {
    val connector = ManualChronicleServiceConnector()

    // First place the connector in the connected state.
    val connectedState = async {
      connector.connectionState.first { it is ChronicleServiceConnector.State.Connected }
    }
    val binder = IRemoteImpl()
    connector.binder = binder
    connectedState.await()

    // Simulate a disconnect.
    val disconnectedState = async {
      connector.connectionState.first { it is ChronicleServiceConnector.State.Disconnected }
    }
    connector.binder = null

    assertThat(disconnectedState.await())
      .isSameInstanceAs(ChronicleServiceConnector.State.Disconnected)
  }

  class IRemoteImpl : IRemote.Stub() {
    override fun serve(request: RemoteRequest?, callback: IResponseCallback?) {
      // Not applicable/no-op
    }
  }
}
