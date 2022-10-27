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

package com.google.android.libraries.pcc.chronicle.codegen.frontend.element

import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

/**
 * A field declaration returned by a [FieldEnumerator]. It represents any arbitrary concept of a
 * "field", including an actual field on a type, as well as a getter method that returns a
 * particular type.
 */
data class FieldDecl(val name: String, val type: TypeMirror)

/**
 * Implementors provide a strategy for getting a list [FieldDecl] for the provided [Element] that
 * represents a composite type.
 */
interface FieldEnumerator {
  /**
   * If this enumerator supports the provided type, a list of field elements using this strategy
   * will be returned. If the type isn't supoprted by this enumerator, it will return null.
   */
  fun fieldsInType(element: Element): List<FieldDecl>?
}
