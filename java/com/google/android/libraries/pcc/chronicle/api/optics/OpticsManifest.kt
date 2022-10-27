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

/**
 * A manifest of all known optics which is capable of composing bespoke, nested optics for a given
 * [OpticalAccessPath].
 */
interface OpticsManifest {
  /**
   * Composes a [Traversal] for a given [accessPath].
   *
   * If one can't be composed (either the access path is invalid, or the manifest doesn't contain
   * sufficient optics to compose a satisfactory result - an exception will be thrown).
   *
   * TODO(b/204939436): Consider making an API like this (or something nicer) - possibly as
   * extension funs.
   * ```
   * optics[AppOps::class]["appOps"]["lastAccessTimeMs"]
   * optics[AppOps::class]["appOps.lastAccessTimeMs"]
   * ```
   */
  fun <S, T, A, B> composeTraversal(
    accessPath: OpticalAccessPath,
    sourceEntityType: Class<out S>,
    targetEntityType: Class<out T>,
    sourceFieldType: Class<out A>,
    targetFieldType: Class<out B>,
  ): Traversal<S, T, A, B>
}

/**
 * Assemble a polymorphic [Traversal] for a given [accessPath].
 *
 * If one can't be assembled (either the access path is invalid, or the manifest doesn't contain
 * sufficient optics to compose a satisfactory result - an exception will be thrown).
 */
inline fun <reified S, reified T, reified A, reified B> OpticsManifest.composePoly(
  accessPath: OpticalAccessPath
): Traversal<S, T, A, B> {
  return composeTraversal(accessPath, S::class.java, T::class.java, A::class.java, B::class.java)
}

/**
 * Assemble a monomorphic [Traversal] for a given [accessPath].
 *
 * If one can't be assembled (either the access path is invalid, or the manifest doesn't contain
 * sufficient optics to compose a satisfactory result - an exception will be thrown).
 */
inline fun <reified S, reified A> OpticsManifest.composeMono(
  accessPath: OpticalAccessPath
): Traversal<S, S, A, A> {
  return composeTraversal(accessPath, S::class.java, S::class.java, A::class.java, A::class.java)
}
