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

package com.google.android.libraries.pcc.chronicle.remote.impl

import android.content.Context
import android.os.Binder
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails
import com.google.android.libraries.pcc.chronicle.remote.ClientDetailsProvider

/**
 * Implementation of [ClientDetailsProvider] which uses [Binder.getCallingUid] and
 * [Context.getPackageManager] to fulfill requests for [ClientDetails] about clients in the current
 * thread handling a binder transaction from [IRemote].
 *
 * Because it uses [Binder.getCallingUid], this class should only ever be used from a BinderThread.
 * TODO(b/224999352): Create an instrumentation test to verify this behavior.
 */
class ClientDetailsProviderImpl(private val context: Context) : ClientDetailsProvider {
  override fun getClientDetails(): ClientDetails {
    val userId = Binder.getCallingUid()
    val associatedPackages =
      context.packageManager.getPackagesForUid(userId)?.asList() ?: emptyList()
    return ClientDetails(
      userId = userId,
      isolationType = isolationTypeForUid(userId),
      associatedPackages = associatedPackages
    )
  }

  private fun isolationTypeForUid(uid: Int): ClientDetails.IsolationType {
    val appId = uid % PER_USER_RANGE
    return if (appId in ISOLATED_RANGE || appId in APP_ZYGOTE_ISOLATED_RANGE) {
      ClientDetails.IsolationType.ISOLATED_PROCESS
    } else {
      ClientDetails.IsolationType.DEFAULT_PROCESS
    }
  }

  companion object {
    // This comes from UserHandle.java:
    // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/
    //    android/os/UserHandle.java
    private const val PER_USER_RANGE = 100000

    // These come from Process.java:
    // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/
    //    android/os/Process.java
    private const val FIRST_ISOLATED_UID = 99000
    private const val LAST_ISOLATED_UID = 99999
    private const val FIRST_APP_ZYGOTE_ISOLATED_UID = 90000
    private const val LAST_APP_ZYGOTE_ISOLATED_UID = 98999

    private val ISOLATED_RANGE = FIRST_ISOLATED_UID..LAST_ISOLATED_UID
    private val APP_ZYGOTE_ISOLATED_RANGE =
      FIRST_APP_ZYGOTE_ISOLATED_UID..LAST_APP_ZYGOTE_ISOLATED_UID
  }
}
