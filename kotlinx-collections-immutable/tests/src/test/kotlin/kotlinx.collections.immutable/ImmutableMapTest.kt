package kotlinx.collections.immutable

import org.junit.Test
import test.collections.behaviors.*
import test.collections.compare
import java.util.*
import kotlin.test.*

class ImmutableHashMapTest : ImmutableMapTest() {
    override fun <K, V> persistentMapOf(vararg pairs: Pair<K, V>): PersistentMap<K, V> = kotlinx.collections.immutable.persistentHashMapOf(*pairs)
}
class ImmutableOrderedMapTest : ImmutableMapTest() {
    override fun <K, V> persistentMapOf(vararg pairs: Pair<K, V>): PersistentMap<K, V> = kotlinx.collections.immutable.persistentMapOf(*pairs)
    override fun <K, V> compareMaps(expected: Map<K, V>, actual: Map<K, V>) = compare(expected, actual) { mapBehavior(ordered = true) }

    @Test fun iterationOrder() {
        var map = persistentMapOf("x" to null, "y" to 1)
        compare(setOf("x", "y"), map.keys) { setBehavior(ordered = true) }

        map += "x" to 1
        compare(setOf("x", "y"), map.keys) { setBehavior(ordered = true) }

        map = map.remove("x")
        map += "x" to 2
        compare(setOf("y", "x"), map.keys) { setBehavior(ordered = true) }
        compare(listOf(1, 2), map.values) { collectionBehavior(ordered = true) }
        compare(mapOf("y" to 1, "x" to 2).entries, map.entries) { setBehavior(ordered = true) }
    }
}

abstract class ImmutableMapTest {

    abstract fun <K, V> persistentMapOf(vararg pairs: Pair<K, V>): PersistentMap<K, V>

    open fun <K, V> compareMaps(expected: Map<K, V>, actual: Map<K, V>) = compareMapsUnordered(expected, actual)
    fun <K, V> compareMapsUnordered(expected: Map<K, V>, actual: Map<K, V>) = compare(expected, actual) { mapBehavior(ordered = false) }


    @Test fun empty() {
        val empty1 = persistentMapOf<Int, String>()
        val empty2 = persistentMapOf<String, Int>()
        assertEquals<PersistentMap<*, *>>(empty1, empty2)
        assertEquals(mapOf<Int, String>(), empty1.asMap())
        assertTrue(empty1 === empty2)

        compareMaps(emptyMap(), empty1.asMap())
    }


    @Test fun ofPairs() {
        val map0 = mapOf("x" to 1, "y" to null, null to 2)
        val map1 = persistentMapOf("x" to 1, "y" to null, null to 2)
        val map2 = persistentMapOf("x" to 1, "y" to null, null to 2)

        compareMaps(map0, map1.asMap())
        compareMaps(map1.asMap(), map2.asMap())
    }


    @Test fun toImmutable() {
        val original = mapOf("x" to 1, "y" to null, null to 2)
        val immOriginal = original.toPersistentMap()
        compareMaps(original, immOriginal.asMap())


        val map = HashMap(original) // copy
        var immMap = map.toPersistentMap()
        val immMap2 = immMap.asMap().toPersistentMap()
        assertTrue(immMap2 === immMap)

        compareMapsUnordered(original, immMap.asMap())
        compareMapsUnordered(map, immMap.asMap())

        map.remove(null)
        assertNotEquals<Map<*, *>>(map, immMap.asMap())

        immMap = immMap.remove(null)
        assertEquals<Map<*, *>>(map, immMap.asMap()) // problem
    }


    @Test fun putElements() {
        var map = persistentMapOf<String, Int?>()
        map = map.put("x", 0)
        map = map.put("x", 1)
        map = map.putAll(arrayOf("x" to null))
        map = map + ("y" to null)
        map += "y" to 1
        assertEquals(mapOf("x" to null, "y" to 1), map.asMap())

        map += map
        map += map.asMap().map { it.key + "!" to it.value }

        assertEquals(map.size, map.entries.size)

        assertEquals(mapOf("x" to null, "y" to 1, "x!" to null, "y!" to 1), map.asMap())
    }

    @Test fun removeElements() {
        val map = persistentMapOf("x" to 1, null to "x")

        fun <K, V> assertEquals(expected: Map<out K, V>, actual: Map<out K, V>) = kotlin.test.assertEquals(expected, actual)

        assertEquals(mapOf("x" to 1), map.remove(null).asMap())
        assertEquals(mapOf("x" to 1), map.remove(null, "x").asMap())
        assertEquals(map, map.remove("x", 2))

        assertEquals(emptyMap(), map.clear().asMap())
        assertEquals(emptyMap(), map.remove("x").remove(null).asMap())
    }


    @Test fun builder() {

        val builder = persistentMapOf<Char, Int?>().builder()
        "abcxaxyz12".associateTo(builder) { it to it.toInt() }
        val map = builder.build()
        assertEquals<Map<*, *>>(map.asMap(), builder)
        assertTrue(map === builder.build(), "Building the same list without modifications")

        val map2 = builder.toImmutableMap()
        assertTrue(map2 === map, "toImmutable calls build()")

        with(map) {
            testMutation { put('K', null) }
            testMutation { putAll("kotlin".associate { it to 0 }) }
            testMutation { this['a'] = null }
            testMutation { remove('x') }
            testMutation { clear() }
        }
    }

    fun <K, V> PersistentMap<K, V>.testMutation(operation: MutableMap<K, V>.() -> Unit) {
        val mutable = HashMap(this.asMap()) as MutableMap<K, V>
        val builder = this.builder()

        operation(mutable)
        operation(builder)

        compareMapsUnordered(mutable, builder)
        compareMapsUnordered(mutable, builder.build().asMap())
    }

    @Test fun noOperation() {
        persistentMapOf<Int, String>().testNoOperation({ clear() }, { clear() })

        val map = persistentMapOf("x" to 1, null to "x")
        with(map) {
            testNoOperation({ remove("y") }, { remove("y") })
            testNoOperation({ remove("x", 2) }, { remove("x", 2) })
            testNoOperation({ put("x", 1) }, { put("x", 1) })     // does not hold
            testNoOperation({ putAll(this.asMap()) }, { putAll(this) })   // does not hold
            testNoOperation({ putAll(emptyMap()) }, { putAll(emptyMap()) })
        }
    }

    fun <K, V> PersistentMap<K, V>.testNoOperation(persistent: PersistentMap<K, V>.() -> PersistentMap<K, V>, mutating: MutableMap<K, V>.() -> Unit) {
        val result = this.persistent()
        val buildResult = this.mutate(mutating)
        // Ensure non-mutating operations return the same instance
        assertTrue(this === result)
        assertTrue(this === buildResult)
    }


    @Test
    fun covariantTyping() {
        val mapNothing = persistentMapOf<Nothing, Nothing>()
        val mapSI: PersistentMap<String, Int> = mapNothing + ("x" to 1)
        val mapSNI: PersistentMap<String, Int?> = mapSI + mapOf("y" to null)
        val mapANA: PersistentMap<Any, Any?> = mapSNI + listOf(1 to "x")

        assertEquals(mapOf(1 to "x", "x" to 1, "y" to null), mapANA.asMap())
    }
}