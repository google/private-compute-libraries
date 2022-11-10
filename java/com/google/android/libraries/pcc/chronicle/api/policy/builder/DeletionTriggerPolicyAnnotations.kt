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

package com.google.android.libraries.pcc.chronicle.api.policy.builder

import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyTarget
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.Annotation
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.AnnotationParam

/** The constants used to encode a [DeletionTrigger] onto a target of the [Policy]. */
object DeletionTriggerPolicyAnnotations {
  const val ANNOTATION_NAME = "deletionTrigger"
  const val TRIGGER_KEY = "trigger"
  const val FIELD_KEY = "field"
}

/** Retrive [DeletionTrigger] from a [PolicyTarget]. */
fun PolicyTarget.deletionTriggers(): Set<DeletionTrigger> {
  return annotations.deletionTriggers()
}

/** Encodes a trigger as a Policy annotation */
fun Trigger.toAnnotation(field: String): Annotation {
  return Annotation(
    DeletionTriggerPolicyAnnotations.ANNOTATION_NAME,
    mapOf(
      DeletionTriggerPolicyAnnotations.TRIGGER_KEY to AnnotationParam.Str(this.name),
      DeletionTriggerPolicyAnnotations.FIELD_KEY to AnnotationParam.Str(field),
    )
  )
}

private fun Collection<Annotation>.deletionTriggers(): Set<DeletionTrigger> {
  return this.filter { it.name == DeletionTriggerPolicyAnnotations.ANNOTATION_NAME }
    .map {
      DeletionTrigger(
        Trigger.valueOf(it.getStringParam(DeletionTriggerPolicyAnnotations.TRIGGER_KEY)),
        it.getStringParam(DeletionTriggerPolicyAnnotations.FIELD_KEY)
      )
    }
    .toSet()
}
