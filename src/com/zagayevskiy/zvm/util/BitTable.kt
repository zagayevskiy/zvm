package com.zagayevskiy.zvm.util

import com.zagayevskiy.zvm.util.extensions.bitCount

class TableIndexOutOfBoundsException(val index: Int, val size: Int) : Exception("Size: $size, index: $index")

class BitTable(val size: Int) : Iterable<Boolean> {

    private val cellSize = 32
    private val cellCount = if (size % cellSize == 0) (size / cellSize) else (size / cellSize + 1)

    private val table = IntArray(cellCount)

    operator fun get(bitIndex: Int): Boolean {
        if (bitIndex < 0 || bitIndex >= size) throw TableIndexOutOfBoundsException(bitIndex, size)
        val cell = table[cellIndex(bitIndex)]
        return cell and (1 shl indexInCell(bitIndex)) != 0
    }

    operator fun set(bitIndex: Int, value: Boolean) {
        val cellIndex = cellIndex(bitIndex)
        val cell = table[cellIndex]
        table[cellIndex] = if (value) {
            cell or (1 shl indexInCell(bitIndex))
        } else {
            cell and ((1 shl indexInCell(bitIndex)).inv())
        }
    }

    fun cardinality() = table.sumBy { it.bitCount() }

    override fun iterator(): Iterator<Boolean> = TableIterator()

    private fun indexInCell(bitIndex: Int) = bitIndex % cellSize

    private fun cellIndex(bitIndex: Int) = bitIndex / cellSize

    private inner class TableIterator : Iterator<Boolean> {
        private var cursor = 0

        override fun hasNext() = cursor < size

        override fun next() = if (!hasNext())
            throw NoSuchElementException()
        else
            get(cursor++)

    }
}
