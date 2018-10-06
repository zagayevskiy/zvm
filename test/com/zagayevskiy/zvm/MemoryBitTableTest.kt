package com.zagayevskiy.zvm

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class MemoryBitTableTest {

    private lateinit var memory: MemoryBitTable

    @Before
    fun setUp() {
        memory = MemoryBitTable(16384)
    }

    @Test
    fun canAllocateOneBigPiece() {
        val memory = MemoryBitTable(desirableSize = 65536)
        val address = memory.allocate(memory.size - memory.allocationInfoSize)
        assertEquals(memory.allocationInfoSize, address)
        assertEquals(0, memory.freeMemorySize)
    }

    @Test
    fun canAllocateAllMemoryBlockByBlock() {
        val size = 65536
        val blockSize = 32
        val memory = MemoryBitTable(desirableSize = size, blockSize = blockSize)

        val blockCount = size / blockSize

        (0 until blockCount)
                .map { memory.allocate(blockSize - memory.allocationInfoSize) }
                .windowed(size = 2, step = 1)
                .forEach { (prev, cur) ->
                    assertEquals(cur, prev + blockSize)
                }
        assertEquals(0, memory.freeMemorySize)
    }

    @Test
    fun allocation_leadsTo_freeMemoryReduction() {
        val prevFreeMemory = memory.freeMemorySize
        val size = 1000
        memory.allocate(size)

        val curFreeMemory = memory.freeMemorySize
        assertTrue(curFreeMemory <= prevFreeMemory - size, "now=$curFreeMemory, prev=$prevFreeMemory")
    }

    @Test
    fun free_leadsTo_freeMemoryIncrease() {
        val memory = MemoryBitTable(desirableSize = 100000, blockSize = 256)
        val prevFreeMemory = memory.freeMemorySize
        val size = 1000

        val addresses = (0 until 50).map { memory.allocate(size) }

        addresses.forEach { memory.free(it) }

        val curFreeMemory = memory.freeMemorySize
        assertEquals(prevFreeMemory, curFreeMemory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blockSizeMustBeDivisibleByIntSize() {
        MemoryBitTable(desirableSize = 10000, blockSize = 123)
    }

    @Test
    fun copyIn_leadsTo_memoryFilling() {
        val size = 1234
        val prevAddress = memory.allocate(size)
        val address = memory.allocate(size)
        val nextAddress = memory.allocate(size)
        val array = ByteArray(size) { it.toByte() }
        memory.copyIn(array, address)

        (0 until size).forEach { index ->
            val addr = address + index
            val actual = memory[addr]
            val expected = array[index]

            assertEquals(expected, actual)
        }

        ((prevAddress until prevAddress + size) + (nextAddress until nextAddress + size)).forEach { addr ->
            assertEquals(0, memory[addr])
        }
        memory.free(address)
        memory.free(prevAddress)
        memory.free(nextAddress)
    }

    @Test
    fun copyOut_worksCorrect() {
        val array = byteArrayOf(1, 2, 3, 4, 11, 22, 33, 111, 127, -128, 0)
        val out = ByteArray(array.size)
        val address = memory.allocate(memory.size / 2)
        memory.copyIn(array, address, array.size, 0)
        memory.copyOut(address, out)

        (0 until array.size).forEach {
            assertEquals(array[it], out[it])
        }

        memory.free(address)
    }

    @Test
    fun readInt_after_writeInt_worksCorrect() {
        val size = 1024
        val address = memory.allocate(1024)
        var offset = 0
        ((Int.MIN_VALUE..Int.MAX_VALUE step 1024 * 1024)).forEach {
            val addr = address + offset
            memory.writeInt(address = addr, value = it)
            val actual = memory.readInt(address = addr)
            assertEquals(it, actual, "addr=$addr")

            offset = (offset + 1) % size
        }
    }
}