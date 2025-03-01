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

package com.google.android.libraries.pcc.chronicle.remote

/**
 * Defines an object capable of determining [ClientDetails] from the current thread, implementations
 * will typically use [Binder.getCallingUid] and system services to flesh out the contents of
 * [ClientDetails] returned from [getClientDetails].
 */
interface ClientDetailsProvider {
  /**
   * Gets the current [ClientDetails]. Should only be called from threads directly handling a binder
   * transaction.
   */
  fun getClientDetails(): ClientDetails
}
