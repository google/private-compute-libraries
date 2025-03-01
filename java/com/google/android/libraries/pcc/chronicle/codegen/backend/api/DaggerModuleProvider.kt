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

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Tool to aide in generating dagger modules as abstract java classes.
 *
 * **Example:**
 *
 * ```kotlin
 * DaggerModuleProvider("MyModule", contents = myListOfContentsProviders)
 *   .provideModule()
 * ```
 *
 * Generates:
 * ```java
 * @Module
 * @InstallIn(SingletonComponent.class)
 * public abstract class MyModule {
 *   /* provides/binds methods from myListOfContentsProviders */
 * }
 * ```
 *
 * @param name the name of the module class itself.
 * @param installInComponent the hilt component the generated module should be installed into.
 * @param contents list of [DaggerModuleContentsProviders][DaggerModuleContentsProvider],
 *   responsible for generating the individual provides/binds methods for the generated module.
 */
class DaggerModuleProvider(
  private val name: String,
  private val installInComponent: ClassName = SINGLETON_COMPONENT,
  private val contents: List<DaggerModuleContentsProvider>,
) {
  /** Generates a [TypeSpec] of the dagger module to use in creating a [JavaFile]. */
  fun provideModule(): TypeSpec {
    return TypeSpec.classBuilder(name)
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .addAnnotation(MODULE_ANNOTATION)
      .addAnnotation(
        AnnotationSpec.builder(INSTALL_IN_ANNOTATION)
          .addMember("value", "\$T.class", installInComponent)
          .build()
      )
      .apply { contents.forEach { it.provideContentsInto(this) } }
      .build()
  }

  companion object {
    /** [ClassName] of the hilt SingletonComponent. */
    val SINGLETON_COMPONENT: ClassName =
      ClassName.get("dagger.hilt.components", "SingletonComponent")

    private val MODULE_ANNOTATION = ClassName.get("dagger", "Module")
    private val INSTALL_IN_ANNOTATION = ClassName.get("dagger.hilt", "InstallIn")
  }
}
