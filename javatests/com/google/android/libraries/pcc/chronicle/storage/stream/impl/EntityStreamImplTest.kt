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

package com.google.android.libraries.pcc.chronicle.storage.stream.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.pcc.chronicle.api.remote.testutil.Person
import com.google.android.libraries.pcc.chronicle.api.storage.EntityMetadata
import com.google.android.libraries.pcc.chronicle.api.storage.WrappedEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntityStreamImplTest {
  @Test
  fun publish_subscribe_groups(): Unit = runBlocking {
    val stream = EntityStreamImpl<Person>()

    val collected =
      async(start = CoroutineStart.UNDISPATCHED) {
        // Simulate a client who is interested in listening until only the second group emission
        // (maybe they wanted to wait for larry's entity).
        stream.subscribeGroups().take(2).toList()
      }

    stream.publishGroup(GROUPED_DATA[0])
    stream.publishGroup(GROUPED_DATA[1])

    assertThat(collected.await()).isEqualTo(GROUPED_DATA)

    // Send the first page again, this shouldn't get received by the subscriber part.
    stream.publishGroup(GROUPED_DATA[0])
    // If we get to this line without timing out, then the stream is still alive and isn't
    // suspending while waiting for a subscriber to consume the item. (This is what we want)
  }

  @Test
  fun publish_subscribe_individual(): Unit = runBlocking {
    val stream = EntityStreamImpl<Person>()

    val collected =
      async(start = CoroutineStart.UNDISPATCHED) {
        // Simulate a client who is interested in listening until it hears larry's entity.
        stream.subscribe().first { it.entity.name == "larry" }
      }

    // Publish all of our individual data.
    INDIVIDUAL_DATA.forEach { stream.publish(it) }

    assertThat(collected.await()).isEqualTo(INDIVIDUAL_DATA[1])
  }

  companion object {
    private val GROUPED_DATA =
      listOf(
        listOf(
          WrappedEntity(
            metadata = EntityMetadata.getDefaultInstance(),
            entity = Person.newBuilder().setName("sundar").build(),
          )
        ),
        listOf(
          WrappedEntity(
            metadata = EntityMetadata.getDefaultInstance(),
            entity = Person.newBuilder().setName("larry").build(),
          ),
          WrappedEntity(
            metadata = EntityMetadata.getDefaultInstance(),
            entity = Person.newBuilder().setName("sergey").build(),
          ),
        ),
      )

    private val INDIVIDUAL_DATA = GROUPED_DATA.flatten()
  }
}
