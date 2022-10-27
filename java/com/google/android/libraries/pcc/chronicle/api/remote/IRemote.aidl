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

package com.google.android.libraries.pcc.chronicle.api.remote;

import com.google.android.libraries.pcc.chronicle.api.remote.IResponseCallback;
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequest;

// Definition of the Binder interface for a ChronicleService, used to implement
// low-level semantics for remote connections.
interface IRemote {
  // Serve a given request, expecting the response or error via the provided
  // callback.
  oneway void serve(in RemoteRequest request, IResponseCallback callback) = 1;
}
