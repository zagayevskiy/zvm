package com.zagayevskiy.zvm.common

import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import com.zagayevskiy.zvm.util.extensions.copyToInt
import kotlin.reflect.KProperty

abstract class BackingStruct(val array: ByteArray, offset: Int) {
    var offset: Int = offset
        protected set

    protected val int
        get() = IntFieldDelegate(offset).also { offset += 4 }

    protected val byte
        get() = ByteFieldDelegate(offset++)

    fun <S : BackingStruct> arrayOf(size: Int, initializer: BackingStructArray.Indexer.(array: ByteArray, offset: Int) -> S) = BackingStructArray(array, offset, size, initializer)

    protected fun <S : BackingStruct> struct(factory: (array: ByteArray, offset: Int) -> S) = StructFieldDelegate(offset, factory).also { offset += sizeOf(factory) }

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
}

class BackingStructArray<S : BackingStruct>(
        array: ByteArray,
        offset: Int,
        val size: Int,
        private val initializer: Indexer.(array: ByteArray, offset: Int) -> S) : BackingStruct(array, offset), Iterable<S> {

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

    override fun iterator() = object : Iterator<S> {
        private var cursor = 0
        override fun hasNext() = cursor < size
        override fun next() = get(cursor++)
    }

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


