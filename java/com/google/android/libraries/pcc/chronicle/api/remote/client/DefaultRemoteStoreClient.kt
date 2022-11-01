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
import com.google.android.libraries.pcc.chronicle.api.remote.StoreRequest
import com.google.android.libraries.pcc.chronicle.api.remote.serialization.Serializer
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.protobuf.Empty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

/** Default implementation of [RemoteStoreClient]. */
class DefaultRemoteStoreClient<T : Any>(
  private val dataTypeName: String,
  serializer: Serializer<T>,
  private val transport: Transport
) : BaseRemoteClient<T>(serializer), RemoteStoreClient<T> {
  override suspend fun count(policy: Policy?): Int {
    logcat.v("Store: count()")
    val request =
      RemoteRequest(buildStoreRequestMetadata(policy) { count = Empty.getDefaultInstance() })
    return transport.serve(request).first().metadata.count
  }

  override fun fetchAll(policy: Policy?): Flow<WrappedEntity<T>> {
    logcat.v("Store: fetchAll()")
    val request =
      RemoteRequest(buildStoreRequestMetadata(policy) { fetchAll = Empty.getDefaultInstance() })
    return transport.serveAsWrappedEntityFlow(request)
  }

  override fun fetchById(policy: Policy?, ids: List<String>): Flow<WrappedEntity<T>> {
    logcat.v("Store: fetchById(#ids: %d)", ids.size)
    val request =
      RemoteRequest(
        buildStoreRequestMetadata(policy) {
          fetchById = StoreRequest.IdentifierList.newBuilder().addAllIds(ids).build()
        }
      )
    return transport.serveAsWrappedEntityFlow(request)
  }

  override suspend fun deleteAll(policy: Policy?) {
    logcat.v("Store: deleteByAll()")
    val request =
      RemoteRequest(buildStoreRequestMetadata(policy) { deleteAll = Empty.getDefaultInstance() })
    transport.serve(request).collect()
  }

  override suspend fun deleteById(policy: Policy?, ids: List<String>) {
    logcat.v("Store: deleteById(#ids: %d)", ids.size)
    val request =
      RemoteRequest(
        buildStoreRequestMetadata(policy) {
          deleteById = StoreRequest.IdentifierList.newBuilder().addAllIds(ids).build()
        }
      )
    transport.serve(request).collect()
  }

  override suspend fun create(policy: Policy?, entities: List<WrappedEntity<T>>) {
    logcat.v("Store: create(#entities: %d)", entities.size)
    val request =
      RemoteRequest(
        metadata = buildStoreRequestMetadata(policy) { create = Empty.getDefaultInstance() },
        entities = entities.map(serializer::serialize)
      )
    transport.serve(request).collect()
  }

  override suspend fun update(policy: Policy?, entities: List<WrappedEntity<T>>) {
    logcat.v("Store: update(#entities: %d)", entities.size)
    val request =
      RemoteRequest(
        metadata = buildStoreRequestMetadata(policy) { update = Empty.getDefaultInstance() },
        entities = entities.map(serializer::serialize)
      )
    transport.serve(request).collect()
  }

  private fun buildStoreRequestMetadata(
    policy: Policy?,
    block: StoreRequest.Builder.() -> Unit,
  ): RemoteRequestMetadata {
    return super.buildRequestMetadata(policy) {
      store = StoreRequest.newBuilder().setDataTypeName(dataTypeName).apply(block).build()
    }
  }
}
