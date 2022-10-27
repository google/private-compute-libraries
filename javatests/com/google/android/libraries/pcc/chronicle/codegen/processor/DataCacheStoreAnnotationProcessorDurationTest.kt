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

package com.google.android.libraries.pcc.chronicle.codegen.processor

import com.google.common.truth.Truth.assertThat
import java.time.Duration
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataCacheStoreAnnotationProcessorDurationTest {
  @Test
  fun ttlDuration_invalid() {
    assertFailsWith<IllegalArgumentException> {
      DataCacheStoreAnnotationProcessor.ttlDuration("4y3M1d")
    }
  }

  @Test
  fun ttlDuration_days() {
    val actual = DataCacheStoreAnnotationProcessor.ttlDuration("42d")
    assertThat(actual).isEqualTo(Duration.ofDays(42))
  }

  @Test
  fun ttlDuration_hours() {
    val actual = DataCacheStoreAnnotationProcessor.ttlDuration("42h")
    assertThat(actual).isEqualTo(Duration.ofHours(42))
  }

  @Test
  fun ttlDuration_minutes() {
    val actual = DataCacheStoreAnnotationProcessor.ttlDuration("42m")
    assertThat(actual).isEqualTo(Duration.ofMinutes(42))
  }

  @Test
  fun ttlDuration_seconds() {
    val actual = DataCacheStoreAnnotationProcessor.ttlDuration("42s")
    assertThat(actual).isEqualTo(Duration.ofSeconds(42))
  }

  @Test
  fun ttlDuration_millis() {
    val actual = DataCacheStoreAnnotationProcessor.ttlDuration("42ms")
    assertThat(actual).isEqualTo(Duration.ofMillis(42))
  }

  @Test
  fun ttlDuration_micros() {
    val actual = DataCacheStoreAnnotationProcessor.ttlDuration("42us")
    assertThat(actual).isEqualTo(Duration.ofNanos(42000))
  }

  @Test
  fun ttlDuration_compound() {
    val actual = DataCacheStoreAnnotationProcessor.ttlDuration("1d2h3m4s5ms6us")
    assertThat(actual)
      .isEqualTo(
        Duration.ofDays(1)
          .plus(Duration.ofHours(2))
          .plus(Duration.ofMinutes(3))
          .plus(Duration.ofSeconds(4))
          .plus(Duration.ofMillis(5))
          .plus(Duration.ofNanos(6000))
      )
  }
}
