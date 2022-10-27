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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.error.PolicyViolation
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata.Type
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IResponseCallbackExtTest {
  private lateinit var scope: CoroutineScope
  private var errorCaught: RemoteError? = null
  private val callback = object : IResponseCallback.Stub() {
    override fun onError(error: RemoteError?) {
      errorCaught = error
    }

    override fun onData(data: RemoteResponse?) = Unit
    override fun onComplete() = Unit
    override fun provideCancellationSignal(signal: ICancellationSignal?) = Unit
  }

  @Before
  fun setUp() {
    scope = CoroutineScope(SupervisorJob())
  }

  @After
  fun tearDown() {
    scope.cancel()
    errorCaught = null
  }

  @Test
  fun createCoroutineExceptionHandler_handleRemoteError() {
    runBlocking {
      scope.launch(callback.createCoroutineExceptionHandler()) {
        throw RemoteError(
          RemoteErrorMetadata.newBuilder()
            .setErrorType(Type.INVALID)
            .setMessage("Hello World")
            .build()
        )
      }.join()
    }

    assertThat(errorCaught!!.metadata.errorType).isEqualTo(Type.INVALID)
    assertThat(errorCaught!!.metadata.message).isEqualTo("Hello World")
    assertWithMessage("Original coroutine scope with the SupervisorJob should still be active")
      .that(scope.isActive)
      .isTrue()
  }

  @Test
  fun createCoroutineExceptionHandler_handlePolicyViolation() {
    runBlocking {
      scope.launch(callback.createCoroutineExceptionHandler()) {
        throw PolicyViolation("A policy was violated!!!!")
      }.join()
    }

    assertThat(errorCaught!!.metadata.errorType).isEqualTo(Type.POLICY_VIOLATION)
    assertThat(errorCaught!!.metadata.message)
      .isEqualTo(PolicyViolation("A policy was violated!!!!").toString())
    assertWithMessage("Original coroutine scope with the SupervisorJob should still be active")
      .that(scope.isActive)
      .isTrue()
  }

  @Test
  fun createCoroutineExceptionHandler_handleUnexpectedException() {
    runBlocking {
      scope.launch(callback.createCoroutineExceptionHandler()) {
        throw IllegalArgumentException("You used bad data....")
      }.join()
    }

    assertThat(errorCaught!!.metadata.errorType).isEqualTo(Type.UNKNOWN)
    assertThat(errorCaught!!.metadata.message)
      .isEqualTo("Unknown error occurred: class java.lang.IllegalArgumentException")
    assertWithMessage("Original coroutine scope with the SupervisorJob should still be active")
      .that(scope.isActive)
      .isTrue()
  }

  @Suppress("RedundantAsync")
  @Test
  fun createCoroutineExceptionHandler_handleExceptionThrownWhileDeeplyNested() {
    var secondWasCanceled = false
    runBlocking {
      scope.launch(callback.createCoroutineExceptionHandler()) {
        coroutineScope {
          launch(Executors.newCachedThreadPool().asCoroutineDispatcher()) {
            coroutineScope {
              async {
                throw IllegalArgumentException("You used bad data....")
              }.await()
            }
          }

          launch {
            suspendCancellableCoroutine<Unit> {
              it.invokeOnCancellation { secondWasCanceled = true }
            }
          }
        }
      }.join()
    }

    assertThat(errorCaught!!.metadata.errorType).isEqualTo(Type.UNKNOWN)
    assertThat(errorCaught!!.metadata.message)
      .isEqualTo("Unknown error occurred: class java.lang.IllegalArgumentException")
    assertWithMessage("Original coroutine scope with the SupervisorJob should still be active")
      .that(scope.isActive)
      .isTrue()
    assertWithMessage(
      "Sibling coroutines which are both children of original launched parent " +
        "coroutine should get canceled."
    )
      .that(secondWasCanceled)
      .isTrue()
  }
}
