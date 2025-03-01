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

package com.google.android.libraries.pcc.chronicle.remote.handler

import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponseMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/**
 * Handles a [Flow] of pages of [WrappedEntities][WrappedEntity] by calling
 * [IResponseCallback.onData] for each page, using the supplied [serializer] to transform each
 * [WrappedEntity] for transmission.
 */
fun <T : Any> Flow<List<WrappedEntity<T>>>.sendEachPage(
  callback: IResponseCallback,
  serializer: Serializer<T>,
): Flow<List<WrappedEntity<T>>> = onEach {
  callback.onData(
    RemoteResponse(
      metadata = RemoteResponseMetadata.newBuilder().build(),
      entities = it.map(serializer::serialize),
    )
  )
}
