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

package com.google.android.libraries.pcc.chronicle.annotation

import kotlin.reflect.KClass

/**
 * This annotation can be used with on a Chronicle connection. When the
 * [ChronicleConnectionAnnotationProcessor] plugin is present, it will operate on all types with
 * this annotation, and collect connections corresponding to the same data class into a generated
 * set of [Connections][Connection] for the data.
 *
 * It will additionally, optionally, generate implementations for annotated [Connections], plus a
 * [ConnectionProvider] class.
 *
 * @param[dataClass] the class annotated with [ChronicleData]
 * @param[generateConnectionProvider] optionally generate [ConnectionProvider] code
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ChronicleConnection(
  val dataClass: KClass<*>,
  val generateConnectionProvider: Boolean = false
)
