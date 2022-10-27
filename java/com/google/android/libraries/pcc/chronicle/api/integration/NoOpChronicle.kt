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

package com.google.android.libraries.pcc.chronicle.api.integration

import com.google.android.libraries.pcc.chronicle.api.Chronicle
import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ConnectionRequest
import com.google.android.libraries.pcc.chronicle.api.ConnectionResult
import com.google.android.libraries.pcc.chronicle.api.error.Disabled
import kotlin.reflect.KClass

/**
 * No-op implementation of [Chronicle].
 *
 * Every call to [getConnection] results in a [ConnectionResult.Failure].
 */
class NoOpChronicle(
  /** Set if the reason for the no-op impl was flags. This enables debugging no-op results. */
  private val disabledFromStartupFlags: Boolean = false
) : Chronicle {
  override fun getAvailableConnectionTypes(dataTypeClass: KClass<*>): Chronicle.ConnectionTypes =
    Chronicle.ConnectionTypes.EMPTY

  override fun <T : Connection> getConnection(request: ConnectionRequest<T>): ConnectionResult<T> {
    return if (disabledFromStartupFlags)
      ConnectionResult.Failure(Disabled("Chronicle disabled via startup flags."))
    else ConnectionResult.Failure(Disabled("Chronicle not present in build."))
  }
}
