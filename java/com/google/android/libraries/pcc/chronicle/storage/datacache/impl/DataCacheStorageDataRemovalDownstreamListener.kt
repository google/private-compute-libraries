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

import android.view.contentcapture.DataRemovalRequest
import com.google.android.libraries.pcc.chronicle.api.DataRemovalDownstreamListener
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage
import com.google.android.libraries.pcc.chronicle.util.Logcat

class DataCacheStorageDataRemovalDownstreamListener(
  private val dataCacheStorage: DataCacheStorage
) : DataRemovalDownstreamListener {

  override fun onDataRemoval(request: DataRemovalRequest) {
    // TODO(b/232848132): Don't just delete everything.
    val countRemoved = dataCacheStorage.purgeAllEntities()
    logger.d("Number of items removed from DataCacheStorage: %d.", countRemoved)
  }

  companion object {
    private val logger = Logcat.default
  }
}
