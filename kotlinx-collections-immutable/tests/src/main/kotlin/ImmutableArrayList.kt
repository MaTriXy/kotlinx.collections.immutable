package kotlinx.collections.immutable

import java.util.*
import kotlin.collections.AbstractList

public class ImmutableArrayList<out E> private constructor(private val impl: Array<E>): PersistentList<E>, ImmutableList<E>, AbstractList<E>() {

    override val size: Int get() = impl.size
    override fun isEmpty(): Boolean = impl.isEmpty()
    override fun contains(element: @UnsafeVariance E): Boolean = impl.contains(element)
    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean = elements.all { impl.contains(it) }
    override fun get(index: Int): E = impl[index]
    override fun indexOf(element: @UnsafeVariance E): Int = impl.indexOf(element)
    override fun lastIndexOf(element: @UnsafeVariance E): Int = impl.lastIndexOf(element)


    override fun iterator(): Iterator<E> = impl.iterator()

    override fun subList(fromIndex: Int, toIndex: Int): ImmutableArrayList<E> = ImmutableArrayList(impl.copyOfRange(fromIndex, toIndex))

    override fun add(element: @UnsafeVariance E): PersistentList<E> = ImmutableArrayList(impl + element)

    override fun addAll(elements: Collection<@UnsafeVariance E>): PersistentList<E> = ImmutableArrayList(impl + elements)

    override fun remove(element: @UnsafeVariance  E): PersistentList<E> {
        val index = indexOf(element).takeUnless { it < 0 } ?: return this
        return removeAt(index)
    }

    override fun removeAll(elements: Collection<@UnsafeVariance E>): PersistentList<E> {
        val values = impl.filter { it in elements }
        return if (values.size < impl.size) ImmutableArrayList(values.toTypedArray<Any?>() as Array<E>) else this
    }

    override fun removeAll(predicate: (E) -> Boolean): PersistentList<E> {
        val values = impl.filterNot(predicate)
        return if (values.size < impl.size) ImmutableArrayList(values.toTypedArray<Any?>() as Array<E>) else this
    }

    override fun clear(): PersistentList<E> = if (impl.isEmpty()) this else EMPTY

    override fun addAll(index: Int, c: Collection<@UnsafeVariance E>): PersistentList<E> = mutate { it.addAll(index, c) }

    override fun set(index: Int, element: @UnsafeVariance E): PersistentList<E> = ImmutableArrayList(impl.copyOf().apply { set(index, element) })

    override fun add(index: Int, element: @UnsafeVariance E): PersistentList<E> = mutate { it.add(index, element) }

    override fun removeAt(index: Int): PersistentList<E> = mutate { it.removeAt(index) }

    override fun builder(): PersistentList.Builder<@UnsafeVariance E> = object : ArrayList<E>(impl.asList()), PersistentList.Builder<E> {
        override fun build(): PersistentList<E> = if (this.modCount == 0) this@ImmutableArrayList else ImmutableArrayList(this.toArray() as Array<E>)
    }

    override fun asCollection(): ImmutableCollection<E> = this
    override fun asList(): ImmutableList<E> = this

    companion object {
        public val EMPTY: ImmutableArrayList<Nothing> = ImmutableArrayList(emptyArray<Any?>()) as ImmutableArrayList<Nothing>
    }
}