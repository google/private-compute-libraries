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

# Private Compute Libraries

[![Build Status](https://circleci.com/github/google/private-compute-libraries.svg?style=svg)](https://app.circleci.com/pipelines/github/google/private-compute-libraries?branch=main)

This repository contains source code for the following libraries, which work
together under the name "Private Compute Libraries":

* Chronicle
* Plugins

## Building & Testing

Although there should be no reason why the Private Compute Libraries shouldn't
build and pass tests on any operating system supported by Bazel, the following
build and test instructions assume the environment to be Debian Linux.

### Install Android SDK 31

Install [Android Studio](https://developer.android.com/studio) and Android SDK
31 from the [SDK Manager](https://developer.android.com/studio/intro/update#sdk-manager).

Ensure your `ANDROID_HOME` environment variable is [configured correctly](https://developer.android.com/studio/command-line/variables).

### Install Bazel and OpenJDK 11

```bash
sudo apt update
sudo apt install default-jdk bazel
```

### Clone the Git Repository

```bash
git clone git@github.com:google/private-compute-libraries.git
cd private-compute-libraries
```

### Build & Test Locally

```bash
bazel build //...
bazel test //...
```

**Note:** If your machine has a nonstandard default JDK, you may need to add a
flag to the above commands: `--java_runtime_version=local-openjdk11`
