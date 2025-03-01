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

/** Defines an object capable of injecting members into the [FileSpec.Builder]. */
interface FileSpecContentsProvider {
  /**
   * Provide members for the [FileSpec.Builder] by calling methods like:
   * * [FileSpec.Builder.addProperty]
   * * [FileSpec.Builder.addFunction]
   * * [FileSpec.Builder.addType]
   * * [FileSpec.Builder.addComment]
   * * etc.
   */
  fun provideContentsInto(builder: FileSpec.Builder)
}
