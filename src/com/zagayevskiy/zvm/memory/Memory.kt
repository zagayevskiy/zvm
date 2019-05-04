package com.zagayevskiy.zvm.memory

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
