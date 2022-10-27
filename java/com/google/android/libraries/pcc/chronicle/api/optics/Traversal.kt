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

package com.google.android.libraries.pcc.chronicle.api.optics

import com.google.android.libraries.pcc.chronicle.api.operation.Action

typealias ListTraversal<A> = Traversal<List<A>, List<A>, A, A>

typealias ListMapping<A, B> = Traversal<List<A>, List<B>, A, B>

/**
 * A [Traversal] is a functional optic intended for getting or updating 0 to N items.
 *
 * In Chronicle it serves as a way to apply updates to lists, sets, and other collection types.
 */
abstract class Traversal<S, T, A, B>(
  val sourceAccessPath: OpticalAccessPath,
  val targetAccessPath: OpticalAccessPath,
  val sourceEntityType: Class<out S>,
  val targetEntityType: Class<out T>,
  val sourceFieldType: Class<out A>,
  val targetFieldType: Class<out B>
) {
  /** Whether or not the [Traversal] is actually monomorphic. */
  val isMonomorphic: Boolean =
    sourceEntityType == targetEntityType && sourceFieldType == targetFieldType

  /**
   * Given an [entity] of type [S], returns a [Sequence] containing every targeted item of type [A]
   * within the entity.
   */
  abstract fun every(entity: S): Sequence<A>

  /**
   * Given an [entity] of type [S], and a modifier mapping from [A] to [B], returns an instance of
   * [T] where each member of [entity] was converted from [A] to [B].
   */
  abstract fun modify(entity: S, modifier: (A) -> B): T

  /**
   * Similar to [modify] but the result type is an [Action], which allows for more nuanced
   * adjustment to the source data.
   */
  abstract fun modifyWithAction(entity: S, modifier: (value: A) -> Action<out B>): Action<out T>

  /** Lift a [modifier] function mapping from [A] -> [B] to the scope of [S] an [T]. */
  fun lift(modifier: (A) -> B): (S) -> T = { modify(it, modifier) }

  /** Compose this [Traversal] with another [Traversal]. */
  @Suppress("UNCHECKED_CAST") // We do check, actually.
  infix fun <AIn : A, BIn : B, NewA : Any?, NewB : Any?> compose(
    other: Traversal<AIn, BIn, NewA, NewB>
  ): Traversal<S, T, NewA, NewB> {
    require(this canCompose other) { "$this cannot compose with $other" }

    return object :
      Traversal<S, T, NewA, NewB>(
        sourceAccessPath = sourceAccessPath compose other.sourceAccessPath,
        targetAccessPath = targetAccessPath compose other.targetAccessPath,
        sourceEntityType = sourceEntityType,
        targetEntityType = targetEntityType,
        sourceFieldType = other.sourceFieldType,
        targetFieldType = other.targetFieldType
      ) {
      override fun every(entity: S): Sequence<NewA> =
        this@Traversal.every(entity).flatMap { other.every(it as AIn) }

      @Suppress("ALWAYS_NULL", "USELESS_CAST")
      // The [B] type variable must be nullable, but alas: type-erasure.
      override fun modify(entity: S, modifier: (NewA) -> NewB): T =
        this@Traversal.modify(entity) {
          if (it != null) {
            other.modify(it as AIn, modifier)
          } else {
            // If `it` is null, just return it - since `other` can't be a
            it as B
          }
        }

      @Suppress("ALWAYS_NULL", "USELESS_CAST")
      // The [B] type variable must be nullable, but alas: type-erasure.
      override fun modifyWithAction(
        entity: S,
        modifier: (value: NewA) -> Action<out NewB>
      ): Action<out T> {
        return this@Traversal.modifyWithAction(entity) {
          if (it != null) {
            other.modifyWithAction(it as AIn, modifier)
          } else {
            Action.Update(it as B)
          }
        }
      }
    }
  }

  /**
   * Returns whether or not the receiving [Traversal] can compose with the [other] [Traversal].
   *
   * A [Traversal] can compose with another if and only if the original entity type of the [other]
   * can be assigned to the original field of the entity targeted by the LHS ***and*** if the
   * modified entity type of the [other] can be assigned to the modified field of the entity
   * targeted by the LHS.
   */
  infix fun canCompose(other: Traversal<*, *, *, *>): Boolean =
    sourceFieldType.isAssignableFrom(other.sourceEntityType) &&
      targetFieldType.isAssignableFrom(other.targetEntityType)

  override fun toString(): String {
    if (isMonomorphic) return "Traversal($sourceAccessPath)"
    return "Traversal($sourceAccessPath -> $targetAccessPath)"
  }

  companion object {
    /** An [OpticalAccessPath] representing a [Traversal] over a [List]. */
    val LIST_TRAVERSAL_ACCESS_PATH = OpticalAccessPath("List", "forEach")

    /** Creates a [Traversal] operating on a [List] of elements of type [A]. */
    inline fun <reified A : Any> list(): ListTraversal<A> {
      val path =
        OpticalAccessPath("List<${A::class.java.simpleName}>") compose LIST_TRAVERSAL_ACCESS_PATH
      val dummyEntity = emptyList<A>()
      return object :
        ListTraversal<A>(
          sourceAccessPath = path,
          targetAccessPath = path,
          sourceEntityType = dummyEntity::class.java,
          targetEntityType = dummyEntity::class.java,
          sourceFieldType = A::class.java,
          targetFieldType = A::class.java
        ) {
        override fun every(entity: List<A>): Sequence<A> = entity.asSequence()

        override fun modify(entity: List<A>, modifier: (A) -> A): List<A> = entity.map(modifier)

        override fun modifyWithAction(
          entity: List<A>,
          modifier: (value: A) -> Action<out A>
        ): Action<out List<A>> {
          return Action.Update(
            entity.mapNotNull {
              when (val res = modifier(it)) {
                Action.OmitFromParent -> null
                is Action.Update -> res.newValue
                // Immediately bubble-up root omissions and throwing results.
                is Action.OmitFromRoot -> return res
                is Action.Throw -> return res
              }
            }
          )
        }
      }
    }

    /**
     * Creates a [Traversal] operating on a [List] of elements of type [A], mapping them to type [B]
     * .
     */
    inline fun <reified A : Any, reified B : Any> listMap(): ListMapping<A, B> {
      val sourcePath =
        OpticalAccessPath("List<${A::class.java.simpleName}>") compose LIST_TRAVERSAL_ACCESS_PATH
      val targetPath =
        OpticalAccessPath("List<${B::class.java.simpleName}>") compose LIST_TRAVERSAL_ACCESS_PATH
      val dummySource = emptyList<A>()
      val dummyTarget = emptyList<B>()
      return object :
        Traversal<List<A>, List<B>, A, B>(
          sourceAccessPath = sourcePath,
          targetAccessPath = targetPath,
          sourceEntityType = dummySource::class.java,
          targetEntityType = dummyTarget::class.java,
          sourceFieldType = A::class.java,
          targetFieldType = B::class.java
        ) {
        override fun every(entity: List<A>): Sequence<A> = entity.asSequence()

        override fun modify(entity: List<A>, modifier: (A) -> B): List<B> = entity.map(modifier)

        override fun modifyWithAction(
          entity: List<A>,
          modifier: (value: A) -> Action<out B>
        ): Action<out List<B>> {
          return Action.Update(
            entity.mapNotNull {
              when (val res = modifier(it)) {
                Action.OmitFromParent -> null
                is Action.Update -> res.newValue
                // Immediately bubble-up root omissions and throwing results.
                is Action.OmitFromRoot -> return res
                is Action.Throw -> return res
              }
            }
          )
        }
      }
    }
  }
}
