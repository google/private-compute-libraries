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

package com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes

import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData
import com.google.android.libraries.pcc.chronicle.annotation.DataCacheStore
import java.time.Duration
import java.time.Instant
import java.util.Optional

@ChronicleData
@DataCacheStore(ttl = "2d", maxItems = 1000)
data class ExampleKotlinType(
  val name: String,
  val amount: Int,
  val others: List<String>,
  val updateTime: Instant,
  val timeSinceLastOpen: Duration,
  val featuresEnabled: Map<String, Boolean>,
  val nickName: Optional<String>,
  val categoryAndScore: Pair<String, Double>,
  val threeThings: Triple<String, Double, Boolean>,
  val status: Status,
) {
  enum class Status {
    ON,
    OFF,
    UNKNOWN,
  }
}
