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
import com.google.android.libraries.pcc.chronicle.api.DataRemovalDownstreamListener
import com.google.android.libraries.pcc.chronicle.api.flags.FakeFlagsReader
import com.google.android.libraries.pcc.chronicle.api.flags.Flags
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.api.storage.toProtoTimestamp
import com.google.android.libraries.pcc.chronicle.storage.datacache.put
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import java.time.Instant
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(minSdk = Build.VERSION_CODES.R)
class DataCacheStorageDataRemovalRequestListenerTest {

  @Before
  fun setUp() {
    // Previous test clean-up
    dataCacheStorage.purgeAllEntities()
    assertThat(dataCacheStorage.all(Data::class.java)).isEmpty()

    flags.config.value = Flags(disableChronicleDataRemovalRequestListener = false)

    // Current test set-up
    dataCacheStorage.registerDataType(Data::class.java, maxSize = 10, ttl = Duration.ofHours(1))
    assertTrue("storage failed!") {
      dataCacheStorage.put(WrappedEntity(metadataWithCreatedAtNow(), Data()))
    }
    assertThat(dataCacheStorage.all(Data::class.java)).isNotEmpty()
  }

  @Test
  fun onDataRemovalRequest_dataIsRemoved() {
    // TODO(b/232848132): Don't just delete everything.
    deletionListener().onDataRemovalRequest(defaultDataRemovalRequest)
    assertThat(dataCacheStorage.all(Data::class.java)).isEmpty()
  }

  @Test
  fun disablingListener_dataIsPreserved() {
    flags.config.value = Flags(disableChronicleDataRemovalRequestListener = true)

    deletionListener().onDataRemovalRequest(defaultDataRemovalRequest)
    assertThat(dataCacheStorage.all(Data::class.java)).isNotEmpty()
  }

  @Test
  fun onDataRemovalRequest_downstreamListenersAreTriggered() {
    var listener0CallCount = 0
    var listener1CallCount = 0

    deletionListener(
        setOf(
          DataRemovalDownstreamListener { listener0CallCount++ },
          DataRemovalDownstreamListener { listener1CallCount++ },
        )
      )
      .onDataRemovalRequest(defaultDataRemovalRequest)

    assertThat(listener0CallCount).isEqualTo(1)
    assertThat(listener1CallCount).isEqualTo(1)
  }

  companion object {
    private val dataCacheStorage = DataCacheStorageImpl(TimeSource.system())
    private val flags = FakeFlagsReader(Flags())
    private val defaultDataRemovalRequest =
      DataRemovalRequest.Builder().addLocusId(LocusId("unused"), /* flags= */ 0).build()

    class Data

    private fun metadataWithCreatedAtNow() =
      EntityMetadata.newBuilder().setCreated(Instant.now().toProtoTimestamp()).build()

    private fun deletionListener(
      downstreamListeners: Set<DataRemovalDownstreamListener> = setOf(),
    ) = DataCacheStorageDataRemovalRequestListener(dataCacheStorage, downstreamListeners, flags)
  }
}
