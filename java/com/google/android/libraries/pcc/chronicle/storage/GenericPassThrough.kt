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

package com.google.android.libraries.pcc.chronicle.storage

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/** A listener method that can be registered with [GenericPassThrough]. */
typealias Listener<T> = (T) -> Unit

/** A cancellation method returned from [GenericPassThrough.listen]. */
typealias Cancel = () -> Unit

/**
 * This is simple thread-safe implementation that can be used to implement the functionality
 * of a pass-through-only [ManagedDataType].
 */
class GenericPassThrough<T> {
  private val listeners = atomic(emptySet<Listener<T>>())

  /**
   * Send `item` to all currently registered [Listener]s.
   *
   * The item is not stored; subsequently registered [Listener]s will not receive this item.
   *
   * This method will block until all listeners have been notified.
   */
  fun write(item: T) {
    listeners.value.forEach { it(item) }
  }

  /**
   * Register a new listener for this node.
   *
   * Note that writers will block until all registered [Listener]s have completed.
   *
   * @return an instance of [Cancel] that can be called to unregister the listener.
   */
  fun listen(listener: Listener<T>): Cancel {
    listeners.update {
      it + listener
    }
    return {
      listeners.update {
        it - listener
      }
    }
  }
}
