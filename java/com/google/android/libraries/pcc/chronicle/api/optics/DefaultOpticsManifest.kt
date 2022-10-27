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

package com.google.android.libraries.pcc.chronicle.api.optics

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptorSet
import com.google.android.libraries.pcc.chronicle.api.FieldType

/**
 * Default implementation of [OpticsManifest] which takes a global set of [Lenses][Lens] along with
 * the all of the known data types via the [DataTypeDescriptorSet] and provides ways to compose
 * traversals for those data types.
 */
class DefaultOpticsManifest(
  lenses: Set<Lens<*, *, *, *>>,
  private val dtds: DataTypeDescriptorSet
) : OpticsManifest {
  private var traversalsByPath: MutableMap<OpticalAccessPath, Traversal<*, *, *, *>> =
    lenses.associateTo(mutableMapOf()) { it.sourceAccessPath to it.asTraversal() }

  @Synchronized
  @Suppress("UNCHECKED_CAST") // Because we do actually check.
  override fun <S, T, A, B> composeTraversal(
    accessPath: OpticalAccessPath,
    sourceEntityType: Class<out S>,
    targetEntityType: Class<out T>,
    sourceFieldType: Class<out A>,
    targetFieldType: Class<out B>
  ): Traversal<S, T, A, B> {
    val result = composeTraversalInner(accessPath, isTail = true) as Traversal<S, T, A, B>

    require(result.sourceEntityType.isAssignableFrom(sourceEntityType)) {
      "Could not compose a traversal for $accessPath with an input entity " +
        "type of $sourceEntityType."
    }
    require(targetEntityType.isAssignableFrom(result.targetEntityType)) {
      "Could not compose a traversal for $accessPath with an output entity " +
        "type of $targetEntityType."
    }
    require(result.sourceFieldType.isAssignableFrom(sourceFieldType)) {
      "Could not compose a traversal for $accessPath with an input field type " +
        "of $sourceFieldType."
    }
    require(targetFieldType.isAssignableFrom(result.targetFieldType)) {
      "Could not compose a traversal for $accessPath with an output field type " +
        "of $targetFieldType."
    }

    return result
  }

  @Suppress("UNCHECKED_CAST") // We end up checking later on.
  private fun composeTraversalInner(
    accessPath: OpticalAccessPath,
    isTail: Boolean = true,
  ): Traversal<Any, Any, Any, Any> {
    val precalculated = traversalsByPath[accessPath]
    if (precalculated != null) {
      // If the access path represents the tail in a chain, we don't need to compose any additional
      // traversals.
      if (isTail) return precalculated as Traversal<Any, Any, Any, Any>

      // If the access path is higher up on a longer chain, we should construct traversals to dive
      // into the field type (where necessary - when the field is a list/array
      var fieldType =
        dtds.findFieldTypeOrThrow(accessPath.dataTypeName, listOf(accessPath.selectors.first()))

      var result = precalculated
      while (fieldType is FieldType.List) {
        result = result as Traversal<*, *, List<*>, List<*>> compose Traversal.list()
        fieldType = fieldType.itemFieldType
      }
      return result as Traversal<Any, Any, Any, Any>
    } else if (accessPath.selectors.size == 1) {
      throw IllegalArgumentException("$accessPath has no associated optic.")
    }

    // if we don't have it, let's get the first optic and compose it with an inner optic.
    val (head, tail) = accessPath.split()
    val result = composeTraversalInner(head, isTail = false) compose composeTraversalInner(tail)
    traversalsByPath[accessPath] = result
    return result
  }

  private fun OpticalAccessPath.split(): Pair<OpticalAccessPath, OpticalAccessPath> {
    val headSelector = listOf(selectors.first())
    val headFieldType = dtds.findFieldTypeOrThrow(dataTypeName, headSelector)
    val headDataType =
      requireNotNull(dtds.findDataTypeDescriptor(headFieldType)) {
        "Cannot find associated DataTypeDescriptor for field $headSelector of $this."
      }

    val head = copy(selectors = headSelector)
    val tail = OpticalAccessPath(headDataType.name, selectors.drop(1))
    return head to tail
  }
}
