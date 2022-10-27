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

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.util.Logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Base [RemoteClient], containing utilities and common functionality for implementations of
 * [RemoteStoreClient], [RemoteComputeClient], and [RemoteStreamClient].
 */
abstract class BaseRemoteClient<T : Any>(protected val serializer: Serializer<T>) :
  RemoteClient<T> {
  /**
   * Constructs [RemoteRequestMetadata] given the [policy], calling the builder [block] in the
   * process.
   */
  protected fun buildRequestMetadata(
    policy: Policy?,
    block: RemoteRequestMetadata.Builder.() -> Unit,
  ): RemoteRequestMetadata {
    return RemoteRequestMetadata.newBuilder()
      .apply {
        // TODO(b/210998515): Change the value passed to the usage type (instead of name).
        policy?.name?.let { usageType = it }
        block()
      }
      .build()
  }

  /**
   * Calls [Transport.serve], and unrolls received [RemoteResponse] instances into individual,
   * deserialized [WrappedEntities][WrappedEntity].
   */
  protected fun Transport.serveAsWrappedEntityFlow(request: RemoteRequest): Flow<WrappedEntity<T>> =
    flow {
      serve(request).collect {
        it.entities.forEach { entity ->
          val deserialized = serializer.deserialize<T>(entity)
          logcat.atVerbose().log("%s: onEntity(%s)", request.metadata.requestTypeCase, deserialized)
          emit(deserialized)
        }
      }
    }

  companion object {
    @JvmStatic protected val logcat = Logcat.clientSide
  }
}
