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

import android.view.contentcapture.DataRemovalRequest
import com.google.android.libraries.pcc.chronicle.api.PackageDeletionListener.PackageInstallInfo
import dagger.multibindings.IntoSet

/** A [ChronicleDeletionListener] for Android package deletions. */
interface PackageDeletionListener : ChronicleDeletionListener<PackageInstallInfo> {
  /** Represents a package deleted by the user from the phone. */
  data class PackageInstallInfo(
    /** Name of the newly-deleted package, e.g. "com.google.android.as". */
    val packageName: String,
    /** User ID of the phone's user from which the package was deleted. */
    val userId: Int
  )
}

/**
 * A [ChronicleDeletionListener] for user consent. [deleteFor] will be called when checkbox consent
 * is revoked. [reconcile] will be called with the set of granted consents.
 */
interface UserConsentListener : ChronicleDeletionListener<UserConsentListener.ConsentType> {
  enum class ConsentType {
    /**
     * "Usage and Diagnostics" consent, typically the basis for collecting aggregated system
     * diagnostics data.
     */
    CHECKBOX,
  }
}

/** A [ChronicleDeletionListener] for Android's Locus ID deletions. */
interface DataRemovalRequestListener {
  // TODO(b/232813455): Investigate modifying the return value of
  // ChronicleDeletionListener::deleteFor and ::reconcile so as to be able to extend
  // ChronicleDeletionListener here.
  fun onDataRemovalRequest(request: DataRemovalRequest)
}

/**
 * Chronicle downstream clients can extend this interface to receive a callback when Chronicle
 * receives a [DataRemovalRequest] from Android framework.
 */
fun interface DataRemovalDownstreamListener {
  fun onDataRemoval(request: DataRemovalRequest)
}

/**
 * Used when data should be cleared in response to an event such as a package uninstall. Customers
 * using Chronicle, DO NOT need to wire this up manually. Use this if you have data external to
 * Chronicle that needs to react to an event such as package deletion. Return an [IntoSet] binding
 * for a specific listener such as [UserConsentListener] to register.
 */
interface ChronicleDeletionListener<T> {
  /**
   * Called when Chronicle detects removal of this entity so this listener can remove its linked
   * data. In rare cases this trigger may not fire due to Android limitations. In that case, the
   * [reconcile] method will ensure it the data gets removed.
   */
  suspend fun deleteFor(value: T): PackageDeletionInfo

  /**
   * Called periodically (e.g daily) with an authoritative set of existing entities. For example,
   * the listeners for PackageDeletion will call this periodically with the full set of installed
   * packages. This listener should walk the packages they've stored and remove any entities not in
   * the authoritative list. This allows any missed [deleteFor] triggers to be corrected.
   */
  suspend fun reconcile(fullSet: Set<T>): PackageDeletionInfo

  /** Metadata about the deletion operation conducted on [PackageInstallInfo]. */
  data class PackageDeletionInfo(
    /* Used to annotate which [ChronicleAnalyticsClient] is responsible for deletion */
    val client: ChronicleAnalyticsClient,
    /* Indicates number of entities / rows deleted as a result of trigger / reconcile */
    val numberOfEntitiesDeleted: Int
  )
}
