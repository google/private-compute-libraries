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

package com.google.android.libraries.pcc.chronicle.api.storage

import com.google.protobuf.Timestamp
import java.time.Instant

/** Converts the receiving [Instant] into a [Timestamp] message. */
fun Instant.toProtoTimestamp(): Timestamp =
  Timestamp.newBuilder().setSeconds(epochSecond).setNanos(nano).build()

/** Converts the receiving [Timestamp] message into an [Instant]. */
fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanos.toLong())

/**
 * Shortcut pseudo-constructor to use when building [EntityMetadata] for [WrappedEntity] instances.
 */
fun EntityMetadata(
  id: String?,
  associatedPackageNames: List<String>,
  created: Instant,
  updated: Instant = created,
): EntityMetadata {
  return EntityMetadata.newBuilder()
    .apply {
      id?.let { this.id = id }
      this.created = created.toProtoTimestamp()
      this.updated = updated.toProtoTimestamp()
      addAllAssociatedPackageNames(associatedPackageNames)
    }
    .build()
}

/**
 * Shortcut pseudo-constructor to use when building [EntityMetadata] for [WrappedEntity] instances
 * when only a single associated package name applies.
 */
fun EntityMetadata(
  id: String?,
  associatedPackageName: String,
  created: Instant,
  updated: Instant = created,
): EntityMetadata = EntityMetadata(id, listOf(associatedPackageName), created, updated)

/**
 * Creates a copy of the receiving [EntityMetadata], using the provided [updated] value. If
 * [associatedPackageNames] is non-null, the returned [EntityMetadata] will use those as its value.
 * If null, no changes will be made to the [EntityMetadata.getAssociatedPackageNames] in the copy.
 */
fun EntityMetadata.update(
  updated: Instant,
  associatedPackageNames: List<String>? = null,
): EntityMetadata {
  return toBuilder()
    .apply {
      this.updated = updated.toProtoTimestamp()
      associatedPackageNames?.let {
        this.clearAssociatedPackageNames()
        this.addAllAssociatedPackageNames(it)
      }
    }
    .build()
}

/**
 * Creates a copy of the receiving [EntityMetadata], using the provided [updated] value. If
 * [associatedPackageName] is non-null, the returned [EntityMetadata] will use it as its value. If
 * null, no changes will be made to the [EntityMetadata.getAssociatedPackageNames] in the copy.
 */
fun EntityMetadata.update(
  updated: Instant,
  associatedPackageNames: String? = null,
): EntityMetadata = update(updated, associatedPackageNames?.let { listOf(it) })
