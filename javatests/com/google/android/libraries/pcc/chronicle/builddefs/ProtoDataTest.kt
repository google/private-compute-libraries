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
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtoDataTest {
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
  fun chronicleDataProtoLibrary_includesGeneratedLenses() {
    var data = MySampleMessageType.getDefaultInstance()
    data = MY_SAMPLE_MESSAGE_TYPE_FIRST_FIELD_GENERATED_LENS.set(data, "Hello")
    data = MY_SAMPLE_MESSAGE_TYPE_SECOND_FIELD_GENERATED_LENS.set(data, "World")
    data =
      MY_SAMPLE_MESSAGE_TYPE_FOREIGN_GENERATED_LENS.set(
        data,
        ForeignNested.newBuilder().setFirstField("Nested").build()
      )

    assertThat(MY_SAMPLE_MESSAGE_TYPE_FIRST_FIELD_GENERATED_LENS.get(data)).isEqualTo("Hello")
    assertThat(MY_SAMPLE_MESSAGE_TYPE_SECOND_FIELD_GENERATED_LENS.get(data)).isEqualTo("World")
    assertThat(MY_SAMPLE_MESSAGE_TYPE_FOREIGN_GENERATED_LENS.get(data).firstField)
      .isEqualTo("Nested")
  }
}
