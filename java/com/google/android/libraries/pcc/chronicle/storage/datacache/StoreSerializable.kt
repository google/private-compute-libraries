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

/**
 * Interfaces to support serialization and deserialization of the objects needed to be stored on
 * disk through [DataCacheStorage].
 *
 * The [StoreSerializable] interface should be implemented by the data type class which is stored in
 * [DataCacheStorage].
 *
 * The [Deserializer] interface should be implemented by the companion object for the data type
 * class which implemented [StoreSerializable].
 *
 * Example:
 *   data class Foo(val data : String) : StoreSerializable {
 *     override fun fun toSerializedString(): String {
 *        // Do something and return String.
 *     }
 *
 *     companion objet : Deserializer<Foo>{
 *       fun fromSerializedString(str : String): Foo? {
 *         // Do something and return Foo?.
 *       }
 *     }
 *   }
 */
interface StoreSerializable {
  fun toSerializedString(): String

  interface Deserializer<T> {
    fun fromSerializedString(str: String): T?
  }
}
