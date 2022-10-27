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

/**
 * This annotation can be used with a type that will be stored with Chronicle. When the
 * [ChronicleDataAnnotationProcessor] plugin is present, it will operate on all types with this
 * annotation. When processed, this annotation will generate a [DataTypeDescriptor] object for the
 * data.
 */
// Runtime retention allows other annotations like [ChronicleConnection] to check if elements are
// annotated with [ChronicleData] during processing.
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ChronicleData
