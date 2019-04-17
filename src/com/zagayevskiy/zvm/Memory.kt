package com.zagayevskiy.zvm

import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.util.BitTable
import com.zagayevskiy.zvm.util.extensions.checkInterval
import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import com.zagayevskiy.zvm.util.extensions.copyToInt
import com.zagayevskiy.zvm.util.extensions.fill

interface Memory {
    fun allocate(size: Int): Address
    fun free(address: Address)

    val freeMemorySize: Int
    val size: Int

    fun copyIn(source: ByteArray, destination: Address, count: Int = source.size, sourceOffset: Int = 0)
    fun copyOut(source: Address, destination: ByteArray, count: Int = destination.size, destinationOffset: Int = 0)

    operator fun get(address: Address): Byte
    operator fun set(address: Address, value: Byte)

    fun readInt(address: Address): Int
    fun writeInt(address: Address, value: Int)
}

private const val INT_BYTES_COUNT = 4

class MemoryBitTable(desirableSize: Int, private val blockSize: Int = 64) : Memory {

    init {
        if (blockSize % INT_BYTES_COUNT != 0) throw IllegalArgumentException("blockSize(=$blockSize) must be divisible by $INT_BYTES_COUNT")
    }

    val allocationInfoSize = INT_BYTES_COUNT
    override val size = computeActualSize(desirableSize)

    private val table = BitTable(size / blockSize)

    //visible just for tests
    private val memory = ByteArray(size)

    override fun allocate(size: Int): Address {
        val actualSize = computeActualSize(size + allocationInfoSize)
        val blockCount = actualSize / blockSize
        val firstBlockIndex = findEmptyPiece(blockCount) ?: throw RuntimeException("OOM, sic!")
        val address = firstBlockIndex * blockSize
        writeServiceInfo(address, blockCount)
        table.fill(firstBlockIndex, firstBlockIndex + blockCount, true)

        return address + allocationInfoSize
    }

    override fun free(address: Address) {
        val actualAddress = address - allocationInfoSize
        val blockCount = readServiceInfo(actualAddress)
        val firstBlockIndex = actualAddress / blockSize

        table.fill(firstBlockIndex, firstBlockIndex + blockCount, false)
    }

    override val freeMemorySize
        get() = (table.size - table.cardinality()) * blockSize

    override fun copyIn(source: ByteArray, destination: Address, count: Int, sourceOffset: Int) {
        copy(source, sourceOffset, memory, destination, count)
    }

    override fun copyOut(source: Address, destination: ByteArray, count: Int, destinationOffset: Int) {
        copy(memory, source, destination, destinationOffset, count)
    }

    override operator fun get(address: Address) = memory[address]

    override operator fun set(address: Address, value: Byte) {
        memory[address] = value
    }

    override fun readInt(address: Address) = memory.copyToInt(startIndex = address)

    override fun writeInt(address: Address, value: Int) {
        value.copyToByteArray(memory, startIndex = address)
    }


    private fun writeServiceInfo(address: Address, blockCount: Int) {
        writeInt(address = address, value = blockCount)
    }

    private fun readServiceInfo(address: Address) = memory.copyToInt(startIndex = address)

    private fun findEmptyPiece(blockCount: Int): Int? {
        require(blockCount > 0)
        val blockFrom = 0
        if (blockCount == 1) return findEmptyBlock(blockFrom)

        var headIndex = blockFrom
        var tailIndex = blockFrom + blockCount - 1
        var nearestNotCheckedIndex = headIndex

        while (tailIndex < table.size) {

            val setIndex = table.checkInterval(nearestNotCheckedIndex, tailIndex) ?: return headIndex

            nearestNotCheckedIndex = tailIndex + 1
            headIndex = setIndex + 1
            tailIndex = headIndex + blockCount - 1
        }

        return null
    }

    private fun findEmptyBlock(blockFrom: Int): Int? = (blockFrom until table.size).firstOrNull { index -> !table[index] }

    private fun computeActualSize(desirableSize: Int): Int {
        val rem = desirableSize % blockSize
        return if (rem == 0) desirableSize else (desirableSize + (blockSize - rem))
    }
}

private fun copy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, count: Int) = System.arraycopy(src, srcPos, dst, dstPos, count)