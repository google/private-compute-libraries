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

package com.google.android.libraries.pcc.chronicle.api

import android.content.LocusId
import android.os.Build
import android.view.contentcapture.DataRemovalRequest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.flags.FakeFlagsReader
import com.google.android.libraries.pcc.chronicle.api.flags.Flags
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(minSdk = Build.VERSION_CODES.R)
class DefaultDataRemovalRequestListenerTest {
  private val defaultDataRemovalRequest =
    DataRemovalRequest.Builder().addLocusId(LocusId("unused"), /* flags= */ 0).build()

  private var listener0CallCount = 0
  private var listener1CallCount = 0
  private val deletionListener =
    DefaultDataRemovalRequestListener(
      setOf(
        DataRemovalDownstreamListener { listener0CallCount++ },
        DataRemovalDownstreamListener { listener1CallCount++ },
      ),
      flags
    )

  @Before
  fun setUp() {
    listener0CallCount = 0
    listener1CallCount = 0
  }

  @Test
  fun disablingListener_downstreamListenersAreNotTriggered() {
    flags.config.value = Flags(disableChronicleDataRemovalRequestListener = true)

    deletionListener.onDataRemovalRequest(defaultDataRemovalRequest)

    assertThat(listener0CallCount).isEqualTo(0)
    assertThat(listener1CallCount).isEqualTo(0)
  }

  @Test
  fun onDataRemovalRequest_downstreamListenersAreTriggered() {
    flags.config.value = Flags(disableChronicleDataRemovalRequestListener = false)

    deletionListener.onDataRemovalRequest(defaultDataRemovalRequest)

    assertThat(listener0CallCount).isEqualTo(1)
    assertThat(listener1CallCount).isEqualTo(1)
  }

  companion object {
    private val flags = FakeFlagsReader(Flags())
  }
}
