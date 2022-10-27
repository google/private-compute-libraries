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

package com.google.android.libraries.pcc.chronicle.api.storage

/**
 * A [WrappedEntity] is an [entity] of type [T] accompanied by [EntityMetadata].
 *
 * [WrappedEntities] allow for separation of storage and policy-specific data (the [EntityMetadata])
 * from the information provided to
 * [ProcessorNodes][com.google.android.libraries.pcc.chronicle.api.ProcessorNode] via
 * [Connections][com.google.android.libraries.pcc.chronicle.api.Connection].
 *
 * This makes it possible to expose data to a feature developer without also necessarily exposing
 * things like that data's creation timestamp.
 */
data class WrappedEntity<T>(val metadata: EntityMetadata, val entity: T)
