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

package com.google.android.libraries.pcc.chronicle.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenericPassThroughTest {
  @Test
  fun write_callsAllNotifiers_eachTime() {
    var first = ""
    var second = ""
    val node = GenericPassThrough<String>()
    node.listen { first = it }
    node.listen { second = it }

    node.write("Hello")

    assertThat(first).isEqualTo("Hello")
    assertThat(second).isEqualTo("Hello")

    node.write("Hello2")

    assertThat(first).isEqualTo("Hello2")
    assertThat(second).isEqualTo("Hello2")
  }

  @Test
  fun cancel_removesNotifier() {
    var first = ""
    var second = ""
    val node = GenericPassThrough<String>()
    val cancel1 = node.listen { first = it }
    val cancel2 = node.listen { second = it }

    cancel1()
    node.write("Hello")

    assertThat(first).isEqualTo("")
    assertThat(second).isEqualTo("Hello")

    cancel2()
    node.write("Hello2")

    assertThat(first).isEqualTo("")
    assertThat(second).isEqualTo("Hello")
  }
}
