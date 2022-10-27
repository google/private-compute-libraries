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

package com.google.android.libraries.pcc.chronicle.api.cantrip

/**
 * A [Cantrip] is a transformation of [Data], represented as a function mapping that data to a
 * mutated or null version of itself.
 */
interface Cantrip<Data> {
  /** Applies the [Cantrip] across a single [datum]. */
  operator fun invoke(datum: Data): Data?
}

/**
 * Applies the [Cantrip] across all items in the [sequence].
 *
 * If a given return value from the [Cantrip] is null, the data is omitted from the sequence.
 */
operator fun <Data> Cantrip<Data>.invoke(sequence: Sequence<Data>): Sequence<Data> =
  sequence.mapNotNull(::invoke)

/** Applies the [Cantrip] across all items in the [List]. */
operator fun <Data> Cantrip<Data>.invoke(list: List<Data>): List<Data> =
  invoke(list.asSequence()).toList()

/** Applies the [Cantrip] across all items in the [Set]. */
operator fun <Data> Cantrip<Data>.invoke(set: Set<Data>): Set<Data> =
  invoke(set.asSequence()).toSet()
