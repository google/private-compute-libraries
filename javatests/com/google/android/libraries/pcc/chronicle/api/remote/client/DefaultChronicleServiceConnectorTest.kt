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

import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultChronicleServiceConnectorTest {
  private lateinit var testContext: Context
  private lateinit var application: ConnectorTestApplication
  private lateinit var connectionScope: CoroutineScope
  private lateinit var serviceComponentName: ComponentName

  @Before
  fun setUp() {
    application = ApplicationProvider.getApplicationContext()
    testContext = InstrumentationRegistry.getInstrumentation().context
    connectionScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    serviceComponentName = ComponentName(application, ConnectorTestService::class.java)
  }

  @After
  fun tearDown() {
    application.callsToOnBind = 0
    application.callsToOnUnbind = 0
    application.latch = null
    application.returnNullBinder = false
    application.unbound = null
    connectionScope.cancel()
  }

  @Test
  fun connectionState_subscribe_connects() = runBlocking {
    val connector =
      DefaultChronicleServiceConnector(
        context = testContext,
        connectionScope = connectionScope,
        serviceComponentName = serviceComponentName,
        timeout = Duration.ofSeconds(5)
      )

    val states = connector.connectionState.take(3).toList()

    assertThat(states[0]).isEqualTo(ChronicleServiceConnector.State.Disconnected)
    assertThat(states[1]).isEqualTo(ChronicleServiceConnector.State.Connecting)
    assertThat(states[2]).isInstanceOf(ChronicleServiceConnector.State.Connected::class.java)
  }

  @Test
  fun connectionState_subscribe_retriesUntilBind(): Unit = runBlocking {
    val connector =
      DefaultChronicleServiceConnector(
        context = testContext,
        connectionScope = connectionScope,
        serviceComponentName = serviceComponentName,
        timeout = Duration.ofSeconds(5)
      )

    launch {
      application.returnNullBinder = true
      delay(2000)
      application.returnNullBinder = false
    }

    val states =
      connector.connectionState
        .takeWhile { it !is ChronicleServiceConnector.State.Connected }
        .toList()

    assertWithMessage("Should be an even number of Disconnected, Connecting states with retries.")
      .that(states.size % 2).isEqualTo(0)
    states.forEachIndexed { index, state ->
      if (index % 2 == 0) assertThat(state).isEqualTo(ChronicleServiceConnector.State.Disconnected)
      else assertThat(state).isEqualTo(ChronicleServiceConnector.State.Connecting)
    }
  }

  @Test
  fun connectionState_subscribe_timeoutRetries(): Unit = runBlocking {
    val connector =
      DefaultChronicleServiceConnector(
        context = testContext,
        connectionScope = connectionScope,
        serviceComponentName = serviceComponentName,
        timeout = Duration.ofSeconds(5)
      )

    application.latch = CountDownLatch(1)

    val states = async(start = CoroutineStart.UNDISPATCHED) {
      connector.connectionState
        .takeWhile { it !is ChronicleServiceConnector.State.Connected }
        .toList()
    }

    application.latch?.countDown()

    assertWithMessage("Should be an even number of Disconnected, Connecting states with retries.")
      .that(states.await().size % 2).isEqualTo(0)
    states.await().forEachIndexed { index, state ->
      if (index % 2 == 0) assertThat(state).isEqualTo(ChronicleServiceConnector.State.Disconnected)
      else assertThat(state).isEqualTo(ChronicleServiceConnector.State.Connecting)
    }
  }

  @Test
  fun connectionScope_cancelled_unbinds(): Unit = runBlocking {
    val connector =
      DefaultChronicleServiceConnector(
        context = testContext,
        connectionScope = connectionScope,
        serviceComponentName = serviceComponentName,
        timeout = Duration.ofSeconds(1)
      )

    application.unbound = CompletableDeferred()

    connector.connectionState.first { it is ChronicleServiceConnector.State.Connected }
      as ChronicleServiceConnector.State.Connected

    connectionScope.cancel()

    application.unbound?.await()

    assertThat(application.callsToOnUnbind).isEqualTo(1)
  }

  @Test
  fun connectionState_maximumFailures_givesUp(): Unit = runBlocking {
    val connector =
      DefaultChronicleServiceConnector(
        context = testContext,
        connectionScope = connectionScope,
        serviceComponentName = serviceComponentName,
        timeout = Duration.ofSeconds(0),
        maximumFailures = 3
      )

    application.returnNullBinder = true

    val e = connector.connectionState.first { it is ChronicleServiceConnector.State.Unavailable }
      as ChronicleServiceConnector.State.Unavailable
    assertThat(e.cause).hasMessageThat().contains("failed 3 times in a row")
  }
}

class ConnectorTestApplication : Application() {
  var returnNullBinder: Boolean = false
  var callsToOnBind: Int = 0
  var callsToOnUnbind: Int = 0
  var latch: CountDownLatch? = null
  var unbound: CompletableDeferred<Unit>? = null
}

class ConnectorTestService : Service() {
  override fun onBind(intent: Intent?): IBinder? {
    val appContext = applicationContext as ConnectorTestApplication

    appContext.callsToOnBind++
    appContext.latch?.await()

    if (appContext.returnNullBinder) return null

    return object : IRemote.Stub() {
      override fun serve(request: RemoteRequest, callback: IResponseCallback) {
        callback.onComplete()
      }
    }
  }

  override fun onUnbind(intent: Intent?): Boolean {
    val appContext = applicationContext as ConnectorTestApplication
    appContext.callsToOnUnbind++
    appContext.unbound?.complete(Unit)
    return super.onUnbind(intent)
  }
}
