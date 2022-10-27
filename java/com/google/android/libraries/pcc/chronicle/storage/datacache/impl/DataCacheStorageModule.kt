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

import com.google.android.libraries.pcc.chronicle.api.ChronicleAnalyticsClient
import com.google.android.libraries.pcc.chronicle.api.DataRemovalDownstreamListener
import com.google.android.libraries.pcc.chronicle.api.DataRemovalRequestListener
import com.google.android.libraries.pcc.chronicle.api.PackageDeletionListener
import com.google.android.libraries.pcc.chronicle.api.flags.FlagsReader
import com.google.android.libraries.pcc.chronicle.storage.datacache.DataCacheStorage
import com.google.android.libraries.pcc.chronicle.util.TimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/** A module provides [DataCacheStorage]. */
@Module
@InstallIn(SingletonComponent::class)
internal object DataCacheStorageModule {
  @Provides
  @Singleton
  fun provideDataCacheStorage(): DataCacheStorage = DataCacheStorageImpl(TimeSource.system())

  @Provides
  @IntoSet
  @Singleton
  fun providePackageDeletionListener(storage: DataCacheStorage): PackageDeletionListener =
    // Note: the only client using DataCacheStorage today is PECAN, so we provide that
    // deletion client name here. In the future, if different analytics clients want to use this
    // storage, we may need to provide qualified variants with their own ChronicleAnalyticsClient
    // value.
    DataCacheStoragePackageDeletionListener(ChronicleAnalyticsClient.PECAN, storage)

  @Provides
  @Singleton
  fun provideDataRemovalRequestListener(
    storage: DataCacheStorage,
    downstreamListeners: Set<@JvmSuppressWildcards DataRemovalDownstreamListener>,
    flags: FlagsReader,
  ): DataRemovalRequestListener =
    DataCacheStorageDataRemovalRequestListener(storage, downstreamListeners, flags)

  /**
   * Provides at least one (no op) DataRemovalDownstreamListener to satisfy the
   * [provideDataRemovalRequestListener] method above.
   */
  @Provides
  @IntoSet
  fun provideNoOpDataRemovalDownstreamListener(): DataRemovalDownstreamListener =
    DataRemovalDownstreamListener {}
}
