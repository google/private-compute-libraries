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

package com.google.android.libraries.pcc.chronicle.codegen.processor

import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.Compilation
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import java.time.Duration
import javax.tools.JavaFileObject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Test compilation of annotated source files using the [ChronicleDataAnnotationProcessor],
 * [DataCacheStoreAnnotationProcessor], and [ChronicleConnectionAnnotationProcessor]. By performing
 * compilation at runtime, this test is able to check erroneous source code and verify that
 * compilation fails with expected errors.
 */
@RunWith(JUnit4::class)
class ChronicleDataAnnotationProcessorCompileTest {

  /**
   * Loads a resource file relative to the location of this test source. File must be included in
   * the 'resources' BUILD file directive for this test.
   */
  private fun getFile(pathToFile: String): JavaFileObject {
    return JavaFileObjects.forResource(
      Resources.getResource(ChronicleDataAnnotationProcessorCompileTest::class.java, pathToFile)
    )
  }

  /** Compiles the given file with our additional processors. */
  private fun compile(vararg pathToFile: String): Compilation {
    return javac()
      .withProcessors(
        ChronicleDataAnnotationProcessor(),
        DataCacheStoreAnnotationProcessor(),
        ChronicleConnectionAnnotationProcessor()
      )
      .compile(pathToFile.map { getFile(it) })
  }

  // @ChronicleConnection test cases

  @Test
  fun processChronicleDataAnnotationForUndefinedDataClass_expectError() {
    val compilation: Compilation =
      compile("testdata/annotatedtypes/ErroneousJavaConnectionForUndefinedDataClass.java")

    assertThat(compilation)
      .hadErrorContaining("Couldn't convert dataClass arg <error> to TypeMirror")
  }

  @Test
  fun processChronicleConnectionAnnotationForNonConnection_expectError() {
    val compilation: Compilation =
      compile("testdata/annotatedtypes/ErroneousJavaNonConnection.java")

    assertThat(compilation).hadErrorContaining("is not a Connection")
  }

  @Test
  fun processChronicleConnectionAnnotationForGenericConnection_expectError() {
    val compilation: Compilation =
      compile("testdata/annotatedtypes/ErroneousJavaGenericConnection.java")

    assertThat(compilation).hadErrorContaining("is neither a ReadConnection nor a WriteConnection")
  }

  @Test
  fun processChronicleConnectionAnnotationForNonDataClass_expectError() {
    val compilation: Compilation =
      compile("testdata/annotatedtypes/ErroneousJavaConnectionForNonDataClass.java")

    assertThat(compilation).hadErrorContaining("is not annotated with @ChronicleData")
  }

  // @DataCacheStore test cases

  @Test
  fun processDataCacheStoreAnnotationForMissingTTL_expectError() {
    val compilation: Compilation =
      compile("testdata/annotatedtypes/ErroneousJavaTypeForMissingTtl.java")

    assertThat(compilation)
      .hadErrorContaining("DataCacheStore is missing a default value for the element 'ttl'")
  }

  @Test
  fun processDataCacheStoreAnnotationForMissingMaxItems_expectError() {
    val compilation: Compilation =
      compile("testdata/annotatedtypes/ErroneousJavaTypeForMissingMaxItems.java")

    assertThat(compilation)
      .hadErrorContaining("DataCacheStore is missing a default value for the element 'maxItems'")
  }

  @Test
  fun processDataCacheStoreAnnotationForInvalidTTL_expectError() {
    val compilation: Compilation =
      compile("testdata/annotatedtypes/ErroneousJavaTypeForInvalidTtl.java")

    assertThat(compilation).hadErrorContaining("not completely consumable during parsing")
    assertThat(compilation)
      .hadErrorContaining(
        "Days (d), hours (h), minutes (m), seconds (s), milliseconds (ms), microseconds (us)"
      )
  }

  @Test
  fun parseValidTTLStrings_expectCorrectCodeBlock() {
    val ttlStringsToCode =
      mapOf(
        "0d" to Duration.ofDays(0),
        "11h" to Duration.ofHours(11),
        "222m" to Duration.ofMinutes(222),
        "3333s" to Duration.ofSeconds(3333),
        "44444ms" to Duration.ofMillis(44444),
      )
    for ((ttlString, expectedDuration) in ttlStringsToCode) {
      val duration: Duration = DataCacheStoreAnnotationProcessor.ttlDuration(ttlString)
      assertThat(duration).isEqualTo(expectedDuration)
    }
  }
}
