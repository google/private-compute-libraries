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

import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyTarget
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import java.time.Duration

/** Finds the most restrained [ManagementStrategy] from the receiving list. */
fun List<ManagementStrategy>.mostRestrained(): ManagementStrategy {
  // Use minOfWith, because comparators order in ascending order, and our comparator gives
  // more-restrained strategies a negative value (placing them towards the front of the order).
  return minOfWith(ManagementStrategyComparator) { it }
}

/**
 * Analysis-specific comparator for [ManagementStrategies][ManagementStrategy].
 *
 * Applies a lexicographic ordering to the components of a [ManagementStrategy], implying that more
 * restrained strategies come before more liberal strategies. From most significant to least
 * significant, the components are:
 * 1. [StorageMedia], as characterized by that media's [danger][StorageMedia.danger] level.
 * 1. Whether or not the storage will be [encrypted].
 * 1. The [time to live][StorageMedia.ttl].
 *
 * **Note:** [PassThru][ManagementStrategy.PassThru] strategies are considered the most restrained
 * of all, and are interpreted as being zero danger storage (because nothing is stored).
 */
object ManagementStrategyComparator : Comparator<ManagementStrategy?> {
  override fun compare(a: ManagementStrategy?, b: ManagementStrategy?): Int {
    if (a == b) return 0
    if (a == null) return 1
    if (b == null) return -1

    if (a is ManagementStrategy.PassThru) return -1
    if (b is ManagementStrategy.PassThru) return 1

    a as ManagementStrategy.Stored
    b as ManagementStrategy.Stored

    if (a.media != b.media) return a.media.danger - b.media.danger
    if (a.encrypted != b.encrypted) {
      if (a.encrypted) return -1
      return 1
    }
    return (a.ttl ?: Duration.ZERO).compareTo(b.ttl ?: Duration.ZERO)
  }
}

/**
 * Converts a [PolicyTarget's][PolicyTarget] retentions and max age into a list of
 * [ManagementStrategies][ManagementStrategy].
 */
fun PolicyTarget.retentionsAsManagementStrategies(): List<ManagementStrategy> {
  if (maxAgeMs == 0L) return listOf(ManagementStrategy.PassThru)

  return retentions.map {
    ManagementStrategy.Stored(
      it.encryptionRequired,
      when (it.medium) {
        StorageMedium.RAM -> StorageMedia.MEMORY
        StorageMedium.DISK -> StorageMedia.LOCAL_DISK
      },
      Duration.ofMillis(maxAgeMs),
    )
  }
}
