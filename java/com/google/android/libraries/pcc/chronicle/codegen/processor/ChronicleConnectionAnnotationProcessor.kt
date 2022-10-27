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

import com.google.android.libraries.pcc.chronicle.annotation.ChronicleConnection
import com.google.android.libraries.pcc.chronicle.annotation.ChronicleData
import com.google.android.libraries.pcc.chronicle.codegen.backend.ConnectionProviderTypeProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.ConnectionsPropertyProvider
import com.google.android.libraries.pcc.chronicle.codegen.backend.api.FileSpecContentsProvider
import com.google.auto.service.AutoService
import com.google.errorprone.annotations.CheckReturnValue
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * This annotation processor will operate on any type annotated with the [ChronicleConnection]
 * annotation. For each dataClass `TypeName`, it will create a file
 * `TypeName_Generated_Connections.kt`, containing a top-level property
 * `TypeName_GENERATED_CONNECTIONS` containing the generated set of Connections corresponding to
 * that dataClass.
 *
 * This annotation processor will also generate a file `TypeName_Generated_Connection_Provider.kt`
 * containing implementations for each Connection class annotated with [ChronicleConnection], and a
 * `TypeNameConnectionProvider` class.
 */
@AutoService(ChronicleConnectionAnnotationProcessor::class)
class ChronicleConnectionAnnotationProcessor : AnnotationProcessor() {
  private val connectionTypeMirror: TypeMirror by lazy {
    processingEnv.elementUtils.getTypeElement(CONNECTION_CLASS_PATH).asType()
  }

  private val readConnectionTypeMirror: TypeMirror by lazy {
    processingEnv.elementUtils.getTypeElement(READ_CONNECTION_CLASS_PATH).asType()
  }

  private val writeConnectionTypeMirror: TypeMirror by lazy {
    processingEnv.elementUtils.getTypeElement(WRITE_CONNECTION_CLASS_PATH).asType()
  }

  override fun getSupportedAnnotationTypes() =
    setOf(ChronicleConnection::class.java.canonicalName!!)

  override fun process(elements: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
    val collectionOfDataToProcessResult = validateAndCollectDataToProcess(env)
    if (collectionOfDataToProcessResult.isFailure) return false

    val collectionOfDataToProcess = collectionOfDataToProcessResult.getOrThrow()
    generateConnectionsCode(collectionOfDataToProcess.forConnections())
    generateConnectionProviderCode(collectionOfDataToProcess.forConnectionProvider())

    return true
  }

  @CheckReturnValue
  private fun validateAndCollectDataToProcess(
    env: RoundEnvironment
  ): Result<CollectionOfDataToProcess> {
    val collectionOfDataToProcess =
      env.getElementsAnnotatedWith(ChronicleConnection::class.java).map {
        try {
          DataToProcess(it)
        } catch (e: IllegalArgumentException) {
          printError(it, e.message ?: "unknown")
          return Result.failure(e)
        }
      }
    return Result.success(CollectionOfDataToProcess(collectionOfDataToProcess))
  }

  inner class DataToProcess(val element: Element) {
    init {
      element.validateConnection()
    }
    val dataClass = element.validateAndExtractDataClass()
    val generateConnectionProviderCode: Boolean =
      element.getAnnotation(ChronicleConnection::class.java).generateConnectionProvider
  }

  inner class CollectionOfDataToProcess(val collectionOfData: List<DataToProcess>) {
    fun forConnections(): Map<Element, Set<TypeName>> =
      collectionOfData.groupBy({ it.dataClass }, { it.element.asType().asTypeName() }).mapValues {
        it.value.toSet()
      }

    fun forConnectionProvider(): List<Triple<Element, List<TypeMirror>, List<TypeMirror>>> =
      collectionOfData
        .filter { it.generateConnectionProviderCode }
        .groupBy { it.dataClass }
        .map { entry ->
          // `entry.value` represents a collection of DataToProcess, each of which has an `element`
          // that must be either ReadConnection or WriteConnection from
          // `element.validateConnection()`
          val (readers, writers) = entry.value.partition { isReadConnection(it.element.asType()) }
          Triple(
            entry.key,
            readers.map { it.element.asType() },
            writers.map { it.element.asType() }
          )
        }
  }

  /**
   * Check that element is a Connection, and specifically a ReadConnection, WriteConnection, or
   * both, and throw an exception otherwise.
   */
  private fun Element.validateConnection() {
    require(processingEnv.typeUtils.isAssignable(asType(), connectionTypeMirror)) {
      "Element $this is not a Connection"
    }

    require(isReadConnection(asType()) || isWriteConnection(asType())) {
      "Element $this is neither a ReadConnection nor a WriteConnection"
    }
  }

  private fun isReadConnection(typeMirror: TypeMirror?): Boolean =
    processingEnv.typeUtils.isAssignable(typeMirror, readConnectionTypeMirror)

  private fun isWriteConnection(typeMirror: TypeMirror?): Boolean =
    processingEnv.typeUtils.isAssignable(typeMirror, writeConnectionTypeMirror)

  // @CheckReturnValue
  private fun Element.validateAndExtractDataClass(): Element =
    getDataClassArgument().asTypeElement().apply {
      requireNotNull(getAnnotation(ChronicleData::class.java)) {
        "data class $this is not annotated with @ChronicleData"
      }
    }

  private fun Element.getDataClassArgument(): TypeMirror {
    // We rely on string comparison of TypeMirrors to avoid "Attempt to access Class object for
    // TypeMirror" when trying to access the Class-argument connections. See this blog post for
    // context
    // https://area-51.blog/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
    val annotationMirrors: List<AnnotationMirror> = annotationMirrors
    // elementValues should never be null. Every element that lands here should have a
    // @ChronicleConnection annotation.
    val elementValues: Map<out ExecutableElement, AnnotationValue> =
      requireNotNull(
          annotationMirrors.find {
            it.annotationType.toString() == ChronicleConnection::class.java.typeName
          }
        )
        .elementValues

    val dataClassAnnotationValue =
      requireNotNull(
        elementValues.asIterable().find { it.key.simpleName.toString() == "dataClass" }?.value
      )

    return requireNotNull(dataClassAnnotationValue.value as? TypeMirror) {
      "Couldn't convert dataClass arg $dataClassAnnotationValue to TypeMirror, the type may be " +
        "undefined or not imported"
    }
  }

  private fun TypeMirror.asTypeElement() = processingEnv.typeUtils.asElement(this)

  private fun generateConnectionsCode(dataClassToConnections: Map<Element, Set<TypeName>>) {
    for ((dataClassElement, connectionsCollection) in dataClassToConnections) {
      printNote(dataClassElement, "Generating connections for $dataClassElement")
      val connections =
        ConnectionsPropertyProvider(dataClassElement.simpleName, connectionsCollection)
      dataClassElement.generateConnectionsFile(connections).writeTo(processingEnv.filer)
    }
  }

  private fun Element.generateConnectionsFile(connections: FileSpecContentsProvider): FileSpec {
    return FileSpec.builder(packageName, "${this.simpleName}$FILE_SUFFIX_FOR_CONNECTIONS")
      .also { connections.provideContentsInto(it) }
      .build()
  }

  private fun generateConnectionProviderCode(
    dataClassToReadConnectionsToWriteConnections:
      List<Triple<Element, List<TypeMirror>, List<TypeMirror>>>
  ) {
    for ((dataClass, readers, writers) in dataClassToReadConnectionsToWriteConnections) {
      printNote(dataClass, "Generating ConnectionProvider for $dataClass")
      val connectionProvider =
        ConnectionProviderTypeProvider(
          dataClass.asType().asTypeName(),
          readers.map { it.asTypeName() },
          writers.map { it.asTypeName() }
        )
      dataClass.generateConnectionProviderFile(connectionProvider).writeTo(processingEnv.filer)
    }
  }

  private fun Element.generateConnectionProviderFile(
    connectionProvider: FileSpecContentsProvider
  ): FileSpec {
    return FileSpec.builder(packageName, "${this.simpleName}$FILE_SUFFIX_FOR_CONNECTION_PROVIDER")
      .also { connectionProvider.provideContentsInto(it) }
      .build()
  }

  companion object {
    private const val CONNECTION_CLASS_PATH =
      "com.google.android.libraries.pcc.chronicle.api.Connection"
    private const val READ_CONNECTION_CLASS_PATH =
      "com.google.android.libraries.pcc.chronicle.api.ReadConnection"
    private const val WRITE_CONNECTION_CLASS_PATH =
      "com.google.android.libraries.pcc.chronicle.api.WriteConnection"
    private const val FILE_SUFFIX_FOR_CONNECTIONS = "_Generated_Connections"
    private const val FILE_SUFFIX_FOR_CONNECTION_PROVIDER = "_Generated_Connection_Provider"
  }
}
