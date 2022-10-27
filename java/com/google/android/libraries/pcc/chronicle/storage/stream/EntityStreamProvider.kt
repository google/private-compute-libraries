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

package com.google.android.libraries.pcc.chronicle.storage.stream

import javax.annotation.concurrent.ThreadSafe
import kotlin.reflect.KClass

/**
 * Defines a class intended to manage multiple entity streams, organized by the [KClass] of the
 * entity type they stream.
 */
@ThreadSafe
interface EntityStreamProvider {
  /**
   * Gets a reference to an [EntityStream], keyed by the class of [T].
   *
   * If a stream doesn't yet exist for that class, one will be created.
   */
  fun <T : Any> getStream(cls: KClass<out T>): EntityStream<T>
}

/**
 * Gets a reference to an [EntityStream], keyed by the class of [T].
 *
 * If a stream doesn't yet exist for that class, one will be created.
 */
inline fun <reified T : Any> EntityStreamProvider.getStream(): EntityStream<T> = getStream(T::class)
