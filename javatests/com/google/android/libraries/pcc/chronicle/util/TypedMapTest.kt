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

package com.google.android.libraries.pcc.chronicle.util

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypedMapTest {
  private val name = "Name"
  private val weather = "Sunny"
  private val temp = 73

  object NameKey : Key<String>
  object WeatherKey : Key<String>
  object TemperatureKey : Key<Int>

  lateinit var mutableTypedMap: MutableTypedMap

  @Before
  fun setup() {
    mutableTypedMap = MutableTypedMap()
  }

  @Test
  fun mutableTypedMap_nullValue() {
    assertThat(mutableTypedMap[NameKey]).isNull()
  }

  @Test
  fun mutableTypedMap_set() {
    mutableTypedMap[NameKey] = name
    mutableTypedMap[WeatherKey] = weather
    mutableTypedMap[TemperatureKey] = temp

    assertThat(mutableTypedMap[NameKey]).isEqualTo(name)
    assertThat(mutableTypedMap[WeatherKey]).isEqualTo(weather)
    assertThat(mutableTypedMap[TemperatureKey]).isEqualTo(temp)
  }

  @Test
  fun mutableTypedMap_toTypedMap() {
    mutableTypedMap[NameKey] = name
    val typedMap = mutableTypedMap.toTypedMap()

    assertThat(typedMap).isEqualTo(TypedMap(mutableTypedMap))
    assertThat(typedMap[NameKey]).isEqualTo(name)
  }

  @Test
  fun mutableTypedMap_equals() {
    mutableTypedMap[NameKey] = name
    val otherMap = MutableTypedMap()

    assertThat(mutableTypedMap).isEqualTo(mutableTypedMap) // this === other
    assertThat(mutableTypedMap).isNotEqualTo(name as Any) // other !is TypedMap
    assertThat(mutableTypedMap).isNotEqualTo(otherMap) // this.map != other.map

    otherMap[NameKey] = name
    assertThat(mutableTypedMap).isEqualTo(otherMap) // this.map == other.map
  }

  @Test
  fun mutableTypedMap_hashCode() {
    assertThat(mutableTypedMap.hashCode()).isEqualTo(mutableMapOf<Key<Any>, Any>().hashCode())
  }

  @Test
  fun typedMap_copiesMutableTypedMapArgument() {
    mutableTypedMap[NameKey] = name
    val typedMap = mutableTypedMap.toTypedMap()
    mutableTypedMap[NameKey] = "other"

    // Should still equal `name`
    assertThat(typedMap[NameKey]).isEqualTo(name)
  }

  @Test
  fun typedMap_get() {
    mutableTypedMap[NameKey] = name
    mutableTypedMap[WeatherKey] = weather
    mutableTypedMap[TemperatureKey] = temp
    val typedMap = mutableTypedMap.toTypedMap()

    assertThat(typedMap[NameKey]).isEqualTo(name)
    assertThat(typedMap[WeatherKey]).isEqualTo(weather)
    assertThat(typedMap[TemperatureKey]).isEqualTo(temp)
  }

  @Test
  fun typedMap_equals() {
    mutableTypedMap[NameKey] = name
    val typedMap = mutableTypedMap.toTypedMap()

    assertThat(typedMap).isEqualTo(typedMap) // this === other
    assertThat(typedMap).isNotEqualTo(name as Any) // other !is TypedMap

    val otherMutableMap = MutableTypedMap()
    otherMutableMap[NameKey] = "Other name"
    val otherMap = TypedMap(otherMutableMap)
    assertThat(typedMap).isNotEqualTo(otherMap) // this.map != other.map

    otherMutableMap[NameKey] = name
    assertThat(typedMap).isEqualTo(TypedMap(otherMutableMap)) // this.map == other.map
  }

  @Test
  fun typedMap_hashCode() {
    mutableTypedMap[NameKey] = name
    val typedMap = mutableTypedMap.toTypedMap()

    assertThat(typedMap.hashCode()).isEqualTo(mutableTypedMap.hashCode())
  }
}
