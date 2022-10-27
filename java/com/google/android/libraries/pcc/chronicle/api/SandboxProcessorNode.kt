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

package com.google.android.libraries.pcc.chronicle.api

/**
 * Only for use by SandboxDataAccessService. This class is for ProcessorNodes that run in an
 * isolated sandbox
 */
class SandboxProcessorNode(private val processorNode: ProcessorNode) : ProcessorNode {
  override val requiredConnectionTypes: Set<Class<out Connection>>
    get() = processorNode.requiredConnectionTypes
}
