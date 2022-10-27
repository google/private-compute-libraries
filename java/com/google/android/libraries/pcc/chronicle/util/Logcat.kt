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

package com.google.android.libraries.pcc.chronicle.util

import com.google.common.flogger.android.AndroidFluentLogger

/**
 * Container for Chronicle's [AndroidFluentLogger] getters.
 */
object Logcat {
  private const val DEFAULT_TAG: String = "Chronicle"
  private const val CLIENT_TAG: String = "ChronicleClient"
  private const val SERVER_TAG: String = "ChronicleServer"

  /**
   * [AndroidFluentLogger] intended for use by implementations of the Chronicle interface and as a
   * catch-all in situations where it doesn't make sense to implement a new logger.
   */
  val default: AndroidFluentLogger = AndroidFluentLogger.createLogcatLogger(DEFAULT_TAG)

  /** [AndroidFluentLogger] intended for use by client-side API components. */
  val clientSide: AndroidFluentLogger = AndroidFluentLogger.createLogcatLogger(CLIENT_TAG)

  /** [AndroidFluentLogger] intended for use by server-side API components. */
  val serverSide: AndroidFluentLogger = AndroidFluentLogger.createLogcatLogger(SERVER_TAG)
}

/**
 * Times the duration of the [block] and logs the provided [message] along with the duration at
 * VERBOSE level.
 */
inline fun <T> AndroidFluentLogger.timeVerbose(message: String, block: () -> T): T {
  val start = System.nanoTime()
  return try {
    block()
  } finally {
    val duration = System.nanoTime() - start
    atVerbose().log("%s [duration: %.3f ms]", message, duration / 1000000.0)
  }
}
