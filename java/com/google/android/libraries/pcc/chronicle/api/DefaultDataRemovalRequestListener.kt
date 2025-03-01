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
import com.google.android.libraries.pcc.chronicle.api.flags.FlagsReader
import com.google.android.libraries.pcc.chronicle.util.Logcat

/**
 * This class's method will be invoked when Android framework reacts to a request to
 * [ContentCaptureManager::removeData]. It will trigger downstreamListener's
 * [DataRemovalDownstreamListener.onDataRemoval] method serially, and on the same thread on which
 * [DefaultDataRemovalRequestListener.onDataRemovalRequest] is called.
 */
class DefaultDataRemovalRequestListener(
  private val downstreamListeners: Set<DataRemovalDownstreamListener>,
  private val flags: FlagsReader,
) : DataRemovalRequestListener {
  private val logger = Logcat.default

  override fun onDataRemovalRequest(request: DataRemovalRequest) {
    if (flags.config.value.disableChronicleDataRemovalRequestListener) return

    logger.d("Handling data removal request.")
    downstreamListeners.forEach { it.onDataRemoval(request) }
  }
}
