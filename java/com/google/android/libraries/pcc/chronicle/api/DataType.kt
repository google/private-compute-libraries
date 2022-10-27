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

import java.time.Duration

/**
 * The base interface for all data types that will be managed by Chronicle.
 *
 * This is how data stewards specify configuration information for the data that they want to store
 * and manage using Chronicle. Data stewards should define a singleton object that extends the
 * [DataType] interface and may add extra configuration parameters to this singleton if needed:
 *
 * ```
 * object MyPrivateDataType : DataType {
 *      override val descriptor = ...
 *      override val managementStrategy = ...
 *      override val connectionTypes = ...
 *      // Additional configuration parameters
 *      const val MAX_ENTITIES = 1000
 * }
 * ```
 *
 * [ConnectionProvider]s must provide a [DataType] object to [Chronicle] to declare how they
 * participate in data flow.
 */
interface DataType {
  /** The description of the data schema such as the fields and its types. */
  val descriptor: DataTypeDescriptor
  /** The storage management strategy for the data. */
  val managementStrategy: ManagementStrategy
  /** The connections supported for accessing this [DataType] */
  val connectionTypes: Set<Class<out Connection>>
  /** Extracts and returns the ttl from [managementStrategy][DataType.managementStrategy]. */
  val ttl: Duration
    get() = managementStrategy.ttl()
}
