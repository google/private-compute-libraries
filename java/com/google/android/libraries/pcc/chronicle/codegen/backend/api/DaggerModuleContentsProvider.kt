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

package com.google.android.libraries.pcc.chronicle.codegen.backend.api

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Represents a class capable of providing contents into a [TypeSpec.Builder] when generating a
 * dagger module.
 */
sealed interface DaggerModuleContentsProvider {
  /** Provides the contents into the [moduleSpec]. */
  fun provideContentsInto(moduleSpec: TypeSpec.Builder)

  /**
   * A [DaggerModuleContentsProvider] which generates an `@Provides` method for a dagger module.
   *
   * @param name the name of the provides method.
   * @param providedType the [TypeName] of the value provided by the `@Provides` method to the
   *   dagger dependency graph.
   * @param isSingleton whether or not the `@Provides` method should also be annotated with
   *   `@Singleton`.
   * @param isIntoSet whether or not the `@Provides` method should also be annoated with `@IntoSet`.
   * @param qualifierAnnotations list of [ClassNames][ClassName] for additional dependency injection
   *   qualifier annotations to include on the generated method.
   */
  abstract class ProvidesMethod(
    val name: String,
    val providedType: TypeName,
    val isSingleton: Boolean,
    val isIntoSet: Boolean = false,
    val qualifierAnnotations: List<ClassName> = emptyList(),
  ) : DaggerModuleContentsProvider {
    /**
     * List of dependencies required for the dagger provider, as parameters for the provides method.
     */
    abstract val parameters: List<ParameterSpec>

    /** Provides the body of the `@Provides` method. */
    abstract fun provideBody(): CodeBlock

    override fun provideContentsInto(moduleSpec: TypeSpec.Builder) {
      val method =
        MethodSpec.methodBuilder(name)
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addParameters(parameters)
          .returns(providedType)
          .addAnnotation(PROVIDES_ANNOTATION_CLASS_NAME)
          .also {
            if (isSingleton) it.addAnnotation(SINGLETON_ANNOTATION_CLASS_NAME)
            if (isIntoSet) it.addAnnotation(INTOSET_ANNOTATION_CLASS_NAME)
            qualifierAnnotations.forEach { qualifier -> it.addAnnotation(qualifier) }
          }
          .addCode(provideBody())
          .build()
      moduleSpec.addMethod(method)
    }
  }

  companion object {
    /** Dependency injection qualifier for `@PrivacyReviewed`. */
    val PRIVACY_REVIEWED_QUALIFIER =
      ClassName.get("com.google.android.libraries.pcc.chronicle.api.qualifier", "PrivacyReviewed")

    private val PROVIDES_ANNOTATION_CLASS_NAME = ClassName.get("dagger", "Provides")
    private val SINGLETON_ANNOTATION_CLASS_NAME = ClassName.get("javax.inject", "Singleton")
    private val INTOSET_ANNOTATION_CLASS_NAME = ClassName.get("dagger.multibindings", "IntoSet")
  }
}
