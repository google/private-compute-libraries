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

package com.google.android.libraries.pcc.chronicle.api.policy.contextrules

import com.google.android.libraries.pcc.chronicle.util.Key
import com.google.android.libraries.pcc.chronicle.util.MutableTypedMap
import com.google.android.libraries.pcc.chronicle.util.TypedMap
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PolicyContextRuleTest {
  /** Name key for TypedMap that correlates to a String value */
  object NameKey : Key<String>

  /** Rule that checks if Name is "Test", using the given context */
  object TestName : PolicyContextRule {
    override val name = "TestName"
    override val operands: List<PolicyContextRule> = emptyList()

    override fun invoke(context: TypedMap): Boolean = context[NameKey] == "Test"
  }

  /** Test object that always evaluates to false, regardless of the context. */
  object None : PolicyContextRule {
    override val name = "None"
    override val operands: List<PolicyContextRule> = emptyList()

    override fun invoke(context: TypedMap): Boolean = false
  }

  lateinit var context: TypedMap

  @Before
  fun setup() {
    context = TypedMap(MutableTypedMap())
  }

  @Test
  fun policyContextRule_all() {
    assertThat(All.name).isEqualTo("All")
    assertThat(All(context)).isTrue()
  }

  @Test
  fun policyContextRule_and() {
    assertThat((All and All).name).isEqualTo("And")
    assertThat((All and All)(context)).isTrue() // true && true
    assertThat((All and None)(context)).isFalse() // true && false
    assertThat((None and None)(context)).isFalse() // false && false
  }

  @Test
  fun policyContextRule_or() {
    assertThat((All or All).name).isEqualTo("Or")
    assertThat((All or All)(context)).isTrue() // true || true
    assertThat((All or None)(context)).isTrue() // true || false
    assertThat((None or All)(context)).isTrue() // false || true
    assertThat((None or None)(context)).isFalse() // false || false
  }

  @Test
  fun policyContextRule_not() {
    assertThat(not(All).name).isEqualTo("Not")
    assertThat(not(All)(context)).isFalse() // !true
    assertThat(not(None)(context)).isTrue() // !false
  }

  @Test
  fun policyContextRule_withContext() {
    assertThat(TestName(context)).isFalse() // null != "Test"

    var mutableTypedMap = MutableTypedMap()
    mutableTypedMap[NameKey] = "hello"
    context = TypedMap(mutableTypedMap)
    assertThat(TestName(context)).isFalse() // "hello" != "Test"

    mutableTypedMap[NameKey] = "Test"
    context = TypedMap(mutableTypedMap)
    assertThat(TestName(context)).isTrue() // "Test" == "Test"
  }
}
