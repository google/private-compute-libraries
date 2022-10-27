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

package com.google.android.libraries.pcc.chronicle.tools.ledger

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonWriter
import java.io.StringWriter

/**
 * This file contains extensions to [JsonWriter] which help to fluently build JSON objects in
 * Kotlin.
 */
internal fun JsonWriter.makeObject(
  objectName: String? = null,
  block: JsonWriter.() -> Unit,
): JsonWriter {
  objectName?.let { name(it) }
  beginObject()
  block()
  return endObject()
}

/** Use this method instead of [makeObject] if the object can be collapsed to a single line. */
internal fun JsonWriter.makeSingleLineObject(
  objectName: String? = null,
  block: JsonWriter.() -> Unit,
): JsonWriter {
  val stringWriter = StringWriter()
  val jsonWriter = JsonWriter(stringWriter)
  objectName?.let { name(it) }

  jsonWriter.setIndent("")
  jsonWriter.beginObject()
  jsonWriter.block()
  jsonWriter.endObject()
  jsonWriter.flush()
  return jsonValue(stringWriter.buffer.toString())
}

internal fun JsonWriter.makeArray(
  arrayName: String? = null,
  block: JsonWriter.() -> Unit,
): JsonWriter {
  arrayName?.let { name(it) }
  beginArray()
  block()
  return endArray()
}

internal inline fun <reified T, R : Comparable<R>> JsonWriter.writeArray(
  gson: Gson,
  collection: Collection<T>,
  noinline sortKey: ((T) -> R)? = null,
): JsonWriter {
  return makeArray {
    (sortKey?.let { collection.sortedBy(sortKey) } ?: collection).forEach { writeField(gson, it) }
  }
}

internal fun JsonWriter.writeStringArray(
  @Suppress("UNUSED_PARAMETER") gson: Gson,
  collection: Collection<String>,
  sorted: Boolean = false
): JsonWriter {
  val stringWriter = StringWriter()
  val jsonWriter = JsonWriter(stringWriter)
  jsonWriter.setIndent("")
  jsonWriter.makeArray {
    (if (sorted) collection.sorted() else collection).forEach(jsonWriter::value)
  }
  jsonWriter.flush()
  return jsonValue(stringWriter.buffer.toString())
}

internal inline fun <reified T, R : Comparable<R>> JsonWriter.writeNamedArray(
  gson: Gson,
  arrayName: String,
  collection: Collection<T>,
  noinline sortKey: ((T) -> R)? = null,
): JsonWriter {
  if (collection.isEmpty()) return this
  name(arrayName)
  return writeArray(gson, collection, sortKey)
}

internal fun JsonWriter.writeNamedStringArray(
  gson: Gson,
  arrayName: String,
  collection: Collection<String>,
  sorted: Boolean = false,
): JsonWriter {
  if (collection.isEmpty()) return this
  name(arrayName)
  return writeStringArray(gson, collection, sorted)
}

internal fun <T> JsonWriter.writeMap(adapter: TypeAdapter<T>, map: Map<String, T>?): JsonWriter {
  if (map == null) return nullValue()
  if (map.isEmpty()) return this
  return makeObject {
    map.entries.sortedBy { it.key }.forEach { writeNamedField(adapter, it.key, it.value) }
  }
}

internal inline fun <reified T> JsonWriter.writeMap(gson: Gson, map: Map<String, T>?): JsonWriter =
  writeMap(gson.getAdapter(T::class.java), map)

internal fun <T> JsonWriter.writeNamedMap(
  adapter: TypeAdapter<T>,
  mapName: String,
  map: Map<String, T>
): JsonWriter {
  if (map.isEmpty()) return this
  name(mapName)
  return writeMap(adapter, map)
}

internal inline fun <reified T> JsonWriter.writeNamedMap(
  gson: Gson,
  mapName: String,
  map: Map<String, T>
): JsonWriter {
  if (map.isEmpty()) return this
  name(mapName)
  return writeMap(gson, map)
}

internal fun <T> JsonWriter.writeField(adapter: TypeAdapter<T>, value: T): JsonWriter {
  adapter.write(this, value)
  return this
}

internal inline fun <reified T> JsonWriter.writeField(gson: Gson, value: T): JsonWriter =
  writeField(gson.getAdapter(T::class.java), value)

internal inline fun <reified T> JsonWriter.writeNamedField(
  gson: Gson,
  name: String,
  value: T,
): JsonWriter {
  name(name)
  return writeField(gson, value)
}

internal fun <T> JsonWriter.writeNamedField(
  adapter: TypeAdapter<T>,
  name: String,
  value: T,
): JsonWriter {
  name(name)
  return writeField(adapter, value)
}
