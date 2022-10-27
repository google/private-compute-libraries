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
 * Represents a connection to a [ConnectionProvider] which is capable of writing data to storage.
 *
 * **Note**
 *
 * This interface is purposefully left empty. It serves as a semantic clue to [Chronicle]'s
 * data-flow checking algorithm and is not intended to impose API requirements on implementations of
 * [ConnectionProvider] and the types of connections they provide.
 */
interface WriteConnection : Connection
