package com.zagayevskiy.zvm.common

import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import com.zagayevskiy.zvm.util.extensions.copyToInt
import com.zagayevskiy.zvm.util.extensions.copyToLong
import kotlin.reflect.KProperty

abstract class BackingStruct(val array: ByteArray, offset: Int) : Sequence<Byte> {
    private val startOffset = offset
    var offset: Int = offset
        protected set(value) {
            if (field > value) throw IllegalArgumentException("Only increasing of offset expected.")
            field = value
        }

    val length
        get() = offset - startOffset

    protected val int
        get() = IntFieldDelegate(offset).also { offset += 4 }

    protected val byte
        get() = ByteFieldDelegate(offset++)

    protected val long
        get() = LongFieldDelegate(offset).also { offset += 8 }

    fun <S : BackingStruct> arrayOf(size: Int, initializer: BackingStructArray.Indexer.(array: ByteArray, offset: Int) -> S) = BackingStructArray(array, offset, size, initializer)

    protected fun <S : BackingStruct> struct(factory: (array: ByteArray, offset: Int) -> S) = StructFieldDelegate(offset, factory).also { offset += sizeOf(factory) }

    protected class LongFieldDelegate(private val offset: Int) {
        operator fun getValue(parent: BackingStruct, property: KProperty<*>): Long = parent.array.copyToLong(offset)

        operator fun setValue(parent: BackingStruct, property: KProperty<*>, value: Long) {
            value.copyToByteArray(parent.array, offset)
        }
    }

    protected class IntFieldDelegate(private val offset: Int) {
        operator fun getValue(parent: BackingStruct, property: KProperty<*>): Int = parent.array.copyToInt(offset)

        operator fun setValue(parent: BackingStruct, property: KProperty<*>, value: Int) {
            value.copyToByteArray(parent.array, offset)
        }
    }


    protected class ByteFieldDelegate(private val offset: Int) {
        operator fun getValue(parent: BackingStruct, property: KProperty<*>): Byte = parent.array[offset]

        operator fun setValue(parent: BackingStruct, property: KProperty<*>, value: Byte) {
            parent.array[offset] = value
        }
    }

    protected class StructFieldDelegate<S : BackingStruct>(private val offset: Int, private val factory: (array: ByteArray, offset: Int) -> S) {
        private var field: S? = null

        operator fun getValue(parent: BackingStruct, property: KProperty<*>): S {
            return field ?: factory(parent.array, offset).also { field = it }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BackingStruct) return false
        if (length != other.length) return false

        return zip(other) { b1, b2 ->
            b1 == b2
        }.all { it }
    }

    override fun hashCode() = foldIndexed(length) { index, hash, byte ->
        hash * 31 + byte + index
    }

    override fun toString(): String {
        return "BackingStruct{ length=$length, bytes=${toList()} }"
    }

    override fun iterator(): Iterator<Byte> = object : Iterator<Byte> {
        private var cursor = startOffset
        override fun hasNext() = cursor != offset

        override fun next(): Byte {
            if (!hasNext()) throw NoSuchElementException("No $cursor element.")

            return array[cursor++]
        }
    }
}


class BackingStructArray<S : BackingStruct>(
        array: ByteArray,
        offset: Int,
        val size: Int,
        private val initializer: Indexer.(array: ByteArray, offset: Int) -> S) : BackingStruct(array, offset){

    private val arrayBeginOffset = offset
    private val indexer = IndexerImpl()
    private val sizeOfElement = sizeOf { array, offset -> indexer.initializer(array, offset) }

    init {
        this@BackingStructArray.offset += size * sizeOfElement
    }

    private val elements: MutableList<S?> = ArrayList(size)

    operator fun get(index: Int): S {
        if (0 > index || index >= size) throw NoSuchElementException("Want $index, has $size")

        return elements[index] ?: createElement(index).also { elements[index] = it }
    }

//    TODO
//    override fun iterator() = object : Iterator<S> {
//        private var cursor = 0
//        override fun hasNext() = cursor < size
//        override fun next() = get(cursor++)
//    }

    private fun createElement(index: Int): S {
        indexer.index = index
        return indexer.initializer(array, arrayBeginOffset + index * sizeOfElement)
    }

    interface Indexer {
        val index: Int
    }

    private class IndexerImpl(override var index: Int = 0) : Indexer
}

private val emptyByteArray = ByteArray(0)
fun <S : BackingStruct> sizeOf(factory: (array: ByteArray, offset: Int) -> S) = factory(emptyByteArray, 0).offset


