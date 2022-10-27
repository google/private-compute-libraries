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

import com.google.android.libraries.pcc.chronicle.api.policy.FieldName
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyConfig
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyRetention
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyTarget
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.Annotation
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.AnnotationParam
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.StringWriter

/** Collection of GSON [TypeAdapter] instances for converting [Policy] classes to JSON. */
internal object PolicyJson {
  object PolicyTypeAdapter : TypeAdapter<Policy>() {
    override fun write(out: JsonWriter, value: Policy?) {
      if (value == null) {
        out.nullValue()
        return
      }
      out.makeObject {
        writeNamedField(DataHubLedger.gson, "name", value.name)
        writeNamedField(DataHubLedger.gson, "egressType", value.egressType)
        if (value.description.isNotEmpty()) {
          writeNamedField(DataHubLedger.gson, "description", value.description)
        }
        writeNamedArray(DataHubLedger.gson, "targets", value.targets) { it.schemaName }
        writeNamedMap(DataHubLedger.gson, "configs", value.configs)
        writeNamedArray(DataHubLedger.gson, "annotations", value.annotations) { it.name }
        writeNamedField(DataHubLedger.gson, "allowedContext", value.allowedContext)
      }
    }

    override fun read(reader: JsonReader): Policy? {
      val type = reader.peek()
      if (type == JsonToken.NULL) return null

      var name = ""
      var egressType = ""
      var description = ""
      var targets = emptyList<PolicyTarget>()
      var configs = emptyMap<String, PolicyConfig>()
      var annotations = emptyList<Annotation>()
      var allowedContext: PolicyContextRule = PolicyContextRuleJson.ContextRule()

      reader.readObject { fieldName ->
        when (fieldName) {
          "name" -> name = reader.nextString()
          "egressType" -> egressType = reader.nextString()
          "description" -> description = reader.nextString()
          "targets" -> targets = reader.readList(DataHubLedger.gson)
          "configs" -> configs = reader.readMap(DataHubLedger.gson)
          "annotations" -> annotations = reader.readList(DataHubLedger.gson)
          "allowedContext" -> allowedContext = reader.readObject(DataHubLedger.gson)
        }
      }
      return Policy(name, egressType, description, targets, configs, annotations, allowedContext)
    }
  }

  object PolicyTargetTypeAdapter : TypeAdapter<PolicyTarget>() {
    override fun write(out: JsonWriter, value: PolicyTarget?) {
      if (value == null) {
        out.nullValue()
        return
      }
      out.makeObject {
        writeNamedField(DataHubLedger.gson, "schemaName", value.schemaName)
        writeNamedField(DataHubLedger.gson, "maxAgeMs", value.maxAgeMs)
        writeNamedArray(DataHubLedger.gson, "retentions", value.retentions) {
          it.medium.name + it.encryptionRequired.toString()
        }
        writeNamedArray(DataHubLedger.gson, "fields", value.fields) { it.fieldPath.joinToString() }
        writeNamedArray(DataHubLedger.gson, "annotations", value.annotations) { it.name }
      }
    }

    override fun read(reader: JsonReader): PolicyTarget? {
      val type = reader.peek()
      if (type == JsonToken.NULL) return null

      var schemaName = ""
      var maxAgeMs = 0L
      var retentions = emptyList<PolicyRetention>()
      var fields = emptyList<PolicyField>()
      var annotations = emptyList<Annotation>()
      reader.readObject { fieldName ->
        when (fieldName) {
          "schemaName" -> schemaName = reader.nextString()
          "maxAgeMs" -> maxAgeMs = reader.nextLong()
          "retentions" -> retentions = reader.readList(DataHubLedger.gson)
          "fields" -> fields = reader.readList(DataHubLedger.gson)
          "annotations" -> annotations = reader.readList(DataHubLedger.gson)
        }
      }
      return PolicyTarget(schemaName, maxAgeMs, retentions, fields, annotations)
    }
  }

  object PolicyFieldTypeAdapter : TypeAdapter<PolicyField>() {
    override fun write(out: JsonWriter, value: PolicyField?) {
      if (value == null) {
        out.nullValue()
        return
      }
      out.makeObject {
        writeNamedStringArray(DataHubLedger.gson, "fieldPath", value.fieldPath)
        if (value.rawUsages.isNotEmpty()) {
          writeNamedField(UsageTypeSetTypeAdapter, "rawUsages", value.rawUsages)
        }
        writeNamedMap(UsageTypeSetTypeAdapter, "redactedUsages", value.redactedUsages)
        writeNamedArray(DataHubLedger.gson, "subfields", value.subfields) {
          it.fieldPath.joinToString()
        }
        writeNamedArray(DataHubLedger.gson, "annotations", value.annotations) { it.name }
      }
    }

    override fun read(reader: JsonReader): PolicyField? {
      val type = reader.peek()
      if (type == JsonToken.NULL) return null

      var fieldPath = emptyList<FieldName>()
      var rawUsages = emptySet<UsageType>()
      var redactedUsages = emptyMap<String, Set<UsageType>>()
      var subfields = emptyList<PolicyField>()
      var annotations = emptyList<Annotation>()
      reader.readObject { fieldName ->
        when (fieldName) {
          "fieldPath" -> fieldPath = reader.readStringList()
          "rawUsages" -> rawUsages = UsageTypeSetTypeAdapter.read(reader) ?: emptySet()
          "redactedUsages" -> redactedUsages = reader.readMap(UsageTypeSetTypeAdapter)
          "subfields" -> subfields = reader.readList(DataHubLedger.gson)
          "annotations" -> annotations = reader.readList(DataHubLedger.gson)
        }
      }
      return PolicyField(fieldPath, rawUsages, redactedUsages, subfields, annotations)
    }
  }

  object UsageTypeSetTypeAdapter : TypeAdapter<Set<UsageType>>() {
    override fun write(out: JsonWriter, value: Set<UsageType>?) {
      if (value == null) {
        out.nullValue()
        return
      }
      val stringWriter = StringWriter()
      val jsonWriter = JsonWriter(stringWriter)
      jsonWriter.writeStringArray(DataHubLedger.gson, value.map { it.toString() }, sorted = true)
      jsonWriter.flush()
      out.jsonValue(stringWriter.buffer.toString())
    }

    override fun read(reader: JsonReader): Set<UsageType>? {
      val type = reader.peek()
      if (type == JsonToken.NULL) return null
      return reader.readSet(DataHubLedger.gson)
    }
  }

  object AnnotationParamTypeAdapter : TypeAdapter<AnnotationParam>() {
    override fun write(out: JsonWriter, value: AnnotationParam?) {
      when (value) {
        is AnnotationParam.Bool -> out.value(value.value)
        is AnnotationParam.Num -> out.value(value.value)
        is AnnotationParam.Str -> out.value(value.value)
        null -> out.nullValue()
      }
    }

    override fun read(reader: JsonReader): AnnotationParam? {
      return when (reader.peek()) {
        JsonToken.STRING -> AnnotationParam.Str(reader.nextString())
        JsonToken.NUMBER -> AnnotationParam.Num(reader.nextInt())
        JsonToken.BOOLEAN -> AnnotationParam.Bool(reader.nextBoolean())
        JsonToken.NULL -> null
        JsonToken.BEGIN_ARRAY,
        JsonToken.END_ARRAY,
        JsonToken.BEGIN_OBJECT,
        JsonToken.END_OBJECT,
        JsonToken.END_DOCUMENT,
        JsonToken.NAME,
        null ->
          throw IllegalArgumentException(
            "Invalid AnnotationParam, expected string, number, boolean, or null"
          )
      }
    }
  }
}
