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

import android.os.Bundle
import android.os.IBinder
import com.google.android.libraries.pcc.chronicle.api.remote.ICancellationSignal
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.util.Logcat
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

/**
 * Implementation of [Transport] which, given a connection to the ChronicleService, issues
 * [RemoteRequests][RemoteRequest] via AIDL.
 */
class AidlTransport(private val connector: ChronicleServiceConnector) : Transport {
  override fun serve(request: RemoteRequest): Flow<RemoteResponse> = callbackFlow {
    logcat.atVerbose().log("AidlTransport: serve called %s", request.metadata)
    val iRemote =
      connector.connectionState
        .mapNotNull { it as? ChronicleServiceConnector.State.Connected }
        .first()
        .binder

    logcat.atDebug().log("AidlTransport: received binder")

    // We will need to be careful to manage the reference to our cancellation signal so that it is
    // releasable. At the end of any operation, this reference should be nulled-out.
    val cancellationSignal = atomic<ICancellationSignal?>(null)
    val deathRecipient =
      IBinder.DeathRecipient {
        logcat.atDebug().log("AidlTransport: iRemote death recipient triggered")
        // Ensure we null-out the cancellation signal reference to not leak it.
        cancellationSignal.value = null
        channel.close(CONNECTION_LOST.copy())
      }
    iRemote.asBinder().linkToDeath(deathRecipient, 0)
    val callback =
      object : IResponseCallback.Stub() {
        override fun onData(data: RemoteResponse) {
          logcat.atVerbose().log("AidlTransport: onData [entities=%d]", data.entities.size)
          trySendBlocking(data)
        }

        override fun onError(error: RemoteError) {
          // We null-out the signal before closing the channel so we avoid attempting to send a
          // cancel signal in the awaitClose block below.
          logcat.atVerbose().withCause(error).log("AidlTransport: onError")
          cancellationSignal.value = null
          channel.close(error)
        }

        override fun onComplete() {
          // We null-out the signal before closing the channel so we avoid attempting to send a
          // cancel signal in the awaitClose block below.
          logcat.atVerbose().log("AidlTransport: onComplete")
          cancellationSignal.value = null
          channel.close()
        }

        override fun provideCancellationSignal(signal: ICancellationSignal) {
          logcat.atVerbose().log("AidlTransport: provideCancellationSignal")
          cancellationSignal.value = signal
        }
      }
    try {
      iRemote.serve(request, callback)
    } catch (e: Throwable) {
      channel.close(
        CONNECTION_LOST.copy(
          extras = Bundle().apply {
            putString("ORIGINAL_MESSAGE", e.message)
            putString("ORIGINAL_TYPE", e.javaClass.name)
          }
        )
      )
    }

    awaitClose {
      logcat.atVerbose().log("AidlTransport: done")
      iRemote.asBinder()?.unlinkToDeath(deathRecipient, 0)

      // If the coroutine was canceled, we should still have a non-null reference to the
      // cancellation signal. In this case - use it to inform the server that we are no longer
      // interested in receiving results.
      val signal = cancellationSignal.getAndUpdate { null }
      signal?.takeIf { it.asBinder().pingBinder() }?.cancel()
    }
  }

  companion object {
    val CONNECTION_LOST =
      RemoteError(
        RemoteErrorMetadata.newBuilder()
          .setErrorType(RemoteErrorMetadata.Type.UNKNOWN)
          .setMessage("Unknown error occurred, connection to server may have been lost.")
          .build()
      )

    private val logcat = Logcat.clientSide
  }
}
