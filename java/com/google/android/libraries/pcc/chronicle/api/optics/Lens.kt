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

/**
 * A functional optic which provides "focus" on a part of an entity (the "original field" of the
 * "original entity") and allows for accessing its value, as well as creating functions to turn
 * source entities into the instances of the [targetEntityType] by operating on the "source field"
 * to return values for the "target field".
 *
 * ### Explanation of the type parameters:
 *
 * The names of these type parameters follow from conventions set-out in other functional
 * programming libraries and literature.
 *
 * * [S]
 * - the source type, or: what is being examined.
 * * [T]
 * - the target type, or: what is created and returned by [lift] or [lift]'s functions.
 * * [A]
 * - the type of the field focused-upon within the source type
 * * [B]
 * - the type of the field whose value is derived-from the field of type [A] in [lift].
 *
 * ### What is it used for?
 *
 * See [the cantrips design doc](http://go/chronicle-cantrips).
 *
 * ### Examples
 *
 * For concrete examples of lenses being used, please see the tests in `LensTest`.
 */
abstract class Lens<S, T, A, B>(
  val sourceAccessPath: OpticalAccessPath,
  val targetAccessPath: OpticalAccessPath,
  val sourceEntityType: Class<out S>,
  val targetEntityType: Class<out T>,
  val sourceFieldType: Class<out A>,
  val targetFieldType: Class<out B>
) {
  /** Whether or not the [Lens] is actually monomorphic. */
  val isMonomorphic: Boolean =
    sourceEntityType == targetEntityType && sourceFieldType == targetFieldType

  /** Representation of this [Lens] as a [Traversal]. */
  private val traversalRepresentation: Traversal<S, T, A, B> by lazy {
    object :
      Traversal<S, T, A, B>(
        sourceAccessPath = sourceAccessPath,
        targetAccessPath = targetAccessPath,
        sourceEntityType = sourceEntityType,
        targetEntityType = targetEntityType,
        sourceFieldType = sourceFieldType,
        targetFieldType = targetFieldType
      ) {
      override fun every(entity: S): Sequence<A> =
        if (entity != null) sequenceOf(get(entity)) else emptySequence()

      override fun modify(entity: S, modifier: (A) -> B): T = this@Lens.modify(entity, modifier)

      override fun modifyWithAction(
        entity: S,
        modifier: (value: A) -> Action<out B>
      ): Action<out T> = this@Lens.modifyWithAction(entity, modifier)

      override fun toString(): String = this@Lens.toString()
    }
  }

  /** Returns the value of the field accessed by the [sourceAccessPath] in the provided [entity]. */
  abstract fun get(entity: S): A

  /** Returns a new [T] based on [entity] with the target field set to [newValue] */
  abstract fun set(entity: S, newValue: B): T

  /**
   * Returns a function capable of turning an [S] into a [T] where the [mapping] is used to
   * determine how to interpret an [A] as a [B].
   *
   * In functional parliance: "lift a function from [A] to [B] to the context of [S] and [T]"
   *
   * For example:
   *
   * ```
   * data class Foo(val x: Int)
   * data class Bar(val y: Boolean)
   *
   * val FooToBar_By_XAndY = Lens.create<Foo, Bar, Int, Boolean>(..) {..}
   *
   * val fooToBarWithEvenOdd = FooToBarByXAndY.lift { x -> x % 2 == 0 }
   *
   * fooToBarWithEvenOdd(Foo(7))  // returns: Bar(false)
   * fooToBarWithEvenOdd(Foo(10)) // returns: Bar(true)
   * ```
   */
  inline fun lift(crossinline mapping: (A) -> B): (S) -> T = { set(it, mapping(get(it))) }

  /**
   * Applies the modifier of the focused field in the [entity] to build and return a new target
   * entity [T].
   *
   * For example:
   *
   * ```
   * data class Foo(val x: Int)
   * data class Bar(val y: Boolean)
   *
   * val FooToBar_By_XAndY = PolymporphicLens.create<Foo, Bar, Int, Boolean>(..)
   *
   * val bar1 = modify(Foo(7)) { it % 2 == 0 } // Bar(false)
   * val bar2 = modify(Foo(7)) { it % 2 != 0 } // Bar(true)
   * ```
   */
  inline fun modify(entity: S, crossinline modifier: (A) -> B): T = lift(modifier)(entity)

  /** Similar to [modify], except the value can take the form of an [Action]. */
  fun modifyWithAction(entity: S, modifier: (value: A) -> Action<out B>): Action<out T> {
    val original = get(entity)
    return when (val res = modifier(original)) {
      is Action.OmitFromParent -> res
      is Action.OmitFromRoot -> res
      is Action.Throw -> res
      is Action.Update -> Action.Update(set(entity, res.newValue))
    }
  }

  /**
   * Composes the receiver/LHS with the [other] [Lens], providing the ability to focus deeper into
   * the [sourceEntityType] of the receiver when fetching values with [get] or converting to the
   * [targetEntityType] with [lift] or [lift].
   *
   * For example:
   *
   * ```kotlin
   * // A lens which lets us get or set a person's pet.
   * val personPetLens: Lens<Person, Person, Pet, Pet>
   * // A lens which lets us get or set a pet's name.
   * val petNameLens: Lens<Pet, Pet, String, String>
   *
   * // Composing the two lenses above gives us one capable of finding/changing the name of a
   * // person's pet.
   * val personPetNameLens: Lens<Person, Person, String, String> =
   *   personPetLens compose petNameLens
   * ```
   */
  @Suppress("UNCHECKED_CAST") // This composition does actually do checking, using `canCompose`.
  infix fun <AIn : A, BIn : B, NewA, NewB> compose(
    other: Lens<AIn, BIn, NewA, NewB>
  ): Lens<S, T, NewA, NewB> {
    require(this canCompose other) { "$this cannot compose with $other" }

    return object :
      Lens<S, T, NewA, NewB>(
        sourceAccessPath = this@Lens.sourceAccessPath compose other.sourceAccessPath,
        targetAccessPath = this@Lens.targetAccessPath compose other.targetAccessPath,
        sourceEntityType = this@Lens.sourceEntityType,
        targetEntityType = this@Lens.targetEntityType,
        sourceFieldType = other.sourceFieldType,
        targetFieldType = other.targetFieldType
      ) {
      override fun get(entity: S): NewA = other.get(this@Lens.get(entity) as AIn)
      override fun set(entity: S, newValue: NewB): T =
        this@Lens.set(
          entity = entity,
          newValue = other.set(entity = this@Lens.get(entity) as AIn, newValue = newValue)
        )
    }
  }

  /** Returns a [Traversal] representation of this [Lens]. */
  fun asTraversal(): Traversal<S, T, A, B> = traversalRepresentation

  /**
   * Returns whether or not the receiving [Lens] can compose with the [other] [Lens].
   *
   * A [Lens] can compose with another if and only if the original entity type of the [other] can be
   * assigned to the original field of the entity targeted by the LHS ***and*** if the modified
   * entity type of the [other] can be assigned to the modified field of the entity targeted by the
   * LHS.
   */
  infix fun canCompose(other: Lens<*, *, *, *>): Boolean =
    sourceFieldType.isAssignableFrom(other.sourceEntityType) &&
      targetFieldType.isAssignableFrom(other.targetEntityType)

  override fun toString(): String {
    if (isMonomorphic) return "Lens($sourceAccessPath)"
    return "Lens($sourceAccessPath -> $targetAccessPath)"
  }

  companion object {
    /** Helper factory function to create a monomorphic instance of a [Lens]. */
    inline fun <reified Entity, reified Focus> create(
      focusAccessPath: OpticalAccessPath,
      crossinline getter: (Entity) -> Focus,
      crossinline setter: (Entity, Focus) -> Entity
    ): Lens<Entity, Entity, Focus, Focus> {
      return object :
        Lens<Entity, Entity, Focus, Focus>(
          sourceAccessPath = focusAccessPath,
          targetAccessPath = focusAccessPath,
          sourceEntityType = Entity::class.java,
          targetEntityType = Entity::class.java,
          sourceFieldType = Focus::class.java,
          targetFieldType = Focus::class.java
        ) {
        override fun get(entity: Entity): Focus = getter(entity)
        override fun set(entity: Entity, newValue: Focus): Entity = setter(entity, newValue)
      }
    }

    /** Helper factory function to create a polymorphic instance of a [Lens]. */
    inline fun <reified S, reified T, reified A, reified B> create(
      sourceAccessPath: OpticalAccessPath,
      targetAccessPath: OpticalAccessPath,
      crossinline getter: (S) -> A,
      crossinline setter: (S, B) -> T
    ): Lens<S, T, A, B> {
      return object :
        Lens<S, T, A, B>(
          sourceAccessPath = sourceAccessPath,
          targetAccessPath = targetAccessPath,
          sourceEntityType = S::class.java,
          targetEntityType = T::class.java,
          sourceFieldType = A::class.java,
          targetFieldType = B::class.java
        ) {
        override fun get(entity: S): A = getter(entity)
        override fun set(entity: S, newValue: B): T = setter(entity, newValue)
      }
    }
  }
}
