<!--
 Copyright 2022 Google LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# People Datahub Sample (Protobuf)

This entry in the Datahub provides an example of using Protocol Buffers to
define data managed by policy in Chronicle.

## Why Protobuf?

The main reason feature developers should consider using Protocol Buffers to
define their data is because it makes supporting remote connections to your data
store far simpler than otherwise. Protocol Buffers have a well documented path
to supporting backwards and forwards compatibility in distributed computing,
they are inherently serializable, and are self-documenting.

## Package Structure

* `client/`
   Defines a client-side ConnectionProvider which uses a RemoteStoreClient to
   communicate remotely with the server managing the actual storage of the
   People data.
* `server/`
   Hosts the actual implementation of the storage back-end for the People
   protos. The implementation here provides a RemoteStoreServer implementation
   as well as server-local connections via the ConnectionpProvider
   implementation.
* `proxy/`
   If the datasteward hosts the actual data storage (the `server/`) on an
   application that is not directly accessible from the `client`, the `proxy`
   will live on an intermediary application which _does_ have the ability to
   connect to the server.
* `person.proto`
   Defines a type of data called `Person` which will be policy-guarded by
   Chronicle.
* `PeopleReader.kt`
   A ReadConnection used to fetch `Person` data.
* `PeopleWriter.kt`
   A WriteConnection which can be used to create, update, or delete `Person`
   data.
