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

/** Used to describe a [Connection], specifying whether it's a Read or Write connection. */
sealed class ConnectionName<T : Connection> {
  internal abstract val name: Name

  data class Reader<T : Connection>(override val name: Name) : ConnectionName<T>()
  data class Writer<T : Connection>(override val name: Name) : ConnectionName<T>()
}

/**
 * Special cased [ConnectionName]s for remote connections. This allows the remote connections system
 * to call Chronicle.getConnection for policy-checking purposes and bypasses actually getting the
 * connection.
 *
 * **Note** this should not be used generally. Use [ConnectionName] instead.
 */
sealed class ConnectionNameForRemoteConnections<T : Connection> : ConnectionName<T>() {
  // TODO(b/251283239): Remove these two subclasses and their uses after policy checking is
  // separated from getting a connection in Chronicle.
  data class Reader<T : Connection>(override val name: Name) : ConnectionName<T>()
  data class Writer<T : Connection>(override val name: Name) : ConnectionName<T>()
}
