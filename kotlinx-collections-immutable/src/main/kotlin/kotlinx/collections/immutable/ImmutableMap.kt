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

public interface ImmutableMap<K, out V> : Map<K, V> {}


public interface PersistentMap<K, out V> {

    /**
     * Returns the number of key/value pairs in the map.
     */
    public val size: Int

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    public fun isEmpty(): Boolean

    /**
     * Returns `true` if the map contains the specified [key].
     */
    public fun containsKey(key: K): Boolean

    /**
     * Returns `true` if the map maps one or more keys to the specified [value].
     */
    public fun containsValue(value: @UnsafeVariance V): Boolean

    /**
     * Returns the value corresponding to the given [key], or `null` if such a key is not present in the map.
     */
    public operator fun get(key: K): V?

/*    *//**
     * Returns the value corresponding to the given [key], or [defaultValue] if such a key is not present in the map.
     *
     * @since JDK 1.8
     *//*
    @SinceKotlin("1.1")
    @PlatformDependent
    public fun getOrDefault(key: K, defaultValue: @UnsafeVariance V): V {
        // See default implementation in JDK sources
        return null as V
    }*/

    // Views
    /**
     * Returns a read-only [Set] of all keys in this map.
     */
    public val keys: Set<K>

    /**
     * Returns a read-only [Collection] of all values in this map. Note that this collection may contain duplicate values.
     */
    public val values: Collection<V>

    /**
     * Returns a read-only [Set] of all key/value pairs in this map.
     */
    public val entries: Set<Map.Entry<K, V>>




    fun put(key: K, value: @UnsafeVariance V): PersistentMap<K, V>

    fun remove(key: K): PersistentMap<K, V>

    fun remove(key: K, value: @UnsafeVariance V): PersistentMap<K, V>

    fun putAll(m: Map<out K, @UnsafeVariance V>): PersistentMap<K, V>  // m: Iterable<Map.Entry<K, V>> or Map<out K,V> or Iterable<Pair<K, V>>

    fun clear(): PersistentMap<K, V>

    interface Builder<K, V>: MutableMap<K, V> {
        fun build(): PersistentMap<K, V>
    }

    fun builder(): Builder<K, @UnsafeVariance V>

    fun asMap(): ImmutableMap<K, V>
}



