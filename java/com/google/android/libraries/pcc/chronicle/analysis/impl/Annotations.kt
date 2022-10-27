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
import java.time.Duration

internal val ANNOTATION_IN_MEMORY = Annotation.createCapability("inMemory")
internal val ANNOTATION_PERSISTENT = Annotation.createCapability("persistent")
internal val ANNOTATION_REMOTE_PERSISTENT = Annotation.createCapability("remotePersistent")
internal val ANNOTATION_ENCRYPTED = Annotation.createCapability("encrypted")

internal fun Duration.toAnnotation() = Annotation.createTtl("${this.toMinutes()} minutes")

internal fun ManagementStrategy.toAnnotations(): List<Annotation> {
  return when (this) {
    ManagementStrategy.PassThru -> listOf(ANNOTATION_IN_MEMORY)
    is ManagementStrategy.Stored -> {
      val result = mutableListOf<Annotation>()

      if (encrypted) result.add(ANNOTATION_ENCRYPTED)

      val mediaAnnotation =
        when (media) {
          StorageMedia.LOCAL_DISK -> ANNOTATION_PERSISTENT
          StorageMedia.REMOTE_DISK -> ANNOTATION_REMOTE_PERSISTENT
          StorageMedia.MEMORY -> ANNOTATION_IN_MEMORY
        }
      result.add(mediaAnnotation)

      ttl?.let { result.add(it.toAnnotation()) }

      result
    }
  }
}
