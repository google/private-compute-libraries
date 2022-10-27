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

package com.google.android.libraries.pcc.chronicle.remote

import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.remote.RemoteRequestMetadata
import com.google.android.libraries.pcc.chronicle.api.remote.server.RemoteServer

/**
 * A [RemotePolicyChecker] is responsible for checking policies as they pertain to a given remote
 * request (described by [RemoteRequestMetadata]).
 */
interface RemotePolicyChecker {
  /**
   * Finds the policy associated with the request (as defined by the [metadata]) and checks that
   * policy, using the provided [server] as if it were a [ConnectionProvider] and the
   * [clientDetails] for any additional information about the remote client that pertains to policy
   * checking.
   *
   * If there a) was a policy associated with the request, and b) the policy check was successful:
   * we return the policy.
   *
   * If there was no policy associated with the request, and if that's okay: we return null.
   *
   * If policy checking failed, or the policy referenced by the [RemoteRequestMetadata] was not
   * found - we throw.
   */
  fun checkAndGetPolicyOrThrow(
    metadata: RemoteRequestMetadata,
    server: RemoteServer<*>,
    clientDetails: ClientDetails,
  ): Policy?
}
