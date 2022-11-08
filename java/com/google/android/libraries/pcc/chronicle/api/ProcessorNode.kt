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
 * A processor node is a description of a component which asks [Chronicle] for [Connection] objects
 * and performs some kind of computation on the data supplied by those connections or writes data
 * back to them (or any mix of the two).
 */
interface ProcessorNode {
  /**
   * The classes of the connections required by this [ProcessorNode].
   *
   * NOTE: override this if you plan on using Class objects in [ConnectionProvider] and
   * [ConnectionRequest].
   */
  // TODO(b/251295492) this property can disappear.
  val requiredConnectionTypes: Set<Class<out Connection>>

  /**
   * The [ConnectionName]s of the connections required by this [ProcessorNode].
   *
   * NOTE: override this if you plan on using [ConnectionName] in [ConnectionProvider] and
   * [ConnectionRequest]. If you do, override [ProcessorNode.requiredConnectionTypes] by adding a
   * getter which returns an emptySet().
   */
  val requiredConnectionNames: Set<ConnectionName<out Connection>>
    get() = requiredConnectionTypes.map { Connection.connectionName(it) }.toSet()
}
