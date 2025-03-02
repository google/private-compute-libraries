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

package com.google.android.libraries.pcc.chronicle.codegen

/** The expected type outputs for the various test protos and classes in [SourceClasses]. */
object ExpectedTypes {
  const val packageName = "com.google.android.libraries.pcc.chronicle.codegen"

  private val LIST_TYPE_LOCATION = TypeLocation("List", pkg = "java.util")
  private val TIMESTAMP_PROTO_TYPE_LOCATION = TypeLocation("Timestamp", pkg = "com.google.protobuf")
  private val IMMUTABLELIST_TYPE_LOCATION =
    TypeLocation("ImmutableList", pkg = "com.google.common.collect")
  private val SET_TYPE_LOCATION = TypeLocation("Set", pkg = "java.util")
  private val MAP_TYPE_LOCATION = TypeLocation("Map", pkg = "java.util")

  private val thingEnum =
    FieldCategory.EnumValue(
      typeLocation("ThingEnum"),
      SourceClasses.ThingEnum.values().map { v -> v.toString() },
    )

  /**
   * Helper to allow most of these test objects to be re-used.
   *
   * Converts the enclosing class of the type and all of its enclosed types to use the enclosing
   * class name `CodegenTestProtos`.
   */
  fun Type.forProtos(
    overridePkg: String? = null,
    convertBytesToStrings: Boolean = true,
    timestampOverridePkg: String? = null,
  ): Type =
    copy(
      location = location.forProtos(overridePkg, timestampOverridePkg),
      jvmLocation = jvmLocation.forProtos(),
      fields =
        fields.map { it.forProtos(overridePkg, convertBytesToStrings, timestampOverridePkg) },
      tooling = Type.Tooling.PROTO,
    )

  val thing =
    Type(
      location = typeLocation("Thing"),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.ByteValue),
          FieldEntry("field3", FieldCategory.ShortValue),
          FieldEntry("field4", FieldCategory.CharValue),
          FieldEntry("field5", FieldCategory.IntValue),
          FieldEntry("field6", FieldCategory.LongValue),
          FieldEntry("field7", FieldCategory.FloatValue),
          FieldEntry("field8", FieldCategory.DoubleValue),
          FieldEntry("field9", FieldCategory.BooleanValue),
          FieldEntry(
            "field10",
            FieldCategory.ListValue(LIST_TYPE_LOCATION, FieldCategory.StringValue),
          ),
          FieldEntry("field11", FieldCategory.SetValue(SET_TYPE_LOCATION, FieldCategory.IntValue)),
          FieldEntry(
            "field12",
            FieldCategory.MapValue(
              MAP_TYPE_LOCATION,
              FieldCategory.StringValue,
              FieldCategory.BooleanValue,
            ),
          ),
          FieldEntry("field13", thingEnum),
          FieldEntry("field14", FieldCategory.InstantValue),
          FieldEntry("field15", FieldCategory.DurationValue),
          FieldEntry(
            "field16",
            FieldCategory.MapValue(
              MAP_TYPE_LOCATION,
              FieldCategory.StringValue,
              FieldCategory.BooleanValue,
            ),
          ),
          FieldEntry("field17", FieldCategory.ByteArrayValue),
        ),
    )

  val protoThing =
    Type(
      location = typeLocation("Thing"),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.ByteArrayValue),
          FieldEntry("field3", FieldCategory.IntValue),
          FieldEntry("field4", FieldCategory.LongValue),
          FieldEntry("field5", FieldCategory.FloatValue),
          FieldEntry("field6", FieldCategory.DoubleValue),
          FieldEntry("field7", FieldCategory.BooleanValue),
          FieldEntry("field8", thingEnum),
          FieldEntry(
            "field9",
            FieldCategory.ListValue(LIST_TYPE_LOCATION, FieldCategory.StringValue),
          ),
          FieldEntry(
            "field10",
            FieldCategory.MapValue(
              MAP_TYPE_LOCATION,
              FieldCategory.StringValue,
              FieldCategory.BooleanValue,
            ),
            sourceName = "field10Map",
          ),
          FieldEntry("field11", FieldCategory.NestedTypeValue(TIMESTAMP_PROTO_TYPE_LOCATION)),
        ),
    )

  val protoTimestamp =
    Type(
      location = TypeLocation("Timestamp", pkg = "com.google.protobuf"),
      fields =
        listOf(
          FieldEntry("seconds", FieldCategory.LongValue),
          FieldEntry("nanos", FieldCategory.IntValue),
        ),
      tooling = Type.Tooling.PROTO,
    )

  val simpleUnenclosedType =
    Type(
      location = TypeLocation(name = "SimpleUnenclosedType", pkg = packageName),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.IntValue),
        ),
    )

  val stringThing =
    Type(
      location = typeLocation("StringThing", "OneOfThing"),
      fields = listOf(FieldEntry("field1", FieldCategory.StringValue)),
    )

  val intThing =
    Type(
      location = typeLocation("IntThing", "OneOfThing"),
      fields = listOf(FieldEntry("field1", FieldCategory.IntValue)),
    )

  val otherThing =
    Type(
      location = typeLocation("OtherThing", "OneOfThing"),
      fields =
        listOf(FieldEntry("field1", thingEnum), FieldEntry("field2", FieldCategory.StringValue)),
    )

  fun oneOfEntry(name: String, location: TypeLocation, enableNullable: Boolean) =
    FieldEntry(
      name,
      if (enableNullable) {
        FieldCategory.NullableValue(FieldCategory.NestedTypeValue(location))
      } else {
        FieldCategory.NestedTypeValue(location)
      },
      presenceCondition = "has${name.capitalize()}",
    )

  fun oneOfThing(enableNullable: Boolean = false) =
    Type(
      location = typeLocation("OneOfThing"),
      fields =
        listOf(
          oneOfEntry("stringThing", stringThing.location, enableNullable),
          oneOfEntry("intThing", intThing.location, enableNullable),
          oneOfEntry("otherThing", otherThing.location, enableNullable),
          oneOfEntry("stringThing2", stringThing.location, enableNullable),
          oneOfEntry("intThing2", intThing.location, enableNullable),
          oneOfEntry("otherThing2", otherThing.location, enableNullable),
        ),
      oneOfs =
        OneOfs(
          oneOfForField =
            mapOf(
              "stringThing" to "field1",
              "intThing" to "field1",
              "otherThing" to "field1",
              "stringThing2" to "field2",
              "intThing2" to "field2",
              "otherThing2" to "field2",
            ),
          fieldsForOneOf =
            mapOf(
              "field1" to listOf("stringThing", "intThing", "otherThing"),
              "field2" to listOf("stringThing2", "intThing2", "otherThing2"),
            ),
        ),
    )

  val simpleThing =
    Type(
      location = typeLocation("SimpleThing"),
      fields = listOf(FieldEntry("field1", FieldCategory.StringValue)),
    )

  val nestedThing =
    Type(
      location = typeLocation("NestedThing"),
      fields =
        listOf(
          FieldEntry(
            "field1",
            FieldCategory.ListValue(LIST_TYPE_LOCATION, FieldCategory.StringValue),
          ),
          FieldEntry("field2", FieldCategory.NestedTypeValue(simpleThing.location)),
        ),
    )

  val innerInnerType =
    Type(
      location = typeLocation("InnerInnerType", "InnerType", "NestedType"),
      fields = listOf(FieldEntry("field1", FieldCategory.StringValue)),
    )

  val innerType =
    Type(
      location = typeLocation("InnerType", "NestedType"),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.NestedTypeValue(innerInnerType.location)),
        ),
    )

  val nestedType =
    Type(
      location = typeLocation("NestedType"),
      fields = listOf(FieldEntry("field1", FieldCategory.NestedTypeValue(innerType.location))),
    )

  val thing3 =
    Type(
      location = typeLocation("Thing3"),
      fields = listOf(FieldEntry("field1", FieldCategory.StringValue)),
    )

  val thing2 =
    Type(
      location = typeLocation("Thing2"),
      fields = listOf(FieldEntry("field1", FieldCategory.NestedTypeValue(thing3.location))),
    )

  val thing1 =
    Type(
      location = typeLocation("Thing1"),
      fields = listOf(FieldEntry("field1", FieldCategory.NestedTypeValue(thing2.location))),
    )

  val listOfEntity =
    Type(
      location = typeLocation("ListOfEntity"),
      fields =
        listOf(
          FieldEntry(
            "listOfEntity",
            FieldCategory.ListValue(
              LIST_TYPE_LOCATION,
              FieldCategory.NestedTypeValue(simpleThing.location),
            ),
          )
        ),
    )

  val listOfListOfEntity =
    Type(
      location = typeLocation("ListOfListOfEntity"),
      fields =
        listOf(
          FieldEntry(
            "listOfListOfEntity",
            FieldCategory.ListValue(
              LIST_TYPE_LOCATION,
              FieldCategory.ListValue(
                LIST_TYPE_LOCATION,
                FieldCategory.NestedTypeValue(simpleThing.location),
              ),
            ),
          )
        ),
    )

  val mapOfEntity =
    Type(
      location = typeLocation("MapOfEntity"),
      fields =
        listOf(
          FieldEntry(
            "mapOfEntity",
            FieldCategory.MapValue(
              location = MAP_TYPE_LOCATION,
              keyType = FieldCategory.StringValue,
              valueType = FieldCategory.NestedTypeValue(simpleThing.location),
            ),
          )
        ),
    )

  val mapOfListOfEntity =
    Type(
      location = typeLocation("MapOfListOfEntity"),
      fields =
        listOf(
          FieldEntry(
            "mapOfListOfEntity",
            FieldCategory.MapValue(
              location = MAP_TYPE_LOCATION,
              keyType = FieldCategory.StringValue,
              valueType =
                FieldCategory.ListValue(
                  LIST_TYPE_LOCATION,
                  FieldCategory.NestedTypeValue(simpleThing.location),
                ),
            ),
          )
        ),
    )

  val mapWithEnumKey =
    Type(
      location = typeLocation("MapWithEnumKey"),
      fields =
        listOf(
          FieldEntry(
            "mapWithEnumKey",
            FieldCategory.MapValue(
              location = MAP_TYPE_LOCATION,
              keyType = thingEnum,
              valueType = FieldCategory.StringValue,
            ),
          )
        ),
    )

  private val recursiveRefBLocation = typeLocation("RecursiveRefB")
  val recursiveRefA =
    Type(
      typeLocation("RecursiveRefA"),
      fields = listOf(FieldEntry("other", FieldCategory.NestedTypeValue(recursiveRefBLocation))),
    )

  val recursiveRefB =
    Type(
      location = recursiveRefBLocation,
      fields = listOf(FieldEntry("other", FieldCategory.NestedTypeValue(recursiveRefA.location))),
    )

  private val recursiveListRefBLocation = typeLocation("RecursiveListRefB")
  val recursiveListRefA =
    Type(
      location = typeLocation("RecursiveListRefA"),
      fields =
        listOf(
          FieldEntry(
            "others",
            FieldCategory.ListValue(
              LIST_TYPE_LOCATION,
              FieldCategory.NestedTypeValue(recursiveListRefBLocation),
            ),
          )
        ),
    )

  val recursiveListRefB =
    Type(
      location = recursiveListRefBLocation,
      fields =
        listOf(
          FieldEntry(
            "others",
            FieldCategory.ListValue(
              LIST_TYPE_LOCATION,
              FieldCategory.NestedTypeValue(recursiveListRefA.location),
            ),
          )
        ),
    )

  private val recursiveMapRefBLocation = typeLocation("RecursiveMapRefB")
  val recursiveMapRefA =
    Type(
      location = typeLocation("RecursiveMapRefA"),
      fields =
        listOf(
          FieldEntry(
            "others",
            FieldCategory.MapValue(
              MAP_TYPE_LOCATION,
              FieldCategory.StringValue,
              FieldCategory.NestedTypeValue(recursiveMapRefBLocation),
            ),
          )
        ),
    )

  val recursiveMapRefB =
    Type(
      location = recursiveMapRefBLocation,
      fields =
        listOf(
          FieldEntry(
            "others",
            FieldCategory.MapValue(
              MAP_TYPE_LOCATION,
              FieldCategory.StringValue,
              FieldCategory.NestedTypeValue(recursiveMapRefA.location),
            ),
          )
        ),
    )

  val repeatedGroup =
    Type(
      location = typeLocation("RepeatedGroup"),
      fields =
        listOf(
          FieldEntry(
            "inner",
            FieldCategory.ListValue(
              LIST_TYPE_LOCATION,
              FieldCategory.NestedTypeValue(typeLocation("Inner", "RepeatedGroup")),
            ),
          )
        ),
    )

  val repeatedGroupInner =
    Type(
      location = typeLocation("Inner", "RepeatedGroup"),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.NestedTypeValue(nestedThing.location)),
        ),
    )

  val protoContainer =
    Type(
      location = TypeLocation(name = "ProtoContainer", pkg = packageName),
      fields =
        listOf(
          FieldEntry("name", FieldCategory.StringValue),
          FieldEntry("proto", FieldCategory.NestedTypeValue(thing.forProtos().location)),
        ),
    )

  val nestedProtoContainer =
    Type(
      location = TypeLocation(name = "NestedProtoContainer", pkg = packageName),
      fields =
        listOf(
          FieldEntry("wrapperName", FieldCategory.StringValue),
          FieldEntry("container", FieldCategory.NestedTypeValue(protoContainer.location)),
        ),
    )

  val simpleAutoValue =
    Type(
      location = TypeLocation(name = "SimpleAutoValue", pkg = packageName),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.IntValue),
          FieldEntry(
            "field3",
            FieldCategory.ListValue(
              location = IMMUTABLELIST_TYPE_LOCATION,
              listType = FieldCategory.StringValue,
            ),
          ),
          FieldEntry("field4", FieldCategory.StringValue),
        ),
    )

  val nestedAutoValue =
    Type(
      location = TypeLocation(name = "NestedAutoValue", pkg = packageName),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.IntValue),
          FieldEntry(
            "field3",
            FieldCategory.ListValue(
              location = IMMUTABLELIST_TYPE_LOCATION,
              listType = FieldCategory.StringValue,
            ),
          ),
          FieldEntry("field4", FieldCategory.StringValue),
          FieldEntry("field5", FieldCategory.NestedTypeValue(location = simpleAutoValue.location)),
          FieldEntry("field6", FieldCategory.NestedTypeValue(location = simpleThing.location)),
        ),
    )

  val notAutoValue =
    Type(
      location = TypeLocation(name = "NotAutoValue", pkg = packageName),
      fields = listOf(FieldEntry("field1", FieldCategory.StringValue)),
    )

  val autoValueInDataClass =
    Type(
      location = TypeLocation(name = "AutoValueInDataClass", pkg = packageName),
      fields =
        listOf(
          FieldEntry("field1", FieldCategory.StringValue),
          FieldEntry("field2", FieldCategory.NestedTypeValue(location = simpleAutoValue.location)),
          FieldEntry("field3", FieldCategory.NestedTypeValue(location = nestedAutoValue.location)),
          FieldEntry("field4", FieldCategory.NestedTypeValue(location = notAutoValue.location)),
        ),
    )

  val subType =
    Type(
      location = typeLocation("SubType"),
      fields =
        listOf(
          FieldEntry("name", FieldCategory.StringValue),
          FieldEntry("baseValue", FieldCategory.StringValue),
          FieldEntry("isBaseFlag", FieldCategory.BooleanValue),
          FieldEntry("subField", FieldCategory.StringValue),
          FieldEntry("isFlag", FieldCategory.BooleanValue),
        ),
    )

  /** Helper used to generate the [TypeLocation] used by most of these classes. */
  private fun typeLocation(name: String, vararg additionalNames: String) =
    TypeLocation(
      name = name,
      enclosingNames = additionalNames.asList() + "SourceClasses",
      pkg = packageName,
    )

  private fun TypeLocation.withEnclosingName(name: String) =
    copy(
      enclosingNames =
        enclosingNames.dropLast(1) + if (name.isNotEmpty()) listOf(name) else emptyList()
    )

  private fun TypeLocation.withPackage(pkg: String) = copy(pkg = pkg)

  private fun TypeLocation.forProtos(
    overridePkg: String? = null,
    timestampOverridePkg: String? = null,
  ) =
    withEnclosingName(
        when (name) {
          "SimpleThing" -> "CodegenTestSubProto"
          "Timestamp" -> ""
          else -> "CodegenTestProto"
        }
      )
      .let {
        if (overridePkg != null && name != "Timestamp") {
          it.withPackage(overridePkg)
        } else if (timestampOverridePkg != null && name == "Timestamp") {
          it.withPackage(timestampOverridePkg)
        } else {
          it
        }
      }

  private fun FieldEntry.protoName(convertBytesToStrings: Boolean = true) =
    when {
      category is FieldCategory.ListValue -> "${name}List"
      category is FieldCategory.MapValue -> "${name}Map"
      convertBytesToStrings && category is FieldCategory.ByteArrayValue -> "$name.toStringUtf8()"
      else -> sourceName
    }

  private fun FieldEntry.forProtos(
    overridePkg: String? = null,
    convertBytesToStrings: Boolean = true,
    timestampOverridePkg: String? = null,
  ): FieldEntry {
    return copy(
      name = name,
      category = category.forProtos(overridePkg, convertBytesToStrings, timestampOverridePkg),
      sourceName = protoName(convertBytesToStrings),
    )
  }

  private fun FieldCategory.forProtos(
    overridePkg: String? = null,
    convertBytesToStrings: Boolean = true,
    timestampOverridePkg: String? = null,
  ): FieldCategory {
    return when (this) {
      is FieldCategory.ByteArrayValue ->
        if (convertBytesToStrings) FieldCategory.StringValue else this
      is FieldCategory.NestedTypeValue ->
        copy(
          location = location.forProtos(overridePkg, timestampOverridePkg),
          jvmLocation = jvmLocation.forProtos(),
        )
      is FieldCategory.NullableValue ->
        copy(
          innerType = innerType.forProtos(overridePkg, timestampOverridePkg = timestampOverridePkg)
        )
      is FieldCategory.EnumValue ->
        copy(location = location.forProtos(overridePkg), jvmLocation = jvmLocation.forProtos())
      is FieldCategory.ListValue ->
        copy(
          listType = listType.forProtos(overridePkg, timestampOverridePkg = timestampOverridePkg)
        )
      is FieldCategory.MapValue ->
        copy(
          keyType = keyType.forProtos(),
          valueType = valueType.forProtos(overridePkg, timestampOverridePkg = timestampOverridePkg),
        )
      else -> this
    }
  }
}
