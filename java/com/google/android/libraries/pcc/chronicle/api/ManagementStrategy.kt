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

import java.time.Duration

/**
 * Declares the strategy a given [ConnectionProvider] uses to manage data provided via the
 * [Connection]s it supports.
 */
sealed class ManagementStrategy {
  /**
   * Declares that the data accessed via [Connection]s governed by [PassThru] are not held in memory
   * or stored on disk but simply passed from writer to reader.
   *
   * Common use-case example: data processing pipelines.
   */
  object PassThru : ManagementStrategy()

  /** Declares that the data accessed via [Connection]s are held, and how they're held. */
  data class Stored(
    /** Whether or not the data is encrypted at rest. */
    val encrypted: Boolean,
    /** The storage location. */
    val media: StorageMedia,
    /** The maximum allowable age of data being stored. */
    val ttl: Duration?,
    /** Connected deletion triggers for this data. */
    val deletionTriggers: Set<DeletionTrigger> = emptySet(),
  ) : ManagementStrategy()
}

/**
 * General type of storage technology being used to manage data.
 *
 * The value of [danger] implies the risk associated with storing data using the given
 * [StorageMedia], while the ordinal value of the enum is related to the [danger], they should not
 * be used interchangeably.
 */
enum class StorageMedia(val danger: Int) {
  /** The data is not persisted, and is only held in memory for at most: the life of the process. */
  MEMORY(0),

  /** The data is stored local to the device, persisted to disk. */
  LOCAL_DISK(1),

  /** The data is persisted, but to an external device. */
  REMOTE_DISK(2)
}

/**
 * A helper method to return the TTL for a [ManagementStrategy].
 *
 * If the [ManagementStrategy] returns a null TTL, the provided `default` value will be returned.
 * The `default` parameter itself defaults to [Duration.ZERO].
 */
fun ManagementStrategy.ttl(default: Duration = Duration.ZERO): Duration {
  return when (this) {
    ManagementStrategy.PassThru -> Duration.ZERO
    is ManagementStrategy.Stored -> ttl ?: default
  }
}

/** A helper method that returns whether or not a [ManagementStrategy] is persisted to disk. */
fun ManagementStrategy.isPersisted(): Boolean {
  return when (this) {
    ManagementStrategy.PassThru -> false
    is ManagementStrategy.Stored -> this.media != StorageMedia.MEMORY
  }
}
