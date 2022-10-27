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
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Empty
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteRequestMetadataExtTest {
  @Test
  fun isReadRequest_storeOps() {
    val builder = RemoteRequestMetadata.newBuilder()
      .setUsageType("Testing")
    val storeReqBuilder = StoreRequest.newBuilder()
      .setDataTypeName("Foo")

    storeReqBuilder.count = Empty.getDefaultInstance()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isTrue()

    storeReqBuilder.fetchAll = Empty.getDefaultInstance()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isTrue()

    storeReqBuilder.fetchById =
      StoreRequest.IdentifierList.newBuilder().addAllIds(listOf("a", "b", "c")).build()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isTrue()

    storeReqBuilder.deleteAll = Empty.getDefaultInstance()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isFalse()

    storeReqBuilder.deleteById =
      StoreRequest.IdentifierList.newBuilder().addAllIds(listOf("a", "b", "c")).build()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isFalse()

    storeReqBuilder.create = Empty.getDefaultInstance()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isFalse()

    storeReqBuilder.update = Empty.getDefaultInstance()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isFalse()

    storeReqBuilder.clearOperation()
    assertThat(builder.setStore(storeReqBuilder).build().isReadRequest).isFalse()
  }

  @Test
  fun isReadRequest_streamOps() {
    val builder = RemoteRequestMetadata.newBuilder()
      .setUsageType("Testing")
    val streamReqBuilder = StreamRequest.newBuilder()
      .setDataTypeName("Foo")

    streamReqBuilder.operation = StreamRequest.Operation.SUBSCRIBE
    assertThat(builder.setStream(streamReqBuilder).build().isReadRequest).isTrue()

    streamReqBuilder.operation = StreamRequest.Operation.PUBLISH
    assertThat(builder.setStream(streamReqBuilder).build().isReadRequest).isFalse()

    streamReqBuilder.operation = StreamRequest.Operation.UNSPECIFIED
    assertThat(builder.setStream(streamReqBuilder).build().isReadRequest).isFalse()

    streamReqBuilder.operationValue = 42
    assertThat(builder.setStream(streamReqBuilder).build().isReadRequest).isFalse()
  }

  @Test
  fun isReadRquest_computeOp() {
    val builder = RemoteRequestMetadata.newBuilder()
      .setUsageType("Testing")
    val computeRequestBuilder = ComputeRequest.newBuilder()
      .setResultDataTypeName("Foo")
      .addAllParameterDataTypeNames(listOf("Bar"))
      .setMethodId(ComputeRequest.MethodId.MOIRAI_CLASSIFY)

    assertThat(builder.setCompute(computeRequestBuilder).build().isReadRequest).isTrue()
  }

  @Test
  fun isReadRequest_noRequestType() {
    val builder = RemoteRequestMetadata.newBuilder()
      .setUsageType("Testing")

    assertThat(builder.build().isReadRequest).isFalse()
  }
}
