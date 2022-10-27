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

import com.google.android.libraries.pcc.chronicle.api.remote.ICancellationSignal;
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteError;
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteResponse;

// Defines a binder interface passed to ChronicleService via the IRemote#serve
// endpoint.
interface IResponseCallback {
  // Called when the RemoteServer has response data for the request this
  // callback was associated with.
  oneway void onData(in RemoteResponse data) = 1;

  // Called when the ChronicleService / RemoteServer experienced an error during
  // request fulfillment.
  oneway void onError(in RemoteError error) = 2;

  // Called when the RemoteServer has finished sending response data via onData.
  oneway void onComplete() = 3;

  // Called by the ChronicleService, passing a cancellation signal object which
  // allows the requester to cancel ongoing processing. This will halt
  // operations in the RemoteServer responding to the original request.
  oneway void provideCancellationSignal(ICancellationSignal signal) = 4;
}
