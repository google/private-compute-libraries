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
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken

/**
 * This file contains extensions to [JsonReader] which help to fluently parse JSON objects in
 * Kotlin.
 */
internal fun JsonReader.readObject(fieldVisitor: JsonReader.(name: String) -> Unit) {
  beginObject()
  val seenNames = mutableSetOf<String>()
  while (true) {
    if (peek() == JsonToken.END_OBJECT) break
    val name = nextName()
    if (name in seenNames) {
      throw IllegalArgumentException("Invalid JSON, key \"$name\" appeared more than once at $path")
    }
    seenNames += name
    fieldVisitor(name)
  }
  endObject()
}

internal inline fun <reified T : Any> JsonReader.readObject(gson: Gson): T? {
  return gson.getAdapter(T::class.java).read(this)
}

internal fun JsonReader.readList(itemVisitor: JsonReader.() -> Unit) {
  beginArray()
  while (true) {
    if (peek() == JsonToken.END_ARRAY) break
    itemVisitor()
  }
  endArray()
}

internal inline fun <reified T : Any> JsonReader.readList(gson: Gson): List<T> {
  val result = mutableListOf<T>()
  readList { result += gson.getAdapter(T::class.java).read(this) }
  return result
}

internal fun JsonReader.readStringList(): List<String> {
  val result = mutableListOf<String>()
  readList { result += nextString() }
  return result
}

internal inline fun <reified T : Any> JsonReader.readSet(gson: Gson): Set<T> {
  val result = mutableSetOf<T>()
  beginArray()
  while (true) {
    if (peek() == JsonToken.END_ARRAY) break
    result += gson.getAdapter(T::class.java).read(this)
  }
  endArray()
  return result
}

internal fun <T> JsonReader.readMap(valueAdapter: TypeAdapter<T>): Map<String, T> {
  val result = mutableMapOf<String, T>()
  readObject { fieldName ->
    val value = valueAdapter.read(this)
    result[fieldName] = value
  }
  return result
}

internal inline fun <reified T : Any> JsonReader.readMap(gson: Gson): Map<String, T> =
  readMap(gson.getAdapter(T::class.java))
