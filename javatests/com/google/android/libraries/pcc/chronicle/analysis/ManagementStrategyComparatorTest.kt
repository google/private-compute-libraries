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
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManagementStrategyComparatorTest {
  @Test
  fun compare_equals_returnsZero() {
    STRATEGIES_IN_ORDER.forEach {
      assertThat(ManagementStrategyComparator.compare(it, it)).isEqualTo(0)
    }
  }

  @Test
  fun compare_aNull_returnsOne() {
    STRATEGIES_IN_ORDER.forEach {
      if (it == null) return@forEach
      assertThat(ManagementStrategyComparator.compare(null, it)).isEqualTo(1)
    }
  }

  @Test
  fun compare_bNull_returnsNegativeOne() {
    STRATEGIES_IN_ORDER.forEach {
      if (it == null) return@forEach
      assertThat(ManagementStrategyComparator.compare(it, null)).isEqualTo(-1)
    }
  }

  @Test
  fun compare_aPassThru_returnsNegativeOne() {
    STRATEGIES_IN_ORDER.forEach {
      if (it == ManagementStrategy.PassThru) return@forEach
      assertThat(ManagementStrategyComparator.compare(ManagementStrategy.PassThru, it))
        .isEqualTo(-1)
    }
  }

  @Test
  fun compare_bPassThru_returnsNegativeOne() {
    STRATEGIES_IN_ORDER.forEach {
      if (it == ManagementStrategy.PassThru) return@forEach
      assertThat(ManagementStrategyComparator.compare(it, ManagementStrategy.PassThru))
        .isEqualTo(1)
    }
  }

  @Test
  fun compare_sortsCorrectly() {
    val shuffled = STRATEGIES_IN_ORDER.shuffled()

    assertThat(shuffled.sortedWith(ManagementStrategyComparator))
      .containsExactlyElementsIn(STRATEGIES_IN_ORDER).inOrder()
  }

  companion object {
    private val STRATEGIES_IN_ORDER = listOf(
      ManagementStrategy.PassThru,
      ManagementStrategy.Stored(true, StorageMedia.MEMORY, Duration.ofMillis(15)),
      ManagementStrategy.Stored(true, StorageMedia.MEMORY, Duration.ofDays(15)),
      ManagementStrategy.Stored(false, StorageMedia.MEMORY, Duration.ofMillis(15)),
      ManagementStrategy.Stored(false, StorageMedia.MEMORY, Duration.ofDays(15)),
      ManagementStrategy.Stored(true, StorageMedia.LOCAL_DISK, Duration.ofMillis(15)),
      ManagementStrategy.Stored(true, StorageMedia.LOCAL_DISK, Duration.ofDays(15)),
      ManagementStrategy.Stored(false, StorageMedia.LOCAL_DISK, Duration.ofMillis(15)),
      ManagementStrategy.Stored(false, StorageMedia.LOCAL_DISK, Duration.ofDays(15)),
      ManagementStrategy.Stored(true, StorageMedia.REMOTE_DISK, Duration.ofMillis(15)),
      ManagementStrategy.Stored(true, StorageMedia.REMOTE_DISK, Duration.ofDays(15)),
      ManagementStrategy.Stored(false, StorageMedia.REMOTE_DISK, Duration.ofMillis(15)),
      ManagementStrategy.Stored(false, StorageMedia.REMOTE_DISK, Duration.ofDays(15)),
      null,
    )
  }
}
