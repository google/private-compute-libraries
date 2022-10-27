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

package com.google.android.libraries.pcc.chronicle.codegen.processor.testdata.annotatedtypes;

import com.google.android.libraries.pcc.chronicle.annotation.ChronicleConnection;
import com.google.android.libraries.pcc.chronicle.api.ReadConnection;

// Incorrectly passes a dataClass which is not annotated with @ChronicleData to
// @ChronicleConnection.
@ChronicleConnection(dataClass = Undefined.class)
public interface ErroneousJavaConnectionForUndefinedDataClass extends ReadConnection {}
