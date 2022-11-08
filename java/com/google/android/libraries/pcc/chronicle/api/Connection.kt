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

/**
 * Basis for all types of [Connections][Connection] exposed to [ProcessorNodes][ProcessorNode] by
 * [ConnectionProviders][ConnectionProvider].
 *
 * Do not extend directly, instead: extend from [ReadConnection], [WriteConnection], or something
 * more concrete than them.
 */
interface Connection {
  companion object {
    /**
     * Factory/convenience method to create a [ConnectionName] from a Class that extends from
     * [ReadConnection] or [WriteConnection]. The result is either [ConnectionName.Reader] or
     * [ConnectionName.Writer].
     */
    @JvmStatic
    fun <T : Connection> connectionName(clazz: Class<T>) =
      if (clazz.isReadConnection) {
        ReadConnection.connectionName(clazz)
      } else {
        WriteConnection.connectionName(clazz)
      }
  }
}
