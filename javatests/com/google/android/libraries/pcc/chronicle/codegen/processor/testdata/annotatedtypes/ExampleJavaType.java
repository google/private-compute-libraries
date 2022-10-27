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

import android.util.Pair;
import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData;
import com.google.android.libraries.pcc.chronicle.annotation.DataCacheStore;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@AutoValue
@ChronicleData
@DataCacheStore(ttl = "2d", maxItems = 1000)
public abstract class ExampleJavaType {
  public enum Status {
    ON,
    OFF,
    UNKNOWN,
  }

  public static ExampleJavaType create(
      String name,
      int amount,
      ImmutableList<String> others,
      Instant updateTime,
      Duration timeSinceLastOpen,
      ImmutableMap<String, Boolean> featuresEnabled,
      Optional<String> nickName,
      Pair<String, Double> categoryAndScore,
      Status status) {
    return new AutoValue_ExampleJavaType(
        name,
        amount,
        others,
        updateTime,
        timeSinceLastOpen,
        featuresEnabled,
        nickName,
        categoryAndScore,
        status);
  }

  public abstract String name();

  public abstract int amount();

  public abstract ImmutableList<String> others();

  public abstract Instant updateTime();

  public abstract Duration timeSinceLastOpen();

  public abstract ImmutableMap<String, Boolean> featuresEnabled();

  public abstract Optional<String> nickName();

  public abstract Pair<String, Double> categoryAndScore();

  public abstract Status status();
}
