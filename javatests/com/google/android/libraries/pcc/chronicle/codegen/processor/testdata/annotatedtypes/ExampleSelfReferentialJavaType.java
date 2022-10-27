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

import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData;
import java.util.List;

@ChronicleData
public class ExampleSelfReferentialJavaType {
  private final List<ExampleSelfReferentialJavaType> children;

  public ExampleSelfReferentialJavaType(List<ExampleSelfReferentialJavaType> children) {
    this.children = children;
  }

  public List<ExampleSelfReferentialJavaType> getChildren() {
    return children;
  }
}
