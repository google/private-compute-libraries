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

package com.google.android.libraries.pcc.chronicle.analysis

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.dataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.builder.target
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilsTest {
  @Test
  fun retentionsAsManagementStrategies_zeroMaxAgeMs_returnsPassThru() {
    val target = target(FOO_DTD, Duration.ZERO)

    assertThat(target.retentionsAsManagementStrategies())
      .containsExactly(ManagementStrategy.PassThru)
  }

  @Test
  fun retentionsAsManagementStrategies_mapsToStrategies() {
    val ttl = Duration.ofMinutes(5)
    val target =
      target(FOO_DTD, ttl) {
        retention(StorageMedium.DISK, true)
        retention(StorageMedium.RAM, false)
      }

    assertThat(target.retentionsAsManagementStrategies())
      .containsExactly(
        ManagementStrategy.Stored(false, StorageMedia.MEMORY, ttl),
        ManagementStrategy.Stored(true, StorageMedia.LOCAL_DISK, ttl),
      )
  }

  @Test
  fun mostRestrained_returnsMostRestrictive() {
    val ttl = Duration.ofMinutes(5)
    val target =
      target(FOO_DTD, ttl) {
        retention(StorageMedium.DISK, true)
        retention(StorageMedium.RAM, false)
      }

    val max = target.retentionsAsManagementStrategies().mostRestrained()

    assertThat(max).isEqualTo(ManagementStrategy.Stored(false, StorageMedia.MEMORY, ttl))
  }

  companion object {
    private val FOO_DTD = dataTypeDescriptor("Foo", Unit::class)
  }
}
