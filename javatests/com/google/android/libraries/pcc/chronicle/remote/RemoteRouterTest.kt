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

import android.os.IBinder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.api.remote.ICancellationSignal
import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteEntity
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteErrorMetadata.Type
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteStreamServer
import com.google.android.libraries.pcc.chronicle.remote.handler.RemoteServerHandler
import com.google.android.libraries.pcc.chronicle.remote.handler.RemoteServerHandlerFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RemoteRouterTest {
  private lateinit var scope: CoroutineScope
  private lateinit var router: RemoteRouter
  private val context = mock<RemoteContext>()
  private val policyChecker = mock<RemotePolicyChecker>()
  private val handler = mock<RemoteServerHandler>()
  private val handlerFactory =
    mock<RemoteServerHandlerFactory> { on { buildServerHandler(any(), any()) } doReturn handler }
  private val clientDetailsProvider =
    mock<ClientDetailsProvider> {
      on { getClientDetails() } doReturn
        ClientDetails(1337, ClientDetails.IsolationType.DEFAULT_PROCESS)
    }

  private val entities = listOf(RemoteEntity(), RemoteEntity(), RemoteEntity())
  private val request = RemoteRequest(RemoteRequestMetadata.getDefaultInstance(), entities)

  @Before
  fun setUp() {
    scope = CoroutineScope(SupervisorJob())
    router = RemoteRouter(scope, context, policyChecker, handlerFactory, clientDetailsProvider)
  }

  @After
  fun tearDown() {
    // The scope should always still be active after a test.
    assertThat(scope.isActive).isTrue()
    scope.cancel()
  }

  @Test
  fun serve_nullRequestOrNullCallback_doesNothing() {
    val request = RemoteRequest(RemoteRequestMetadata.getDefaultInstance())
    val callback = mock<IResponseCallback.Stub>()

    router.serve(request = null, callback = callback)

    verifyNoMoreInteractions(callback)
    verifyNoMoreInteractions(context)
    verifyNoMoreInteractions(policyChecker)
    verifyNoMoreInteractions(handlerFactory)

    router.serve(request = request, callback = null)

    verifyNoMoreInteractions(callback)
    verifyNoMoreInteractions(context)
    verifyNoMoreInteractions(policyChecker)
    verifyNoMoreInteractions(handlerFactory)
  }

  @Test
  fun serve_serverNotFound_callsCallbackOnError(): Unit = runBlocking {
    whenever(context.findServer(any())).thenReturn(null)

    val errorDeferred = CompletableDeferred<RemoteError>()
    val callback = spy(OnErrorCallback(errorDeferred::complete))

    router.serve(request, callback)

    // Death recipient should've been linked.
    callback.linkedToDeath.await()

    val e = errorDeferred.await()
    assertThat(e.metadata.errorType).isEqualTo(Type.UNSUPPORTED)
    assertThat(e)
      .hasMessageThat()
      .contains("Server not found for request with metadata: ${request.metadata}")

    // Death recipient should've been unlinked.
    callback.unlinkedToDeath.await()

    verify(callback, times(1)).provideCancellationSignal(any())
    verify(callback, times(1)).onError(any())
    verify(callback, never()).onComplete()
  }

  @Test
  fun serve_exceptionThrownFromPolicyChecker_callsCallbackOnError(): Unit = runBlocking {
    val server = mock<RemoteStreamServer<*>>()
    whenever(context.findServer(any())).thenReturn(server)
    whenever(policyChecker.checkAndGetPolicyOrThrow(any(), any(), any())).then {
      throw IllegalStateException()
    }

    val errorDeferred = CompletableDeferred<RemoteError>()
    val callback = spy(OnErrorCallback(errorDeferred::complete))

    router.serve(request, callback)

    // Death recipient should've been linked.
    callback.linkedToDeath.await()

    val e = errorDeferred.await()
    assertThat(e.metadata.errorType).isEqualTo(Type.UNKNOWN)
    assertThat(e).hasMessageThat().contains(IllegalStateException::class.java.name)

    // Death recipient should've been unlinked.
    callback.unlinkedToDeath.await()

    verify(callback, times(1)).provideCancellationSignal(any())
    verify(callback, times(1)).onError(any())
    verify(callback, never()).onComplete()
  }

  @Test
  fun serve_exceptionThrownFromHandlerFactory_callsCallbackOnError(): Unit = runBlocking {
    val server = mock<RemoteStreamServer<*>>()
    whenever(context.findServer(any())).thenReturn(server)
    whenever(policyChecker.checkAndGetPolicyOrThrow(any(), any(), any())).thenReturn(POLICY)
    whenever(handlerFactory.buildServerHandler(any(), any())).then {
      throw IllegalArgumentException()
    }

    val errorDeferred = CompletableDeferred<RemoteError>()
    val callback = spy(OnErrorCallback(errorDeferred::complete))

    router.serve(request, callback)

    // Death recipient should've been linked.
    callback.linkedToDeath.await()

    val e = errorDeferred.await()
    assertThat(e.metadata.errorType).isEqualTo(Type.UNKNOWN)
    assertThat(e).hasMessageThat().contains(IllegalArgumentException::class.java.name)

    // Death recipient should've been unlinked.
    callback.unlinkedToDeath.await()

    verify(callback, times(1)).provideCancellationSignal(any())
    verify(callback, times(1)).onError(any())
    verify(callback, never()).onComplete()
  }

  @Test
  fun serve_callsHandlerHandle_beforeCallingComplete(): Unit = runBlocking {
    val onCompleteCalled = CompletableDeferred<Unit>()
    val handlerCalled = CompletableDeferred<Unit>()
    val handlerShouldProceed = CompletableDeferred<Unit>()
    val callback = spy(OnCompleteCallback { onCompleteCalled.complete(Unit) })
    val handler =
      object : RemoteServerHandler {
        override suspend fun handle(
          policy: Policy?,
          input: List<RemoteEntity>,
          callback: IResponseCallback,
        ) {
          assertThat(policy).isEqualTo(POLICY)
          assertThat(input).isEqualTo(entities)
          assertThat(callback).isSameInstanceAs(callback)
          handlerCalled.complete(Unit)
          handlerShouldProceed.await()
        }
      }
    whenever(context.findServer(any())).thenReturn(mock())
    whenever(policyChecker.checkAndGetPolicyOrThrow(any(), any(), any())).thenReturn(POLICY)
    whenever(handlerFactory.buildServerHandler(any(), any())).thenReturn(handler)

    router.serve(request, callback)

    // Death recipient should've been linked.
    callback.linkedToDeath.await()

    handlerCalled.await()
    handlerShouldProceed.complete(Unit)
    onCompleteCalled.await()

    // Death recipient should've been unlinked.
    callback.unlinkedToDeath.await()

    verify(callback, times(1)).provideCancellationSignal(any())
    verify(callback, times(1)).onComplete()
    verify(callback, never()).onError(any())
  }

  @Test
  fun serve_callsHandlerHandle_exceptionThrown_goesToCallbackOnError(): Unit = runBlocking {
    val onError = CompletableDeferred<RemoteError>()
    val callback = spy(OnErrorCallback(onError::complete))
    val handler =
      object : RemoteServerHandler {
        override suspend fun handle(
          policy: Policy?,
          input: List<RemoteEntity>,
          callback: IResponseCallback,
        ): Unit = throw IllegalStateException("Oops")
      }
    whenever(context.findServer(any())).thenReturn(mock())
    whenever(policyChecker.checkAndGetPolicyOrThrow(any(), any(), any())).thenReturn(POLICY)
    whenever(handlerFactory.buildServerHandler(any(), any())).thenReturn(handler)

    router.serve(request, callback)

    // Death recipient should've been linked.
    callback.linkedToDeath.await()

    val error = onError.await()
    assertThat(error.metadata.errorType).isEqualTo(Type.UNKNOWN)
    assertThat(error).hasMessageThat().contains(IllegalStateException::class.java.name)

    // Death recipient should've been unlinked.
    callback.unlinkedToDeath.await()

    verify(callback, times(1)).provideCancellationSignal(any())
    verify(callback, times(1)).onError(any())
    verify(callback, never()).onComplete()
  }

  @Test
  fun serve_cancellationSignal_cancelsOngoingHandlerHandle(): Unit = runBlocking {
    val onComplete = CompletableDeferred<Unit>()
    val onCancellationSignal = CompletableDeferred<ICancellationSignal>()
    val handleCalled = CompletableDeferred<Unit>()
    val callback =
      spy(OnCompleteCallback(onCancellationSignal::complete) { onComplete.complete(Unit) })
    val cancelled = CompletableDeferred<Unit>()
    val handler =
      object : RemoteServerHandler {
        override suspend fun handle(
          policy: Policy?,
          input: List<RemoteEntity>,
          callback: IResponseCallback,
        ): Unit = suspendCancellableCoroutine { continuation ->
          continuation.invokeOnCancellation { cancelled.complete(Unit) }
          handleCalled.complete(Unit)
        }
      }
    whenever(context.findServer(any())).thenReturn(mock())
    whenever(policyChecker.checkAndGetPolicyOrThrow(any(), any(), any())).thenReturn(POLICY)
    whenever(handlerFactory.buildServerHandler(any(), any())).thenReturn(handler)

    router.serve(request, callback)

    // Death recipient should've been linked.
    callback.linkedToDeath.await()

    val signal = onCancellationSignal.await()

    // Wait for the handle to have been used, it should now be suspending its coroutine.
    handleCalled.await()

    // Use the cancellation signal to cause cancellation.
    signal.cancel()

    // Wait for the handle to be notified.
    cancelled.await()

    // On complete should be called
    onComplete.await()

    // Death recipient should've been unlinked.
    callback.unlinkedToDeath.await()

    verify(callback, times(1)).provideCancellationSignal(any())
    verify(callback, times(1)).onComplete()
    verify(callback, never()).onError(any())
  }

  @Test
  fun serve_callbackDeathRecipientTriggered_cancelsOperation() = runBlocking {
    val cancelled = CompletableDeferred<Unit>()
    val handlerCalled = CompletableDeferred<Unit>()
    val handler =
      object : RemoteServerHandler {
        override suspend fun handle(
          policy: Policy?,
          input: List<RemoteEntity>,
          callback: IResponseCallback,
        ): Unit = suspendCancellableCoroutine { continuation ->
          // Suspend the coroutine, but don't resume...
          continuation.invokeOnCancellation { cancelled.complete(Unit) }
          handlerCalled.complete(Unit)
        }
      }
    whenever(context.findServer(any())).thenReturn(mock())
    whenever(policyChecker.checkAndGetPolicyOrThrow(any(), any(), any())).thenReturn(POLICY)
    whenever(handlerFactory.buildServerHandler(any(), any())).thenReturn(handler)

    val callback = OnCompleteCallback {}

    router.serve(request, callback)
    val deathRecipient = callback.linkedToDeath.await()
    handlerCalled.await()

    // Now that the handler is suspending, use the death recipient and wait for the handler to be
    // canceled.
    deathRecipient.binderDied()
    cancelled.await()
  }

  abstract class AbstractIResponseCallback : IResponseCallback.Stub() {
    val linkedToDeath = CompletableDeferred<IBinder.DeathRecipient>()
    val unlinkedToDeath = CompletableDeferred<Unit>()
    private val spyBinder by
      lazy(LazyThreadSafetyMode.NONE) {
        spy(super.asBinder()) {
          on { linkToDeath(any(), eq(0)) } doAnswer
            { invocation ->
              linkedToDeath.complete(invocation.arguments[0] as IBinder.DeathRecipient)
              invocation.callRealMethod()
              Unit
            }
          on { unlinkToDeath(any(), eq(0)) } doAnswer
            { invocation ->
              unlinkedToDeath.complete(Unit)
              invocation.callRealMethod() as Boolean
            }
        }
      }

    override fun onData(data: RemoteResponse) = Unit

    override fun onError(error: RemoteError) = Unit

    override fun onComplete() = Unit

    override fun provideCancellationSignal(signal: ICancellationSignal) = Unit

    override fun asBinder(): IBinder = spyBinder
  }

  open class OnCompleteCallback(
    private val receiveCancellationSignal: (ICancellationSignal) -> Unit = {},
    private val doOnComplete: () -> Unit,
  ) : AbstractIResponseCallback() {
    override fun onComplete() = doOnComplete()

    override fun provideCancellationSignal(signal: ICancellationSignal) =
      receiveCancellationSignal(signal)
  }

  open class OnErrorCallback(private val doOnError: (RemoteError) -> Unit) :
    AbstractIResponseCallback() {
    override fun onError(error: RemoteError) = doOnError(error)
  }

  companion object {
    private val POLICY = policy("MyPolicy", "Testing")
  }
}
