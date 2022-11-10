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

/*
 * Copyright 2021 Google LLC.
 *
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 *
 * Code distributed by Google as part of this project is also subject to an additional IP rights
 * grant found at
 * http://polymer.github.io/PATENTS.txt
 */
package com.google.android.libraries.pcc.chronicle.api.policy.builder

import com.google.android.libraries.pcc.chronicle.api.DataTypeDescriptor
import com.google.android.libraries.pcc.chronicle.api.FieldType
import com.google.android.libraries.pcc.chronicle.api.Trigger
import com.google.android.libraries.pcc.chronicle.api.policy.FieldName
import com.google.android.libraries.pcc.chronicle.api.policy.Policy
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyConfig
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyField
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyRetention
import com.google.android.libraries.pcc.chronicle.api.policy.PolicyTarget
import com.google.android.libraries.pcc.chronicle.api.policy.StorageMedium
import com.google.android.libraries.pcc.chronicle.api.policy.UsageType
import com.google.android.libraries.pcc.chronicle.api.policy.annotation.Annotation
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.All
import com.google.android.libraries.pcc.chronicle.api.policy.contextrules.PolicyContextRule
import java.time.Duration

typealias UsageType = UsageType

typealias StorageMedium = StorageMedium

/**
 * Builds a [Policy] with the supplied [name] and [egressType], using a [PolicyBuilder].
 *
 * Example:
 *
 * ```kotlin
 * val myPolicy = policy("MyPolicy", "Analytics") {
 *   description =
 *     """
 *     Policy describing valid usage of Foos and Bars when publishing statistics to cloud-based
 *     analytics.
 *     """.trimIndent()
 *
 *   allowedContexts = AllowAllContextsRule
 *
 *   config("AnalyticsServer") {
 *     "url" to "https://mypolicyanalytics.com/stats"
 *   }
 *
 *   target(fooDTD) {
 *     maxAgeMillis = Duration.ofDays(5)
 *
 *     retention(StorageMedium.DISK, encryptionRequired = true)
 *     retention(StorageMedium.RAM)
 *
 *     "age" to { rawUsage(UsageType.ANY) }
 *     "parents" to {
 *       "mother.firstName" to { rawUsage(UsageType.ANY) }
 *       "father.firstName" to { rawUsage(UsageType.ANY) }
 *     }
 *     "address" to {
 *       "latitude" to {
 *         rawUsage(UsageType.JOIN)
 *         conditionalUsage("citylevelAccuracy", UsageType.EGRESS)
 *       }
 *       "longitude" to {
 *         rawUsage(UsageType.JOIN)
 *         conditionalUsage("citylevelAccuracy", UsageType.EGRESS)
 *       }
 *     }
 *   }
 *
 *   target(barDTD) {
 *     maxAgeMillis = Duration.ofHours(6)
 *
 *     retention(StorageMedium.RAM)
 *
 *     "bestFriend.name" to { conditionalUsage("mangled", UsageType.ANY) }
 *   }
 * }
 * ```
 */
fun policy(name: String, egressType: String, block: PolicyBuilder.() -> Unit = {}): Policy =
  PolicyBuilder(name, egressType).apply(block).build()

/**
 * Builds a [PolicyTarget] with the supplied [dataTypeDescriptor], using a [PolicyTargetBuilder].
 *
 * Providing `0` as [maxAgeMillis] implies that the target may not be held for any length of time.
 *
 * See [policy] for an example.
 */
fun target(
  dataTypeDescriptor: DataTypeDescriptor,
  maxAge: Duration,
  block: PolicyTargetBuilder.() -> Unit = {},
): PolicyTarget =
  PolicyTargetBuilder(dataTypeDescriptor, requireNotNull(dataTypeDescriptor.name), maxAge)
    .apply(block)
    .build()

/** Builder of [Policy] instances. */
@DataDsl
class PolicyBuilder(
  private val name: String,
  private val egressType: String,
) {
  /** Human-readable description of the policy. */
  var description: String = ""

  /**
   * `allowedContext` can be used to define when a policy should be applied. PolicyContextRules can
   * be expressed and combined with boolean logic, using `and`/`or`/`not` operators.
   */
  var allowedContext: PolicyContextRule = All

  // internal for tests only
  internal val targets = mutableListOf<PolicyTarget>()
  internal val configs = mutableMapOf<String, PolicyConfig>()

  /** Clones the PolicyBuilder. */
  constructor(
    policyBuilder: PolicyBuilder,
  ) : this(policyBuilder.name, policyBuilder.egressType) {
    this.apply {
      description = policyBuilder.description
      allowedContext = policyBuilder.allowedContext
      // The values of these collections are immutable, so duplication the collections themselves is
      // sufficient.
      targets.addAll(policyBuilder.targets)
      configs.putAll(policyBuilder.configs)
    }
  }

  fun target(
    dataTypeDescriptor: DataTypeDescriptor,
    maxAge: Duration,
    block: PolicyTargetBuilder.() -> Unit,
  ): PolicyTarget {
    return PolicyTargetBuilder(dataTypeDescriptor, requireNotNull(dataTypeDescriptor.name), maxAge)
      .apply(block)
      .build()
      .also(targets::add)
  }

  /** Adds a [PolicyConfig] block to the [Policy] being built. */
  fun config(configName: String, block: PolicyConfigBuilder.() -> Unit): PolicyConfig =
    PolicyConfigBuilder().apply(block).build().also { configs[configName] = it }

  /** Builds the [Policy]. */
  fun build(): Policy {
    return Policy(
      name = name,
      egressType = egressType,
      description = description,
      targets = targets,
      configs = configs,
      allowedContext = allowedContext
    )
  }
}

/** Builder of [PolicyTarget] instances. */
@DataDsl
class PolicyTargetBuilder
internal constructor(
  private val dataTypeDescriptor: DataTypeDescriptor,
  private val schemaName: String,
  /** The maximum allowable age of the entities being targeted. */
  var maxAge: Duration,
) {
  private val retentions = mutableSetOf<PolicyRetention>()
  private val fields = mutableSetOf<PolicyField>()
  private val annotations = mutableSetOf<Annotation>()

  /**
   * Adds a deletion requirement to a particular [field]. Use dots to delimit nested fields. For
   * example: `target(FOO) { deletionTrigger(Trigger.PackageUninstalled, "packageName") }`
   */
  fun deletionTrigger(trigger: Trigger, field: String): PolicyTargetBuilder {
    annotations.add(trigger.toAnnotation(field))
    return this
  }

  /** Adds a new [PolicyRetention] object to the [PolicyTarget] being built. */
  fun retention(medium: StorageMedium, encryptionRequired: Boolean = false): PolicyRetention =
    PolicyRetention(medium, encryptionRequired).also(retentions::add)

  /**
   * Adds a [PolicyField] to the [PolicyTarget] being built, with the receiving string as the
   * dot-delimited access path of the field.
   *
   * Example:
   *
   * ```kotlin
   * target(personDTD)) {
   *   "name" { rawUsage(UsageType.ANY) }
   *   "bestFriend.name" { rawUsage(UsageType.JOIN) }
   * }
   * ```
   */
  operator fun String.invoke(block: PolicyFieldBuilder.() -> Unit): PolicyField {
    val entityDataTypeDescriptor =
      PolicyFieldBuilder.validateAndGetDataTypeDescriptor(this, dataTypeDescriptor)
    return PolicyFieldBuilder(entityDataTypeDescriptor, listOf(this))
      .apply(block)
      .build()
      .also(fields::add)
  }

  /** Builds the [PolicyTarget]. */
  fun build(): PolicyTarget =
    PolicyTarget(
      schemaName = schemaName,
      maxAgeMs = maxAge.toMillis(),
      retentions = retentions.toList(),
      fields = fields.toList(),
      annotations = annotations.toList()
    )
}

/** Builder of [PolicyField] instances. */
@DataDsl
class PolicyFieldBuilder(
  private val dataTypeDescriptor: DataTypeDescriptor?,
  private val fieldPath: List<FieldName>
) {
  private val rawUsages = mutableSetOf<UsageType>()
  private val redactedUsages = mutableMapOf<String, Set<UsageType>>()
  private val subFields = mutableSetOf<PolicyField>()
  private val annotations = mutableSetOf<Annotation>()

  /** Adds [UsageType]s as raw-usage affordances. */
  fun rawUsage(vararg usageTypes: UsageType): PolicyFieldBuilder = apply {
    rawUsages.addAll(usageTypes)
  }

  /**
   * Adds [UsageType]s as affordances, if and only if the [requiredLabel] is present on the field.
   */
  fun conditionalUsage(requiredLabel: String, vararg usageTypes: UsageType): PolicyFieldBuilder =
    apply {
      redactedUsages[requiredLabel] = (redactedUsages[requiredLabel] ?: emptySet()) + usageTypes
    }

  fun ConditionalUsage.whenever(vararg usageTypes: UsageType): PolicyFieldBuilder {
    conditionalUsage(this.serializedName, *usageTypes)
    return this@PolicyFieldBuilder
  }

  /** Adds a new sub-field to the [PolicyField] being built. */
  operator fun String.invoke(block: PolicyFieldBuilder.() -> Unit): PolicyField {
    val fieldDataTypeDescriptor =
      requireNotNull(dataTypeDescriptor) { "Trying to lookup field '$this' in a non-entity type." }
    val entityDataTypeDescriptor = validateAndGetDataTypeDescriptor(this, fieldDataTypeDescriptor)
    return PolicyFieldBuilder(entityDataTypeDescriptor, fieldPath + listOf(this))
      .apply(block)
      .build()
      .also(subFields::add)
  }

  /** Builds the [PolicyField]. */
  fun build(): PolicyField =
    PolicyField(
      fieldPath = fieldPath,
      rawUsages = rawUsages,
      redactedUsages = redactedUsages,
      subfields = subFields.toList(),
      annotations = annotations.toList()
    )

  companion object {
    /**
     * Verifies that [fieldName] is a valid field in [dataTypeDescriptor] and returns the
     * [DataTypeDescriptor] if [fieldName] is a field with another nested schema.
     */
    fun validateAndGetDataTypeDescriptor(
      fieldName: String,
      dataTypeDescriptor: DataTypeDescriptor
    ): DataTypeDescriptor? {
      // Only validate field if [schema] is not null.
      val fieldType =
        requireNotNull(dataTypeDescriptor.fields.get(fieldName)) {
          "Field '$fieldName' not found in '${dataTypeDescriptor.name}'."
        }
      return fieldType.getDataTypeDescriptor(dataTypeDescriptor)
    }

    /** Returns the underlying [DataTypeDescriptor] if available. */
    private fun FieldType.getDataTypeDescriptor(
      parentDataTypeDescriptor: DataTypeDescriptor
    ): DataTypeDescriptor? {
      return when (this) {
        is FieldType.Array -> itemFieldType.getDataTypeDescriptor(parentDataTypeDescriptor)
        is FieldType.List -> itemFieldType.getDataTypeDescriptor(parentDataTypeDescriptor)
        is FieldType.Nested -> parentDataTypeDescriptor.innerTypes.find { it.name == this.name }
        is FieldType.Nullable -> itemFieldType.getDataTypeDescriptor(parentDataTypeDescriptor)
        FieldType.Boolean,
        FieldType.Byte,
        FieldType.ByteArray,
        FieldType.Char,
        FieldType.Double,
        FieldType.Duration,
        FieldType.Float,
        FieldType.Instant,
        FieldType.Integer,
        FieldType.Long,
        FieldType.Short,
        FieldType.String,
        is FieldType.Enum,
        is FieldType.Opaque,
        is FieldType.Reference,
        is FieldType.Tuple -> null
      }
    }
  }
}

/** Builder of [PolicyConfig] maps. */
@DataDsl
class PolicyConfigBuilder {
  private val backingMap = mutableMapOf<String, String>()

  /** Adds a key-value pair to the [PolicyConfig] being built. */
  infix fun String.to(value: String) {
    backingMap[this] = value
  }

  /** Builds the [PolicyConfig]. */
  fun build(): PolicyConfig = backingMap
}
