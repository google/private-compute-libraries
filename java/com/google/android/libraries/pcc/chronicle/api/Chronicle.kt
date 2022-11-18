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

import com.google.android.libraries.pcc.chronicle.api.error.ChronicleError
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import kotlin.reflect.KClass

/** Chronicle is the primary entry point for features when accessing or manipulating data. */
interface Chronicle {
  /**
   * Checks policy adherence of the [ConnectionRequest.requester] and the data being requested.
   * @param isForReading true if the policy will be used in conjunction with a [ReadConnection]
   */
  fun checkPolicy(
    dataTypeName: String,
    policy: Policy?,
    isForReading: Boolean,
    requester: ProcessorNode
  ): Result<Unit>

  /**
   * Returns the [ConnectionTypes] associated with the provided [dataTypeClass] which are available
   * via [getConnection].
   */
  fun getAvailableConnectionTypes(dataTypeClass: KClass<*>): ConnectionTypes

  /**
   * Returns the [ConnectionTypes] associated with the provided [dataTypeClass] which are available
   * via [getConnection].
   */
  fun getAvailableConnectionTypes(dataTypeClass: Class<*>): ConnectionTypes =
    getAvailableConnectionTypes(dataTypeClass.kotlin)

  /**
   * Returns a [Connection] of type [T] after checking policy adherence of the
   * [ConnectionRequest.requester] and the data being requested.
   */
  fun <T : Connection> getConnection(request: ConnectionRequest<T>): ConnectionResult<T>

  /**
   * A convenience method which calls [getConnection], returning `null` for
   * [ConnectionResult.Failure] results. An optional [onError] parameter will be called with the
   * failure result.
   */
  fun <T : Connection> getConnectionOrNull(
    request: ConnectionRequest<T>,
    onError: (ChronicleError) -> Unit = {}
  ): T? {
    return when (val result = getConnection(request)) {
      is ConnectionResult.Success<T> -> result.connection
      is ConnectionResult.Failure<T> -> {
        onError(result.error)
        null
      }
    }
  }

  /**
   * A convenience method which calls [getConnection], returning `null` for
   * [ConnectionResult.Failure] results. If a failure occurs, there will be no information provided
   * about the failure.
   */
  fun <T : Connection> getConnectionOrNull(
    request: ConnectionRequest<T>,
  ): T? = getConnectionOrNull(request) {}

  /**
   * A convenience method which calls [getConnection], and throws [ChronicleError] for
   * [ConnectionResult.Failure] results.
   */
  fun <T : Connection> getConnectionOrThrow(request: ConnectionRequest<T>): T {
    return when (val result = getConnection(request)) {
      is ConnectionResult.Success<T> -> result.connection
      is ConnectionResult.Failure<T> -> throw (result.error)
    }
  }

  data class ConnectionTypes(
    val readConnections: Set<Class<out ReadConnection>>,
    val writeConnections: Set<Class<out WriteConnection>>
  ) {
    companion object {
      val EMPTY = ConnectionTypes(emptySet(), emptySet())
    }
  }
}

/** Result container for [Connection] attempts. */
sealed class ConnectionResult<T : Connection> {
  class Success<T : Connection>(val connection: T) : ConnectionResult<T>()
  class Failure<T : Connection>(val error: ChronicleError) : ConnectionResult<T>()
}

/**
 * A convenience method that will create a [ConnectionRequest] using the class provided as a type
 * parameter to the call, the provided processor, and provided policy.
 */
inline fun <reified T : Connection> Chronicle.getConnection(
  requester: ProcessorNode,
  policy: Policy? = null
) = getConnection(ConnectionRequest(T::class.java, requester, policy))

/**
 * A convenience method that will create a [ConnectionRequest] using the class provided as a type
 * parameter to the call, the provided processor, and provided policy.
 *
 * An optional [onError] callback will be called with the [ChronicleError] containing the failure
 * reason if the connection fails.
 */
inline fun <reified T : Connection> Chronicle.getConnectionOrNull(
  requester: ProcessorNode,
  policy: Policy? = null,
  noinline onError: (ChronicleError) -> Unit = {}
) = getConnectionOrNull(ConnectionRequest(T::class.java, requester, policy), onError)

/**
 * A convenience method that will create a [ConnectionRequest] using the class provided as a type
 * parameter to the call, the provided processor, and provided policy.
 */
inline fun <reified T : Connection> Chronicle.getConnectionOrThrow(
  requester: ProcessorNode,
  policy: Policy? = null
) = getConnectionOrThrow(ConnectionRequest(T::class.java, requester, policy))
