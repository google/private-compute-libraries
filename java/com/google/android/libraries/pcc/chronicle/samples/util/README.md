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

# Samples Utils

This package contains utilities that are re-used between various sample
applications. Some of these utilities may eventually be promoted to parts of the
actual Chronicle API with enough interest in their reuse.

## `ChronicleHelper`

An encapsulation of much of the details required to stand Chronicle up for use
in an application. It provides access to an instance of `Chronicle` as well as
the `IBinder` interface that should be returned from a remote connections
service's `onBind(Intent)` method: `IRemote`.

### Why isn't it part of the supported public API yet?

Many applications choosing to integrate with Chronicle rely on dependency
injection to enable various build variants of their apps to have different
compositions. The current users of Chronicle have found it useful to provide
their own versions of something like `ChronicleHelper` which are built via
dependency injection frameworks with more "levers" to toggle based on
alpha/beta/prod build variants.
