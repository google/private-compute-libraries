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

package com.google.android.libraries.pcc.chronicle.storage.stream.impl

import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.android.libraries.pcc.chronicle.storage.stream.EntityStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Implementation of [EntityStream]. */
class EntityStreamImpl<T : Any> : EntityStream<T> {
  private val flow = MutableSharedFlow<List<WrappedEntity<T>>>(replay = 0)

  override suspend fun publishGroup(group: List<WrappedEntity<T>>) = flow.emit(group)

  override fun subscribeGroups(): Flow<List<WrappedEntity<T>>> = flow.asSharedFlow()
}
