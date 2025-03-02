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

package com.google.android.libraries.pcc.chronicle.analysis.impl

import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.Annotation
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AnnotationTest {
  @Test
  fun duration_toAnnotation() {
    assertThat(Duration.ofMillis(150).toAnnotation()).isEqualTo(Annotation.createTtl("0 minutes"))
    assertThat(Duration.ofMillis(1500).toAnnotation()).isEqualTo(Annotation.createTtl("0 minutes"))
    assertThat(Duration.ofSeconds(60).toAnnotation()).isEqualTo(Annotation.createTtl("1 minutes"))
    assertThat(Duration.ofDays(10).toAnnotation())
      .isEqualTo(Annotation.createTtl("${Duration.ofDays(10).toMinutes()} minutes"))
  }

  @Test
  fun managementStrategy_toAnnotations_passThru() {
    val annotations = ManagementStrategy.PassThru.toAnnotations()
    assertThat(annotations).containsExactly(ANNOTATION_IN_MEMORY)
  }

  @Test
  fun managementStrategy_toAnnotations_storedEncryptedLocalDiskNoTtl() {
    val annotations =
      ManagementStrategy.Stored(encrypted = true, media = StorageMedia.LOCAL_DISK, ttl = null)
        .toAnnotations()

    assertThat(annotations).containsExactly(ANNOTATION_PERSISTENT, ANNOTATION_ENCRYPTED)
  }

  @Test
  fun managementStrategy_toAnnotations_storedUnencryptedMemoryNoTtl() {
    val annotations =
      ManagementStrategy.Stored(encrypted = false, media = StorageMedia.MEMORY, ttl = null)
        .toAnnotations()

    assertThat(annotations).containsExactly(ANNOTATION_IN_MEMORY)
  }

  @Test
  fun managementStrategy_toAnnotations_storedUnencryptedMemoryTtl() {
    val annotations =
      ManagementStrategy.Stored(
          encrypted = false,
          media = StorageMedia.MEMORY,
          ttl = Duration.ofMinutes(5),
        )
        .toAnnotations()

    assertThat(annotations)
      .containsExactly(ANNOTATION_IN_MEMORY, Duration.ofMinutes(5).toAnnotation())
  }

  @Test
  fun managementStrategy_toAnnotations_storedEncryptedRemoteTtl() {
    val annotations =
      ManagementStrategy.Stored(
          encrypted = true,
          media = StorageMedia.REMOTE_DISK,
          ttl = Duration.ofMinutes(500),
        )
        .toAnnotations()

    assertThat(annotations)
      .containsExactly(
        ANNOTATION_REMOTE_PERSISTENT,
        ANNOTATION_ENCRYPTED,
        Duration.ofMinutes(500).toAnnotation(),
      )
  }
}
