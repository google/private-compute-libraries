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

package com.google.android.libraries.pcc.chronicle.samples.policy.peopleproto

import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.builder.policy
import com.google.android.libraries.pcc.chronicle.samples.datahub.peopleproto.PERSON_GENERATED_DTD
import java.time.Duration

/** [Policy] which can be used when requesting a [ReadConnection] to [Person] data. */
val PEOPLE_PROTO_POLICY =
  policy(name = "PeopleProtoPolicy", egressType = "UI Display") {
    description =
      """
      Allows the user to access Person data with the purpose of displaying and providing editing
      capability via a user interface.
      """
        .trimIndent()

    target(dataTypeDescriptor = PERSON_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.DISK, encryptionRequired = false)

      "name" { rawUsage(UsageType.ANY) }
      "age" { rawUsage(UsageType.ANY) }
    }
  }
