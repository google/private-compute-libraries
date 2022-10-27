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

package com.google.android.libraries.pcc.chronicle.api.testutil

import com.google.android.libraries.pcc.chronicle.api.Connection
import com.google.android.libraries.pcc.chronicle.api.ProcessorNode

/**
 * Empty [ProcessorNode] implementation to use in tests. Typically used when constructing
 * [ConnectionRequests][ConnectionRequest] for testing [ConnectionProviders][ConnectionProvider].
 */
object EmptyProcessorNode : ProcessorNode {
  override val requiredConnectionTypes: Set<Class<out Connection>> = emptySet()
}
