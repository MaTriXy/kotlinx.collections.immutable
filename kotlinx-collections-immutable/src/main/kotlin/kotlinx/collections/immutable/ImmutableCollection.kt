/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.collections.immutable

public interface ImmutableCollection<out E> : Collection<E> {}

public interface PersistentCollection<out E> {

    // Query Operations
    /**
     * Returns the size of the collection.
     */
    public val size: Int

    /**
     * Returns `true` if the collection is empty (contains no elements), `false` otherwise.
     */
    public fun isEmpty(): Boolean

    /**
     * Checks if the specified element is contained in this collection.
     */
    public operator fun contains(element: @UnsafeVariance E): Boolean

    public operator fun iterator(): Iterator<E>

    // Bulk Operations
    /**
     * Checks if all elements in the specified collection are contained in this collection.
     */
    public fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean


    fun add(element: @UnsafeVariance E): PersistentCollection<E>

    fun addAll(elements: Collection<@UnsafeVariance E>): PersistentCollection<E>

    fun remove(element: @UnsafeVariance E): PersistentCollection<E>

    fun removeAll(elements: Collection<@UnsafeVariance E>): PersistentCollection<E>

    fun removeAll(predicate: (E) -> Boolean): PersistentCollection<E>

    fun clear(): PersistentCollection<E>

    interface Builder<E>: MutableCollection<E> {
        fun build(): PersistentCollection<E>
    }

    fun builder(): Builder<@UnsafeVariance E>

    fun asCollection(): ImmutableCollection<E>
}