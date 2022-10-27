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

package com.google.android.libraries.pcc.chronicle.api.policy.proto

import com.google.android.libraries.pcc.chronicle.api.policy.annotation.Annotation
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.AnnotationParam
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AnnotationProtoTest {
  @Test
  fun roundTrip() {
    val annotation = Annotation(
      name = "MyAnnotation",
      params = mapOf(
        "str" to AnnotationParam.Str("abc"),
        "bool" to AnnotationParam.Bool(true),
        "num" to AnnotationParam.Num(123)
      )
    )
    assertThat(annotation.encode().decode()).isEqualTo(annotation)
  }
}
