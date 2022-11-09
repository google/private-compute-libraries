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

import kotlin.reflect.KClass

/**
 * Defines a particular type of data managed by a [ConnectionProvider], as well as: how it's managed
 * ([managementStrategy]) and how it can be accessed ([connectionTypes]).
 */
data class ManagedDataType(
  override val descriptor: DataTypeDescriptor,
  override val managementStrategy: ManagementStrategy,
  override val connectionTypes: Set<Class<out Connection>>
) : DataType {
  /**
   * A convenience constructor allowing you to pass [Connection] interface classes as [KClass]
   * varargs at the end of the parameters list.
   */
  constructor(
    descriptor: DataTypeDescriptor,
    managementStrategy: ManagementStrategy,
    vararg connectionTypes: KClass<out Connection>
  ) : this(descriptor, managementStrategy, connectionTypes.map { it.java }.toSet())
}

data class ManagedDataTypeWithRemoteConnectionNames(
  override val descriptor: DataTypeDescriptor,
  override val managementStrategy: ManagementStrategy,
  override val connectionTypes: Set<Class<out Connection>>,
  override val connectionNames: Set<ConnectionName<out Connection>> =
    setOf(
      ReadConnection.connectionNameForRemoteConnections(descriptor.name),
      WriteConnection.connectionNameForRemoteConnections(descriptor.name)
    )
) : DataType
