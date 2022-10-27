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

package com.google.android.libraries.pcc.chronicle.storage

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy

/**
 * An interface inherited by Chronicle managed stores. These stores provide [ManagementStrategy]
 * details to Chronicle to verify compatibility with policies.
 */
interface ManagedStore {
  /** The data type descriptor of the underlying type. */
  val dataTypeDescriptor: DataTypeDescriptor

  /** The management strategy of this storage. */
  val managementStrategy: ManagementStrategy
}
