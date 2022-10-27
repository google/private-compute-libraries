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

package com.google.android.libraries.pcc.chronicle.storage.datacache

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionProvider
import com.google.android.libraries.pcc.chronicle.api.DataType
import com.google.android.libraries.pcc.chronicle.api.ManagedDataType

/**
 * An abstract implementation of a [ConnectionProvider] that supports managing [connections]
 * [Connection] to an underlying [ManagedDataCache] storage.
 */
abstract class ManagedDataCacheConnectionProvider<T>(
  private val cache: ManagedDataCache<T>,
  private val connectionTypes: Set<Class<out Connection>>
) : ConnectionProvider {
  final override val dataType: DataType =
    ManagedDataType(cache.dataTypeDescriptor, cache.managementStrategy, connectionTypes)
}
