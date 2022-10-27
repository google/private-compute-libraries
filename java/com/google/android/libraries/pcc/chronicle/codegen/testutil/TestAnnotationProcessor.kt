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

package com.google.android.libraries.pcc.chronicle.codegen.testutil

import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class TestAnnotation

/**
 * A trivial processor to run tests that operate on [Element] types provided to an * annotation
 * processor corresponding to a test-provided [Class].
 *
 * This is useful for testing *reusable components* that are to be composed into an annotation
 * processor.
 *
 * In your annotation test, instantiate the processor, and provide the instance to a
 * [Compiler.compile] call:
 *
 * ```
 *   val processor = TestAnnotationProcessor { processingEnv, element ->
 *     return element.simpleName
 *   }
 *
 *   val resultingName = processor.runAnnotationTestForType(SomeType::class.java)
 *
 *   // resultingName should now contain the string "SomeType"
 * ```
 */
class TestAnnotationProcessor<T>(
  private val processor: (ProcessingEnvironment, Element) -> T
) : AbstractProcessor() {
  override fun getSupportedAnnotationTypes() = setOf(TestAnnotation::class.java.canonicalName)
  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

  private val results = mutableMapOf<Element, T>()

  override fun process(
    annotations: MutableSet<out TypeElement>,
    env: RoundEnvironment
  ): Boolean {
    try {
      env.getElementsAnnotatedWith(TestAnnotation::class.java).forEach {
        results[it] = processor(processingEnv, it)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }
    return true
  }

  /**
   * Creates and compiles a test snippet of code that includes a field of the
   * provided type with a @TestAnnotation.
   *
   * This triggers the processor to execute an instances of the provided [processorCreate].
   * The results of the run will be returned from this method.
   */
  fun runAnnotationTestForType(typeClass: KClass<*>): T {
    val compilerResult = Compiler.javac()
      .withProcessors(this)
      .compile(
        JavaFileObjects.forSourceString(
          "Test",
          codeSnippetForTypeWithTestAnnotation(typeClass)
        )
      )
    require(compilerResult.errors().isEmpty())
    return results.values.first()
  }

  companion object {
    /**
     * This creates a code snippet that can be passed to a compiler call, it will trigger
     * annotation processing for the provided type as the root element.
     */
    private fun codeSnippetForTypeWithTestAnnotation(type: KClass<*>) =
      """
      import com.google.android.libraries.pcc.chronicle.codegen.testutil.TestAnnotation;
      import ${type.qualifiedName};
      class Test {
        @TestAnnotation
        ${type.simpleName} thing;
      }
      """.trimIndent()
  }
}
