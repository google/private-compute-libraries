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

package com.google.android.libraries.pcc.chronicle.storage.datacache.impl

import android.content.LocusId
import android.os.Build
import android.view.contentcapture.DataRemovalRequest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toProtoTimestamp
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage
import com.google.android.libraries.pcc.chronicle.storage.datacache.put
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import java.time.Instant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(minSdk = Build.VERSION_CODES.R)
class DataCacheStorageDataRemovalDownstreamListenerTest {
  private val defaultDataRemovalRequest =
    DataRemovalRequest.Builder().addLocusId(LocusId("unused"), /* flags = */ 0).build()

  private lateinit var dataCacheStorage: DataCacheStorage

  @Before
  fun setUp() {
    // Previous test clean-up
    dataCacheStorage = DataCacheStorageImpl(TimeSource.system())

    // Current test set-up
    dataCacheStorage.registerDataType(Data::class.java, maxSize = 10, ttl = Duration.ofHours(1))
    dataCacheStorage.put(WrappedEntity(metadataWithCreatedAtNow(), Data()))
  }

  @Test
  fun onDataRemoval_dataIsRemoved() {
    // TODO(b/232848132): Don't just delete everything.
    DataCacheStorageDataRemovalDownstreamListener(dataCacheStorage)
      .onDataRemoval(defaultDataRemovalRequest)
    assertThat(dataCacheStorage.all(Data::class.java)).isEmpty()
  }

  companion object {
    class Data

    private fun metadataWithCreatedAtNow() =
      EntityMetadata.newBuilder().setCreated(Instant.now().toProtoTimestamp()).build()
  }
}
