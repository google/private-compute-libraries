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

import android.util.Log
import android.util.Log.DEBUG
import android.util.Log.ERROR
import android.util.Log.INFO
import android.util.Log.VERBOSE
import android.util.Log.WARN

/** Container for Chronicle's loggers. */
object Logcat {
  private const val DEFAULT_TAG: String = "Chronicle"
  private const val CLIENT_TAG: String = "ChronicleClient"
  private const val SERVER_TAG: String = "ChronicleServer"

  /**
   * [TaggedLogger] intended for use by implementations of the Chronicle interface and as a
   * catch-all in situations where it doesn't make sense to implement a new logger.
   */
  val default: TaggedLogger = TaggedLogger(DEFAULT_TAG)

  /** [TaggedLogger] intended for use by client-side API components. */
  val clientSide: TaggedLogger = TaggedLogger(CLIENT_TAG)

  /** [TaggedLogger] intended for use by server-side API components. */
  val serverSide: TaggedLogger = TaggedLogger(SERVER_TAG)
}

/**
 * Helper class to associate a tag with logging calls. Each method simply calls the corresponding
 * method in Android's native logger with the given tag.
 */
class TaggedLogger(val tag: String) {

  /**
   * Send a [VERBOSE] log message.
   * @param msg The message you would like logged.
   */
  fun v(msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, VERBOSE)) {
      Log.v(tag, String.format(msg, *args))
    }
  }

  /**
   * Send a [VERBOSE] log message and log the exception.
   * @param tr An exception to log
   * @param msg The message you would like logged.
   */
  fun v(tr: Throwable, msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, VERBOSE)) {
      Log.v(tag, String.format(msg, *args), tr)
    }
  }

  /**
   * Send a [DEBUG] log message.
   * @param msg The message you would like logged.
   */
  fun d(msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, DEBUG)) {
      Log.d(tag, String.format(msg, *args))
    }
  }

  /**
   * Send a [DEBUG] log message and log the exception.
   * @param tr An exception to log
   * @param msg The message you would like logged.
   */
  fun d(tr: Throwable, msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, DEBUG)) {
      Log.d(tag, String.format(msg, *args), tr)
    }
  }

  /**
   * Send an [INFO] log message.
   * @param msg The message you would like logged.
   */
  fun i(msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, INFO)) {
      Log.i(tag, String.format(msg, *args))
    }
  }

  /**
   * Send a [INFO] log message and log the exception.
   * @param tr An exception to log
   * @param msg The message you would like logged.
   */
  fun i(tr: Throwable, msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, INFO)) {
      Log.i(tag, String.format(msg, *args), tr)
    }
  }

  /**
   * Send a [WARN] log message.
   * @param msg The message you would like logged.
   */
  fun w(msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, WARN)) {
      Log.w(tag, String.format(msg, *args))
    }
  }

  /**
   * Send a [WARN] log message and log the exception.
   * @param tr An exception to log
   * @param msg The message you would like logged.
   */
  fun w(tr: Throwable, msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, WARN)) {
      Log.w(tag, String.format(msg, *args), tr)
    }
  }

  /**
   * Send a [WARN] log message and log the exception.
   * @param tr An exception to log
   */
  fun w(tr: Throwable) {
    if (Log.isLoggable(tag, WARN)) {
      Log.w(tag, tr)
    }
  }

  /**
   * Send an [ERROR] log message.
   * @param msg The message you would like logged.
   */
  fun e(msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, ERROR)) {
      Log.e(tag, String.format(msg, *args))
    }
  }

  /**
   * Send a [ERROR] log message and log the exception.
   * @param tr An exception to log
   * @param msg The message you would like logged.
   */
  fun e(tr: Throwable, msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, ERROR)) {
      Log.e(tag, String.format(msg, *args), tr)
    }
  }

  /**
   * Handy function to get a loggable stack trace from a Throwable
   * @param tr An exception to log
   */
  fun getStackTraceString(tr: Throwable): String {
    return Log.getStackTraceString(tr)
  }

  /**
   * Low-level logging call.
   * @param priority The priority/type of this log message
   * @param msg The message you would like logged.
   * @return The number of bytes written.
   */
  fun println(priority: Int, msg: String, vararg args: Any) {
    if (Log.isLoggable(tag, priority)) {
      Log.println(priority, tag, String.format(msg, *args))
    }
  }

  /**
   * Times the duration of the [block] and logs the provided [message] along with the duration at
   * [VERBOSE] level.
   */
  inline fun <T> timeVerbose(message: String, block: () -> T): T {
    val start = System.nanoTime()
    return try {
      block()
    } finally {
      val duration = System.nanoTime() - start
      v("%s [duration: %.3f ms]", message, duration / 1000000.0)
    }
  }
}
