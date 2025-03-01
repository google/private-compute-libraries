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

package com.google.android.libraries.pcc.chronicle.api.remote

import android.os.DeadObjectException
import com.google.android.libraries.pcc.chronicle.api.error.PolicyViolation
import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * Creates a [CoroutineExceptionHandler] from the receiving [IResponseCallback].
 *
 * This [CoroutineExceptionHandler] can be used in conjunction with a SupervisorJob-based coroutine
 * scope to unlock the following functionality:
 * * Not crashing the server application if an exception is thrown while handling a request.
 * * Reporting that exception back to the client application via [IResponseCallback.onError].
 *
 * Before reporting the error to the client application, the handler will first ping the binder for
 * the [IResponseCallback] to ensure it's still alive (to not risk an
 * [android.os.DeadObjectException]).
 *
 * **Note:** This method is intended for use by internal Chronicle infrastructure, it should not be
 * necessary to be used by clients of Chronicle directly.
 */
fun IResponseCallback.createCoroutineExceptionHandler(): CoroutineExceptionHandler =
  CoroutineExceptionHandler { _, throwable ->
    if (!asBinder().pingBinder()) return@CoroutineExceptionHandler

    // If the binder is alive, send the error.
    try {
      when (throwable) {
        is RemoteError -> onError(throwable)
        is PolicyViolation ->
          onError(
            RemoteError(
              RemoteErrorMetadata.newBuilder()
                .setErrorType(RemoteErrorMetadata.Type.POLICY_VIOLATION)
                .setMessage(throwable.toString())
                .build()
            )
          )
        else ->
          onError(
            RemoteError(
              RemoteErrorMetadata.newBuilder()
                .setErrorType(RemoteErrorMetadata.Type.UNKNOWN)
                .setMessage("Unknown error occurred: ${throwable.javaClass}")
                .build()
            )
          )
      }
    } catch (e: DeadObjectException) {
      throw AssertionError("Impossible", e)
    }
  }
