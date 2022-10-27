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

package com.google.android.libraries.pcc.chronicle.builddefs

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.optics.Lens
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = ProtoDataTest_Application::class)
class ProtoDataTest {
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Inject lateinit var lenses: Set<@JvmSuppressWildcards Lens<*, *, *, *>>

  @Inject lateinit var dtds: Set<DataTypeDescriptor>

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Test
  fun chronicleDataProtoLibrary_includesGeneratedDtd() {
    // Just the fact that the generated DTD exists is good test, but let's also check some stuff
    // about it.
    val dtdName = MY_SAMPLE_MESSAGE_TYPE_GENERATED_DTD.name
    val fields = MY_SAMPLE_MESSAGE_TYPE_GENERATED_DTD.fields

    assertThat(dtdName).isEqualTo("chronicle.data.sample.MySampleMessageType")
    assertThat(fields).hasSize(4)
  }

  @Test
  fun chronicleDataProtoLibrary_exportsProto() {
    // Creating a default instance is enough to show that the proto was included in the exports as
    // long as the test lib doesn't explicitly include the java_lite_proto lib.
    MySampleMessageType.getDefaultInstance()
  }

  @Test
  fun lensMultibindSet() {
    assertThat(lenses)
      .containsExactly(
        MY_SAMPLE_MESSAGE_TYPE_FIRST_FIELD_GENERATED_LENS,
        MY_SAMPLE_MESSAGE_TYPE_SECOND_FIELD_GENERATED_LENS,
        MY_SAMPLE_MESSAGE_TYPE_THIRD_FIELD_GENERATED_LENS,
        MY_SAMPLE_MESSAGE_TYPE_FOREIGN_GENERATED_LENS,
        NESTED_NAME_GENERATED_LENS
      )
  }

  @Test
  fun dtdMultibindSet() {
    assertThat(dtds).containsExactly(MY_SAMPLE_MESSAGE_TYPE_GENERATED_DTD, NESTED_GENERATED_DTD)
  }
}
