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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import com.google.android.libraries.pcc.chronicle.api.remote.client.ChronicleServiceConnector.State
import com.google.android.libraries.pcc.chronicle.util.Logcat
import java.time.Duration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/**
 * Default implementation of [ChronicleServiceConnector], automatically connects and re-connects to
 * the ChronicleService using [Context.bindService].
 *
 * The reconnection/retry flow works as follows:
 *
 * ```
 * 1. While we haven't seen [maximumFailures] failures in a row:
 *    1. emit([State.Disconnected])
 *    1. delay for any backoff time from previous attempts
 *    1. emit([State.Connecting])
 *    1. `Context.bindService(serviceConnection)`
 *    1. if serviceConnection.onServiceConnected isn't called within the [timeout]:
 *       1. increment our repeated failures count, increase our backoff delay period
 *       1. go to top of loop
 *    1. if an error occurred while trying to connect:
 *       1. increment our repeated failures count, increase our backoff delay period
 *       1. go to top of loop
 *    1. otherwise:
 *       1. emit([State.Connected])
 *       1. set our repeated failures count and backoff delay to 0
 *       1. wait until the connection is lost (serviceConnection.onServiceDisconnected is called)
 *       1. go to the top of the loop
 * 1. emit([State.Unavailable])
 * 1. Exit the flow
 * ```
 *
 * @param connectionScope A coroutine scope to which the lifecycle of the service binding should be
 * scoped. When this scope is canceled, [Context.unbindService] will be used to release a bind to
 * the ChronicleService.
 * @param serviceComponentName The [ComponentName] where the ChronicleService can be found.
 * @param timeout Maximum amount of time to spend trying to bind to the ChronicleService before
 * trying again after a backoff.
 */
class DefaultChronicleServiceConnector(
  private val context: Context,
  connectionScope: CoroutineScope,
  private val serviceComponentName: ComponentName,
  private val timeout: Duration,
  private val maximumFailures: Int = 10
) : ChronicleServiceConnector {
  private val serviceConnection = atomic<ServiceConnection?>(null)

  override val connectionState: SharedFlow<State> =
    flow {
        val intent = Intent().apply { component = serviceComponentName }
        var retryDelayMillis = 0L
        var repeatedFailures = 0

        while (currentCoroutineContext().isActive && repeatedFailures < maximumFailures) {
          // We start off disconnected.
          emit(State.Disconnected)

          // Suspend for a backoff (if any is necessary) in the case that this is a retry.
          delay(retryDelayMillis)

          try {
            // Let downstream users know we are going to try to connect.
            emit(State.Connecting)

            // Create a deferred we can use to be notified when the connection is dropped.
            val disconnectedDeferred = CompletableDeferred<Unit>()

            // Try to connect, with a timeout.
            val remote =
              withTimeout(timeout.toMillis()) {
                suspendCancellableCoroutine<IRemote> { continuation ->
                  val connection: ServiceConnection =
                    object : ServiceConnection {
                      override fun onServiceConnected(name: ComponentName, service: IBinder?) {
                        if (!continuation.isActive) return
                        if (service != null) {
                          continuation.resume(IRemote.Stub.asInterface(service))
                        } else {
                          continuation.resumeWithException(
                            IllegalStateException(
                              "IBinder was null for service: $serviceComponentName"
                            )
                          )
                        }
                      }

                      override fun onServiceDisconnected(name: ComponentName) {
                        logcat.i("Default CSC: onServiceDisconnected")
                        if (!continuation.isActive) return
                        serviceConnection.value = null
                        disconnectedDeferred.complete(Unit)
                      }
                    }

                  continuation.invokeOnCancellation {
                    logcat.d("Default CSC: connection coroutine canceled")
                    context.unbindService(connection)
                  }
                  if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                    continuation.resumeWithException(
                      IllegalStateException("Could not start service: $serviceComponentName")
                    )
                  }
                  serviceConnection.value = connection
                }
              }

            // If we've reached this line we're connected! Reset the failure count and backoff
            // delay.
            emit(State.Connected(remote))
            repeatedFailures = 0
            retryDelayMillis = 0

            // Wait for disconnect, to retry connecting again.
            disconnectedDeferred.await()
          } catch (e: TimeoutCancellationException) {
            logcat.w(e, "Default CSC: timeout - [repeatedFailures = %d]", repeatedFailures)
            repeatedFailures++
            retryDelayMillis = retryDelayMillis * repeatedFailures + RETRY_DELAY_INCREMENT_MS
          } catch (e: IllegalStateException) {
            logcat.w(
              e,
              "Default CSC: illegal state exception - [repeatedFailures = %d]",
              repeatedFailures
            )
            repeatedFailures++
            retryDelayMillis = retryDelayMillis * repeatedFailures + RETRY_DELAY_INCREMENT_MS
          }
        }

        if (repeatedFailures == maximumFailures) {
          // Exited the loop due to too many failures in a row.
          emit(
            State.Unavailable(
              IllegalStateException(
                "Tried to connect to $serviceComponentName, failed $maximumFailures times in a row"
              )
            )
          )
        }
      }
      .onEach { logcat.i("Default CSC: State = %s", it) }
      .onCompletion {
        // If the scope was canceled while we were still connected, unbind the service.
        val connection = serviceConnection.getAndUpdate { null }
        connection?.let { context.unbindService(it) }
      }
      .shareIn(
        scope = connectionScope,
        // Use lazily-started mode, so we don't try to connect immediately - but only when needed.
        started = SharingStarted.Lazily,
        // Replay the last-seen Connection State to any new subscribers.
        replay = 1
      )

  companion object {
    private const val RETRY_DELAY_INCREMENT_MS = 100L

    private val logcat = Logcat.clientSide
  }
}
