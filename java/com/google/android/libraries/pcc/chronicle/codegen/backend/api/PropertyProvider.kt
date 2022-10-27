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

package com.google.android.libraries.pcc.chronicle.codegen.backend.api

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * Defines an object capable of building a [PropertySpec] and injecting it into kotlinpoet builders
 * like [FileSpec.Builder].
 */
abstract class PropertyProvider : FileSpecContentsProvider {
  /** Returns a [PropertySpec] that can be added to a kotlinpoet builder. */
  abstract fun provideProperty(): PropertySpec

  override fun provideContentsInto(builder: FileSpec.Builder) {
    builder.addProperty(provideProperty())
  }
}
