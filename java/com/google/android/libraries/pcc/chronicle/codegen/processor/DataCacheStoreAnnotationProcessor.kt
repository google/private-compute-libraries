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

import com.google.android.libraries.pcc.chronicle.annotation.DataCacheStore
import com.google.android.libraries.pcc.chronicle.api.DeletionTrigger
import com.google.android.libraries.pcc.chronicle.api.ManagementStrategy
import com.google.android.libraries.pcc.chronicle.api.StorageMedia
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.codegen.backend.ManagedDataCacheStorageDaggerProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.DaggerModuleProvider
import com.google.android.libraries.pcc.chronicle.codegen.util.upperSnake
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import java.time.Duration
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * This annotation processor will operate on any type annotated with the [DataCacheStore]
 * annotation. For a type `TypeName`, it will create a file `TypeName_Generated_Storage.kt`,
 * containing a top-level property `TypeName_GENERATED_MANAGEMENT_STRATEGY` containing the generated
 * [ManagementStrategy] object.
 */
class DataCacheStoreAnnotationProcessor : AnnotationProcessor() {
  override fun getSupportedAnnotationTypes() = setOf(DataCacheStore::class.java.canonicalName!!)

  override fun process(elements: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
    env.getElementsAnnotatedWith(DataCacheStore::class.java).forEach { element ->
      val name = element.simpleName.toString()
      val storage =
        try {
          element.storage()
        } catch (e: IllegalArgumentException) {
          printError(element, e.message ?: "unknown")
          return false
        }
      element.generateFile(name, storage).writeTo(processingEnv.filer)

      val daggerModuleProvider =
        try {
          DaggerModuleProvider(
            name = "${name}GeneratedStorageProviderModule",
            contents =
              listOf(
                ManagedDataCacheStorageDaggerProvider(
                  elementName = name,
                  chronicleDataType = TypeName.get(element.asType()),
                  maxItems = element.getAnnotation(DataCacheStore::class.java).maxItems,
                  ttlDuration = ttlDuration(element.ttlString())
                )
              )
          )
        } catch (e: IllegalArgumentException) {
          printError(element, e.message ?: "unknown")
          return false
        }

      JavaFile.builder(element.packageName, daggerModuleProvider.provideModule())
        .build()
        .writeTo(processingEnv.filer)
    }
    return true
  }

  private fun Element.storage(): CodeBlock {
    return CodeBlock.builder()
      .add("%T(", ManagementStrategy.Stored::class)
      .add("encrypted = %L, ", false)
      .add(
        "media = %T.%L, ",
        StorageMedia::class,
        getAnnotation(DataCacheStore::class.java).storageMedia
      )
      .add(ttlKotlin())
      .add(
        "deletionTriggers = setOf(%T(%T.%L, %S)),",
        DeletionTrigger::class,
        Trigger::class,
        Trigger.PACKAGE_UNINSTALLED,
        "packageName"
      )
      .add(")")
      .build()
  }

  private fun Element.ttlKotlin(): CodeBlock {
    val duration: Duration = ttlDuration(ttlString())
    val durationParse = MemberName("java.time.Duration", "parse")
    return CodeBlock.of("ttl = %M(%S), ", durationParse, duration)
  }

  private fun Element.ttlString(): String {
    return getAnnotation(DataCacheStore::class.java).ttl.ifEmpty {
      printWarning(
        this,
        "Empty TTL string, using default $DEFAULT_TTL_STRING for generated storage"
      )
      DEFAULT_TTL_STRING
    }
  }

  /**
   * Generate the wrapper Kotlin file that will hold the generated ManagementStrategy and MaxItems.
   *
   * Looks like:
   * ```
   * package <packageName>
   *
   * import <needed imports>
   *
   * val TypeName_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY: ManagementStrategy =
   *     ManagementStrategy.Stored(encrypted = , media = StorageMedia.MEMORY, ttl = <>,
   *     deletionTriggers = setOf(DeletionTrigger(Trigger.PACKAGE_UNINSTALLED, "packageName")),)
   *
   * val TypeName_GENERATED_MAX_ITEMS: Int = <>
   * ```
   */
  private fun Element.generateFile(name: String, storage: CodeBlock): FileSpec {
    return FileSpec.builder(packageName, outStorageFileName)
      .addProperty(managementStrategyPropertySpec(name, storage))
      .addProperty(maxItems(name))
      .build()
  }

  /** Get the output storage filename of the receiving [Element]. */
  private val Element.outStorageFileName: String
    get() = "${this.simpleName}_Generated_Storage"

  /**
   * Generate the [PropertySpec] representing the ManagementStrategy based on the supplied
   * [CodeBlock].
   */
  private fun Element.managementStrategyPropertySpec(
    name: String,
    storage: CodeBlock
  ): PropertySpec {
    val genManagementStrategyName =
      "${name.upperSnake()}_GENERATED_DATA_CACHE_STORE_MANAGEMENT_STRATEGY"
    printNote(this, "Generating $genManagementStrategyName")

    return PropertySpec.builder(genManagementStrategyName, ManagementStrategy::class)
      .addAnnotation(JvmField::class)
      .initializer(storage)
      .build()
  }

  /**
   * Generate the [PropertySpec] representing the maxItems in memory from the annotation argument.
   */
  private fun Element.maxItems(name: String): PropertySpec {
    val genMaxItemsName = "${name.upperSnake()}_GENERATED_MAX_ITEMS"
    printNote(this, "Generating $genMaxItemsName")

    return PropertySpec.builder(genMaxItemsName, Int::class)
      .addAnnotation(JvmField::class)
      .initializer("%L", getAnnotation(DataCacheStore::class.java).maxItems)
      .build()
  }

  companion object {
    private const val DEFAULT_TTL_STRING = "0ms"
    private val DURATION_UNIT_PATTERN = "(\\d+)(d|h|s|ms|m|us)".toRegex()
    private val VALID_FORMAT_STRING =
      "Days (d), hours (h), minutes (m), seconds (s), milliseconds (ms), microseconds (us)"

    /** Parse the TTL String, e.g. "2d4h3m2s", and construct a corresponding [Duration]. */
    fun ttlDuration(ttlString: String): Duration {
      return try {
        val (duration, consumed) =
          DURATION_UNIT_PATTERN.findAll(ttlString).fold(Duration.ZERO to emptySet<Int>()) {
            (acc, consumed),
            match ->
            val amount = match.groupValues[1].toLong(10)
            val duration =
              when (val unit = match.groupValues[2]) {
                "d" -> Duration.ofDays(amount)
                "h" -> Duration.ofHours(amount)
                "m" -> Duration.ofMinutes(amount)
                "s" -> Duration.ofSeconds(amount)
                "ms" -> Duration.ofMillis(amount)
                "us" -> Duration.ofNanos(amount * 1000)
                else ->
                  throw IllegalArgumentException("Invalid unit: $unit, for ttl string: $ttlString")
              }
            acc.plus(duration) to (consumed + match.range)
          }
        require(consumed.size == ttlString.length) {
          "Ttl string: $ttlString not completely consumable during parsing, " +
            "expected units are $VALID_FORMAT_STRING"
        }
        duration
      } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Invalid TTL duration string: $ttlString", e)
      }
    }
  }
}
