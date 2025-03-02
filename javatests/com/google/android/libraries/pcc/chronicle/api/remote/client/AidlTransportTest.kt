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

import android.os.DeadObjectException
import android.os.IBinder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.ICancellationSignal
import com.google.android.libraries.pcc.chronicle.api.remote.IRemote
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponseMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.client.ChronicleServiceConnector.State
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AidlTransportTest {
  private val request = RemoteRequest(RemoteRequestMetadata.getDefaultInstance())
  private val connectionState = MutableSharedFlow<State>(replay = 1)
  private val connector =
    object : ChronicleServiceConnector {
      override val connectionState: SharedFlow<State>
        get() = this@AidlTransportTest.connectionState
    }

  private var serveCall = MutableStateFlow<Pair<RemoteRequest, IResponseCallback>?>(null)
  private val stub = IRemoteStub { request, callback ->
    if (request != null && callback != null) serveCall.value = request to callback
  }

  @Test
  @Suppress("DeferredIsResult", "DeferredReturnValueIgnored")
  fun serve_flowSuspendsUntilConnected(): Unit = runBlocking {
    val transport = AidlTransport(connector)

    val resultFlow = transport.serve(request)

    // Request shouldn't be issued until the flow is collected.
    assertThat(serveCall.value).isNull()

    val results =
      async(start = CoroutineStart.UNDISPATCHED) {
        resultFlow.toList().also {
          assertThat(connectionState.replayCache.last()).isInstanceOf(State.Connected::class.java)
        }
      }

    connectionState.emit(State.Disconnected)
    assertThat(results.isCompleted).isFalse()
    connectionState.emit(State.Connecting)
    assertThat(results.isCompleted).isFalse()
    connectionState.emit(State.Connected(stub))

    // Await the call.
    serveCall.first { it != null }

    val response = RemoteResponse(RemoteResponseMetadata.getDefaultInstance())
    provideCancellationSignal()
    sendResponse(response)
    sendComplete()

    assertThat(results.await()).containsExactly(response)

    stub.verifyDeathRecipientLinkedAndUnlinked()
  }

  @Test
  fun serve_flowEmitsMultipleResponses(): Unit = runBlocking {
    val transport = AidlTransport(connector)
    connectionState.emit(State.Connected(stub))
    val results = async { transport.serve(request).toList() }

    // Await the call.
    serveCall.first { it != null }

    val firstResponse =
      RemoteResponse(
        RemoteResponseMetadata.getDefaultInstance(),
        listOf(
          RemoteEntity(EntityMetadata.getDefaultInstance()),
          RemoteEntity(EntityMetadata.getDefaultInstance()),
          RemoteEntity(EntityMetadata.getDefaultInstance()),
        ),
      )
    val secondResponse =
      RemoteResponse(
        RemoteResponseMetadata.getDefaultInstance(),
        listOf(
          RemoteEntity(EntityMetadata.getDefaultInstance()),
          RemoteEntity(EntityMetadata.getDefaultInstance()),
        ),
      )

    sendResponse(firstResponse)
    sendResponse(secondResponse)
    sendComplete()

    assertThat(results.await()).containsExactly(firstResponse, secondResponse).inOrder()

    stub.verifyDeathRecipientLinkedAndUnlinked()
  }

  @Test
  fun serve_flowEndsWithException_whenOnErrorTriggered(): Unit = runBlocking {
    val transport = AidlTransport(connector)
    connectionState.emit(State.Connected(stub))
    val error =
      RemoteError(
        RemoteErrorMetadata.newBuilder()
          .setErrorType(RemoteErrorMetadata.Type.POLICY_VIOLATION)
          .setMessage("Policy was violated!")
          .build()
      )

    // Start a request, asynchronously.
    val results = async {
      val caught = assertFailsWith<RemoteError> { transport.serve(request).toList() }
      assertThat(caught).isEqualTo(error)
    }

    // Await the call.
    serveCall.first { it != null }

    sendError(error)

    // Await the failure of the flow.
    results.await()

    stub.verifyDeathRecipientLinkedAndUnlinked()
  }

  @Test
  fun serve_cancellingFlowCollection_causesCancellationSignalToBeCalled(): Unit = runBlocking {
    val transport = AidlTransport(connector)
    connectionState.emit(State.Connected(stub))
    val job = launch { transport.serve(request).toList() }

    // Await the call.
    serveCall.first { it != null }

    val signalCalledDeferred = provideCancellationSignal()

    // Cancel the job, this should cancel the flow.
    job.cancel()

    // The cancellationSignal callback should get called, and when it does - the deferred will be
    // completed.
    signalCalledDeferred.await()

    stub.verifyDeathRecipientLinkedAndUnlinked()
  }

  @Test
  fun serve_throwsDeadObjectException_causesCancellationWithConnectionLost(): Unit = runBlocking {
    val transport = AidlTransport(connector)
    val iRemote = IRemoteStub { _, _ -> throw DeadObjectException() }
    connectionState.emit(State.Connected(iRemote))

    val e = assertFailsWith<RemoteError> { transport.serve(request).toList() }
    assertThat(e.metadata.errorType).isEqualTo(RemoteErrorMetadata.Type.UNKNOWN)
    assertThat(e.extras["ORIGINAL_TYPE"]).isEqualTo(DeadObjectException::class.java.name)
    assertThat(e.extras["ORIGINAL_MESSAGE"]).isNull()
    iRemote.verifyDeathRecipientLinkedAndUnlinked()
  }

  private fun sendResponse(response: RemoteResponse) {
    serveCall.value?.second?.onData(response)
  }

  private fun sendError(error: RemoteError) {
    serveCall.value?.second?.onError(error)
  }

  private fun sendComplete() {
    serveCall.value?.second?.onComplete()
  }

  /** Returns a [Deferred] which will be completed when/if the cancellation signal is used. */
  @Suppress("DeferredIsResult")
  private fun provideCancellationSignal(): Deferred<Unit> {
    val result = CompletableDeferred<Unit>()
    val signal =
      object : ICancellationSignal.Stub() {
        override fun cancel() {
          result.complete(Unit)
        }
      }
    serveCall.value?.second?.provideCancellationSignal(signal)
    return result
  }

  private open class IRemoteStub(val serveImpl: (RemoteRequest?, IResponseCallback?) -> Unit) :
    IRemote.Stub() {
    private val spyBinder by lazy<IBinder> { spy(super.asBinder()) }

    override fun serve(request: RemoteRequest?, callback: IResponseCallback?) {
      serveImpl(request, callback)
    }

    override fun asBinder(): IBinder = spyBinder

    fun verifyDeathRecipientLinkedAndUnlinked() {
      val deathCaptor = argumentCaptor<IBinder.DeathRecipient>()
      verify(spyBinder).linkToDeath(deathCaptor.capture(), eq(0))
      verify(spyBinder).unlinkToDeath(eq(deathCaptor.firstValue), eq(0))
    }
  }
}
