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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.remote.ClientDetails
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowBinder
import org.robolectric.shadows.ShadowPackageManager

@RunWith(AndroidJUnit4::class)
class ClientDetailsProviderImplTest {
  private lateinit var context: Context
  private lateinit var shadowPackageManager: ShadowPackageManager
  private lateinit var provider: ClientDetailsProviderImpl

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    shadowPackageManager = Shadows.shadowOf(context.packageManager)
    provider = ClientDetailsProviderImpl(context)
  }

  @Test
  fun getClientDetails_notIsolated() {
    ShadowBinder.setCallingUid(42)
    shadowPackageManager.setPackagesForUid(42, "com.google.android.as", "com.google.android.odad")

    var details = provider.getClientDetails()
    assertThat(details.userId).isEqualTo(42)
    assertThat(details.associatedPackages)
      .containsExactly("com.google.android.as", "com.google.android.odad")
    assertThat(details.isolationType).isEqualTo(ClientDetails.IsolationType.DEFAULT_PROCESS)

    ShadowBinder.setCallingUid(89999)
    shadowPackageManager.setPackagesForUid(
      89999,
      "com.google.android.as",
      "com.google.android.odad",
    )

    details = provider.getClientDetails()
    assertThat(details.userId).isEqualTo(89999)
    assertThat(details.associatedPackages)
      .containsExactly("com.google.android.as", "com.google.android.odad")
    assertThat(details.isolationType).isEqualTo(ClientDetails.IsolationType.DEFAULT_PROCESS)

    ShadowBinder.setCallingUid(100000)
    shadowPackageManager.setPackagesForUid(
      100000,
      "com.google.android.as",
      "com.google.android.odad",
    )

    details = provider.getClientDetails()
    assertThat(details.userId).isEqualTo(100000)
    assertThat(details.associatedPackages)
      .containsExactly("com.google.android.as", "com.google.android.odad")
    assertThat(details.isolationType).isEqualTo(ClientDetails.IsolationType.DEFAULT_PROCESS)
  }

  @Test
  fun getClientDetails_isolated() {
    ShadowBinder.setCallingUid(90000)
    shadowPackageManager.setPackagesForUid(
      90000,
      "com.google.android.as",
      "com.google.android.odad",
    )

    var details = provider.getClientDetails()
    assertThat(details.userId).isEqualTo(90000)
    assertThat(details.associatedPackages)
      .containsExactly("com.google.android.as", "com.google.android.odad")
    assertThat(details.isolationType).isEqualTo(ClientDetails.IsolationType.ISOLATED_PROCESS)

    ShadowBinder.setCallingUid(99999)
    shadowPackageManager.setPackagesForUid(
      99999,
      "com.google.android.as",
      "com.google.android.odad",
    )

    details = provider.getClientDetails()
    assertThat(details.userId).isEqualTo(99999)
    assertThat(details.associatedPackages)
      .containsExactly("com.google.android.as", "com.google.android.odad")
    assertThat(details.isolationType).isEqualTo(ClientDetails.IsolationType.ISOLATED_PROCESS)

    ShadowBinder.setCallingUid(190000)
    shadowPackageManager.setPackagesForUid(
      190000,
      "com.google.android.as",
      "com.google.android.odad",
    )

    details = provider.getClientDetails()
    assertThat(details.userId).isEqualTo(190000)
    assertThat(details.associatedPackages)
      .containsExactly("com.google.android.as", "com.google.android.odad")
    assertThat(details.isolationType).isEqualTo(ClientDetails.IsolationType.ISOLATED_PROCESS)

    ShadowBinder.setCallingUid(199999)
    shadowPackageManager.setPackagesForUid(
      199999,
      "com.google.android.as",
      "com.google.android.odad",
    )

    details = provider.getClientDetails()
    assertThat(details.userId).isEqualTo(199999)
    assertThat(details.associatedPackages)
      .containsExactly("com.google.android.as", "com.google.android.odad")
    assertThat(details.isolationType).isEqualTo(ClientDetails.IsolationType.ISOLATED_PROCESS)
  }
}
