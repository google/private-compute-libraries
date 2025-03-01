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

import android.os.IBinder.DeathRecipient
import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.remote.ICancellationSignal
import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata.Type
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.createCoroutineExceptionHandler
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer
import com.google.android.libraries.pcc.chronicle.remote.handler.RemoteServerHandlerFactory
import com.google.android.libraries.pcc.chronicle.util.Logcat
import java.lang.ref.WeakReference
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The RemoteRouter is first and foremost an implementation of [IRemote]. Its purpose is to fulfill
 * [RemoteRequests][RemoteRequest] via the [serve] method. It does so, by delegating the request to
 * one of a collection of [RemoteServer] implementations, maintained by the [RemoteContext] after
 * first checking the policy associated with the request using [Chronicle].
 *
 * While handling the [RemoteRequest], the RemoteRouter needs to follow the protocol of the
 * [IResponseCallback] interface - calling the `onComplete` and `onError` callbacks after the server
 * has had an opportunity to respond, and providing an [ICancellationSignal] before the server gets
 * started.
 *
 * **Note:** This class is internal to Chronicle and outside teams should not need to interact with
 * it directly.
 */
class RemoteRouter(
  private val scope: CoroutineScope,
  private val context: RemoteContext,
  private val policyChecker: RemotePolicyChecker,
  private val handlerFactory: RemoteServerHandlerFactory,
  private val clientDetailsProvider: ClientDetailsProvider,
) : IRemote.Stub() {
  private val nextRequestNumber = atomic(1)

  override fun serve(request: RemoteRequest?, callback: IResponseCallback?) {
    if (request == null || callback == null) return

    val requestNumber = nextRequestNumber.getAndIncrement()

    // Fetch the client details while we're still running on the binder thread.
    val clientDetails = clientDetailsProvider.getClientDetails()

    // Launch a coroutine which responds to the request, and calls callback.onComplete() afterwards,
    // or otherwise: calls callback.onError(err) if an error occurs.
    val serveJob =
      scope.launch(callback.createCoroutineExceptionHandler()) {
        val metadata = request.metadata

        logcat.v(
          "RemoteRouter[%d] checking policy\n\t%s\n\t%s]",
          requestNumber,
          clientDetails,
          metadata,
        )

        // Find the server, check the policy, and fetch a handler. If no server can be found, a
        // RemoteError will be thrown and passed to the client by the CoroutineExceptionHandler.
        val server: RemoteServer<*> =
          context.findServer(metadata)
            ?: throw RemoteError(
              type = Type.UNSUPPORTED,
              message = "Server not found for request with metadata: $metadata",
            )

        // Check the policy and get it (if there is one). If the policy check fails, or the policy
        // could not be found, the exception will be caught by the CoroutineExceptionHandler and
        // sent to the client.
        val policy = policyChecker.checkAndGetPolicyOrThrow(metadata, server, clientDetails)

        // Build a handler from the request and server. If a handler can't be built, the exception
        // will be caught by the CoroutineExceptionHandler and sent to the client.
        val handler = handlerFactory.buildServerHandler(metadata, server)

        logcat.v("RemoteRouter[%d] handling request", requestNumber)

        // Handle the call! If the server throws while handling, the exception will be caught by
        // the CoroutineExceptionHandler and sent to the client.
        //
        // In some situations (like with stream subscription requests), it's possible for this to
        // suspend forever - or at least until the client uses the cancellation signal. That's okay.
        logcat.timeVerbose("RemoteRouter[$requestNumber] handled") {
          handler.handle(policy, request.entities, callback)

          // Mark the whole operation as done.
          logcat.v("RemoteRouter[%d] calling onComplete", requestNumber)
          callback.onComplete()
        }
      }

    // Register a death recipient with the callback, so we can cancel the job if the client dies.
    val deathRecipient = DeathRecipient(serveJob::cancel)
    logcat.v("RemoteRouter[%d] linking death recipient", requestNumber)
    callback.asBinder().linkToDeath(deathRecipient, 0)
    serveJob.invokeOnCompletion {
      // Remove our death recipient when the job is done/fails, this is important to avoid leaking
      // the death recipient in the BinderProxy.
      logcat.v("RemoteRouter[%d] unlinking death recipient", requestNumber)
      callback.asBinder().unlinkToDeath(deathRecipient, 0)
    }

    // Provide a cancellation signal which cancels the serveJob if used.
    logcat.v("RemoteRouter[%d] providing cancellation signal", requestNumber)
    callback.provideCancellationSignal(
      CancellationSignal(requestNumber, WeakReference(serveJob), WeakReference(callback))
    )
  }

  // We use WeakReferences in the CancellationSignal so that the callback given to the client
  // doesn't
  // block garbage collection of the job or callback if the client didn't manage to release the
  // CancellationSignal on its end.
  private class CancellationSignal(
    private val requestNumber: Int,
    private val job: WeakReference<Job>,
    private val callback: WeakReference<IResponseCallback>,
  ) : ICancellationSignal.Stub() {
    override fun cancel() {
      logcat.v("RemoteRouter[%d] cancellation signal triggered", requestNumber)

      // Cancel the job and, if the callback is still active - send complete (cancellation is
      // considered a successful end result of the interaction).
      job.get()?.cancel()
      callback.get()?.takeIf { it.asBinder().pingBinder() }?.onComplete()
    }
  }

  companion object {
    private val logcat = Logcat.serverSide
  }
}
