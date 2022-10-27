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

import android.app.assist.ActivityId;
import android.os.IBinder;
import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData;

@ChronicleData
public class ExampleJavaOpaqueType {
  private final String name;
  private final IBinder iBinder;
  private final ActivityId activityId;

  public ExampleJavaOpaqueType(String name, IBinder iBinder, ActivityId activityId) {
    this.name = name;
    this.iBinder = iBinder;
    this.activityId = activityId;
  }

  public String getName() {
    return name;
  }

  public IBinder getIBinder() {
    return iBinder;
  }

  public ActivityId getActivityId() {
    return activityId;
  }
}
