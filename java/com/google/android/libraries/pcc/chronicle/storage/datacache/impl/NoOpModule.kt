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
import com.google.android.libraries.pcc.chronicle.api.DataRemovalRequestListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** For projects/labs that use a [NoOpChronicle], this companion class exists. */
@Module
@InstallIn(SingletonComponent::class)
internal object NoOpModule {
  @Provides
  @Singleton
  fun provideDataRemovalRequestListener(): DataRemovalRequestListener =
    object : DataRemovalRequestListener {
      override fun onDataRemovalRequest(request: DataRemovalRequest) {}
    }
}
