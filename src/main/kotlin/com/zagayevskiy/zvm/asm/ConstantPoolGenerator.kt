package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.util.extensions.copyToByteArray


private typealias PoolOffset = Int


private sealed class ConstantPoolEntry(open val offset: PoolOffset) {
    abstract fun copyToByteArray(to: ByteArray, startIndex: Int)
}

private data class ConstantPoolString(val value: String, override val offset: PoolOffset) : ConstantPoolEntry(offset) {
    override fun copyToByteArray(to: ByteArray, startIndex: Int) {
        value.length.copyToByteArray(to, startIndex)
        value.copyToByteArray(to, startIndex + 4)
    }
}

private data class ConstantPoolFunction(
        val name: String,
        val nameOffset: PoolOffset,
        override val offset: PoolOffset,
        val address: Address? = null,
        val args: Int? = null,
        val locals: Int? = null) : ConstantPoolEntry(offset) {
    val defined: Boolean
        get() = address != null && args != null && locals != null

    override fun copyToByteArray(to: ByteArray, startIndex: Int) {
        listOf(address!!, args!!, locals!!, nameOffset).forEachIndexed { index, value ->
            value.copyToByteArray(to, startIndex + index * 4)
        }
    }
}

class ConstantPoolGenerator {
    private val pool = mutableListOf<ConstantPoolEntry>()
    private var poolDataSize = 0

    fun obtainStringIndex(value: String): Int {
        val index = indexOf<ConstantPoolString> { it.value == value }
        if (index != null) return index

        val offset = poolDataSize
        pool.add(ConstantPoolString(value, offset))
        poolDataSize += value.toByteArray(Charsets.UTF_8).size + 4

        return pool.size - 1
    }

    fun obtainFunctionIndex(name: String): Int {
        return indexOf<ConstantPoolFunction> { it.name == name } ?: return appendFunction(name)
    }

    fun defineFunction(name: String, address: Address, args: Int, locals: Int): Boolean {
        val index = indexOf<ConstantPoolFunction> { it.name == name }

        if (index != null) {
            val function = pool[index] as ConstantPoolFunction
            if (function.defined) return false
            pool[index] = function.copy(address = address, args = args, locals = locals)
            return true
        }

        appendFunction(
                name = name,
                address = address,
                args = args,
                locals = locals)
        return true
    }

    fun toByteArray(): ByteArray {
        val serviceInfoSize = 4 + 4 * pool.size
        val totalSize = serviceInfoSize + poolDataSize
        val array = ByteArray(totalSize)
        pool.size.copyToByteArray(array, 0)

        pool.forEachIndexed { index, entry ->
            entry.offset.copyToByteArray(array, 4 + index * 4)
            entry.copyToByteArray(array, serviceInfoSize + entry.offset)
        }

        return array
    }


    private fun appendFunction(name: String, address: Address? = null, args: Int? = null, locals: Int? = null): Int {
        val nameOffset = obtainStringIndex(name)
        val offset = poolDataSize
        pool.add(ConstantPoolFunction(
                name = name,
                nameOffset = nameOffset,
                offset = offset,
                address = address,
                args = args,
                locals = locals))
        poolDataSize += 4 * 4 //4 arguments 4 byte each TODO refactor

        return pool.size - 1
    }

    private inline fun <reified T : ConstantPoolEntry> indexOf(predicate: (T) -> Boolean): Int? = pool
            .indexOfFirst { it is T && predicate(it) }
            .takeIf { it != -1 }
}