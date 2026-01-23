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

package com.google.android.libraries.pcc.chronicle.api.remote.testutil

import com.google.android.libraries.pcc.chronicle.api.remote.testutil.SimpleProtoMessage.SimpleEnum
import com.google.protobuf.ByteString
import kotlin.random.Random

/** Utility for generating random proto messages for testing. */
class RandomProtoGenerator(seed: Long) {
  private val random = Random(seed)

  /** Generates a random [SimpleProtoMessage]. */
  fun generateSimpleProtoMessage(): SimpleProtoMessage {
    return SimpleProtoMessage.newBuilder()
      .apply {
        doubleField = random.nextDouble()
        floatField = random.nextFloat()
        intField = random.nextInt()
        unsignedIntField = random.nextInt()
        signedIntField = random.nextInt()
        fixedWidthIntField = random.nextInt()
        signedFixedWidthIntField = random.nextInt()
        longField = random.nextLong()
        unsignedLongField = random.nextLong()
        signedLongField = random.nextLong()
        fixedWidthLongField = random.nextLong()
        signedFixedWidthLongField = random.nextLong()
        boolField = random.nextBoolean()
        stringField = STRING_OPTIONS[random.nextInt(STRING_OPTIONS.size)]
        bytesField = BYTES_OPTIONS[random.nextInt(BYTES_OPTIONS.size)]
        enumField = SimpleEnum.values()[random.nextInt(SimpleEnum.values().size - 1)]
      }
      .build()
  }

  /**
   * Generates a random [RepeatedProtoMessage] with its [RepeatedProtoMessage.getIntValues] length
   * capped to [maxLength].
   */
  fun generateRepeatedProtoMessage(maxLength: Int): RepeatedProtoMessage {
    return RepeatedProtoMessage.newBuilder()
      .apply { repeat(random.nextInt(maxLength + 1)) { addIntValues(random.nextInt()) } }
      .build()
  }

  /** Generates a random [TreeProtoMessage] recursively. */
  fun generateTreeProtoMessage(depth: Int, maxWidth: Int): TreeProtoMessage {
    return TreeProtoMessage.newBuilder()
      .apply {
        value = random.nextInt()

        if (depth > 1) {
          repeat(random.nextInt(1, maxWidth + 1)) {
            addChildren(generateTreeProtoMessage(depth - 1, maxWidth))
          }
        }
      }
      .build()
  }

  /**
   * Generates a random [NestedProtoMessage], where [dimension] is used as the arguments when
   * building its nested [RepeatedProtoMessage] and [TreeProtoMessage] using
   * [generateRepeatedProtoMessage] and [generateTreeProtoMessage], respectively.
   */
  fun generateNestedProtoMessage(dimension: Int): NestedProtoMessage {
    return NestedProtoMessage.newBuilder()
      .apply {
        simpleMessage = generateSimpleProtoMessage()
        repeatedMessage = generateRepeatedProtoMessage(dimension)
        treeMessage = generateTreeProtoMessage(dimension, dimension)
      }
      .build()
  }

  companion object {
    private val STRING_OPTIONS =
      """
      Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur in mauris quis felis
      malesuada congue non ultricies mi. Aenean pretium metus in viverra convallis. In hendrerit
      lorem eu imperdiet posuere. Nullam tristique eleifend aliquam. Mauris condimentum nibh a
      lacus tincidunt tempus. Vivamus semper est velit, vel fringilla ipsum lacinia a. Aenean eu
      felis sed mi malesuada aliquam ut ac lorem. Suspendisse auctor metus sapien, eu auctor
      nisl pellentesque non. Vivamus quis vulputate neque, eget euismod neque. Aenean ultricies
      convallis justo eu imperdiet.
      """
        .trimIndent()
        .split(". ")
    private val BYTES_OPTIONS =
      STRING_OPTIONS.map { ByteString.copyFrom(it.toByteArray(Charsets.UTF_8)) }
  }
}
