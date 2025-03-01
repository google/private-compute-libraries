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

package com.google.android.libraries.pcc.chronicle.api.optics.testdata

import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData
import com.google.android.libraries.pcc.chronicle.api.optics.Lens
import com.google.android.libraries.pcc.chronicle.api.optics.OpticalAccessPath

@ChronicleData data class TestLocation(val latitude: Float, val longitude: Float)

val TestLocation_Latitude =
  Lens.create<TestLocation, Float>(
    focusAccessPath = OpticalAccessPath(TEST_LOCATION_GENERATED_DTD, "latitude"),
    getter = { it.latitude },
    setter = { entity, newValue -> entity.copy(latitude = newValue) },
  )

val TestLocation_Longitude =
  Lens.create<TestLocation, Float>(
    focusAccessPath = OpticalAccessPath(TEST_LOCATION_GENERATED_DTD, "longitude"),
    getter = { it.longitude },
    setter = { entity, newValue -> entity.copy(longitude = newValue) },
  )

val TestLocation_Lenses = setOf(TestLocation_Latitude, TestLocation_Longitude)
