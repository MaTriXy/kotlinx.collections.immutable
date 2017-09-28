package kotlinx.collections.immutable

import org.junit.Test
import kotlin.test.*

class ImmutableSetTest : ImmutableSetTestBase() {
    override fun <T> immutableSetOf(vararg elements: T) = kotlinx.collections.immutable.persistentSetOf(*elements)
}

class ImmutableHashSetTest : ImmutableSetTestBase() {
    override fun <T> immutableSetOf(vararg elements: T) = kotlinx.collections.immutable.persistentHashSetOf(*elements)

    override fun empty() {
        val empty1 = immutableSetOf<Int>()
        val empty2 = immutableSetOf<String>()
        assertEquals<PersistentSet<Any>>(empty1, empty2)
        assertEquals<Set<Any>>(setOf(), empty1.asSet())
//        assertTrue(empty1 === empty2)   // fails to implement this property
    }

    override fun noOperation() {
        // immutableSetOf<Int>().testNoOperation({ clear() }, { clear() })  // fails to implement this property

        val list = "abcxyz12".toList().toPersistentSet()
        with(list) {
            testNoOperation({ add('a') }, { add('a') })
            testNoOperation({ addAll(listOf('a', 'b')) }, { addAll(listOf('a', 'b')) })
            testNoOperation({ remove('d') }, { remove('d') })
            testNoOperation({ removeAll(listOf('d', 'e')) }, { removeAll(listOf('d', 'e')) })
            testNoOperation({ removeAll { it.isUpperCase() } }, { removeAll { it.isUpperCase() } })
        }
    }
}

abstract class ImmutableSetTestBase {

    abstract fun <T> immutableSetOf(vararg elements: T): PersistentSet<T>

    @Test open fun empty() {
        val empty1 = immutableSetOf<Int>()
        val empty2 = immutableSetOf<String>()
        assertEquals<PersistentSet<Any>>(empty1, empty2)
        assertEquals<Set<Any>>(setOf(), empty1.asSet())
        assertTrue(empty1 === empty2)
    }

    @Test fun ofElements() {
        val set0 = setOf("a", "d", 1, null)
        val set1 = immutableSetOf("a", "d", 1, null)
        val set2 = immutableSetOf("a", "d", 1, null)

        assertEquals(set0, set1.asSet())
        assertEquals(set1, set2)
    }

    @Test fun toImmutable() {
        val original = setOf("a", "bar", "cat", null)

        val set = original.toMutableSet() // copy
        var immSet = set.toPersistentSet()
        val immSet2 = immSet.asSet().toPersistentSet()
        assertTrue(immSet2 === immSet)

        assertEquals<Set<*>>(set, immSet.asSet()) // problem
        assertEquals(set.toString(), immSet.toString())
        assertEquals(set.hashCode(), immSet.hashCode())

        set.remove("a")
        assertNotEquals<Set<*>>(set, immSet.asSet())

        immSet = immSet.remove("a")
        assertEquals<Set<*>>(set, immSet.asSet()) // problem
    }

    @Test fun addElements() {
        var set = immutableSetOf<String>()
        set = set.add("x")
        set = set.addAll(set.asSet())
        set = set + "y"
        set += "z"
        set += arrayOf("1", "2").asIterable()
        assertEquals("xyz12".map { it.toString() }.toSet(), set.asSet())
    }


    @Test fun removeElements() {
        val set = "abcxyz12".toList().toPersistentSet()
        fun expectSet(content: String, set: PersistentSet<Char>) {
            assertEquals(content.toSet(), set.asSet())
        }

        expectSet("abcyz12", set.remove('x'))
        expectSet("abcyz12", set - 'x')
        expectSet("abcy12", set.removeAll(setOf('x', 'z')))
        expectSet("abcy12", set - setOf('x', 'z'))
        expectSet("abcxyz", set.removeAll { it.isDigit() })

        assertEquals(emptySet<Char>(), (set - set).asSet())
        assertEquals(emptySet<Char>(), set.clear().asSet())
    }

    @Test fun builder() {
        val builder = immutableSetOf<Char>().builder()
        "abcxaxyz12".toCollection(builder)
        val set = builder.build()
        assertEquals<Set<*>>(set.asSet(), builder)
        assertTrue(set === builder.build(), "Building the same set without modifications")

        val set2 = builder.toPersistentSet()
        assertTrue(set2 === set, "toImmutable calls build()")

        with(set) {
            testMutation { add('K') }
            testMutation { addAll("kotlin".toSet()) }
            testMutation { remove('x') }
            testMutation { removeAll(setOf('x', 'z')) }
            testMutation { removeAll { it.isDigit() } }
            testMutation { clear() }
            testMutation { retainAll("xyz".toSet()) }
            testMutation { retainAll { it.isDigit() } }
        }
    }

    fun <T> PersistentSet<T>.testMutation(operation: MutableSet<T>.() -> Unit) {
        val mutable = this.asSet().toMutableSet()
        val builder = this.builder()

        operation(mutable)
        operation(builder)

        assertEquals(mutable, builder)
        assertEquals<Set<*>>(mutable, builder.build().asSet())
    }



    @Test open fun noOperation() {
        immutableSetOf<Int>().testNoOperation({ clear() }, { clear() })

        val set = "abcxyz12".asIterable().toPersistentSet()
        with(set) {
            testNoOperation({ add('a') }, { add('a') })
            testNoOperation({ addAll(listOf('a', 'b')) }, { addAll(listOf('a', 'b')) })
            testNoOperation({ remove('d') }, { remove('d') })
            testNoOperation({ removeAll(listOf('d', 'e')) }, { removeAll(listOf('d', 'e')) })
            testNoOperation({ removeAll { it.isUpperCase() } }, { removeAll { it.isUpperCase() } })
        }
    }

    fun <T> PersistentSet<T>.testNoOperation(persistent: PersistentSet<T>.() -> PersistentSet<T>, mutating: MutableSet<T>.() -> Unit) {
        val result = this.persistent()
        val buildResult = this.mutate(mutating)
        // Ensure non-mutating operations return the same instance
        assertTrue(this === result)
        assertTrue(this === buildResult)
    }

}