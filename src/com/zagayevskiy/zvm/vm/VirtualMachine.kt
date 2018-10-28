package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.Memory
import com.zagayevskiy.zvm.MemoryBitTable
import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.common.Opcodes.ALLOC
import com.zagayevskiy.zvm.common.Opcodes.ALOADI
import com.zagayevskiy.zvm.common.Opcodes.CALL
import com.zagayevskiy.zvm.common.Opcodes.FREE
import com.zagayevskiy.zvm.common.Opcodes.ADDI
import com.zagayevskiy.zvm.common.Opcodes.CONSTI
import com.zagayevskiy.zvm.common.Opcodes.JMP
import com.zagayevskiy.zvm.common.Opcodes.LLOADI
import com.zagayevskiy.zvm.common.Opcodes.LSTORI
import com.zagayevskiy.zvm.common.Opcodes.MLOADI
import com.zagayevskiy.zvm.common.Opcodes.MSTORI
import com.zagayevskiy.zvm.common.Opcodes.OUT
import com.zagayevskiy.zvm.common.Opcodes.RET
import com.zagayevskiy.zvm.util.extensions.*


data class RuntimeFunction(val address: Address, val args: Int, val locals: Int)

private class StackFrame(val args: List<StackEntry>, val locals: MutableList<StackEntry>, val returnAddress: Address)

sealed class StackEntry {

    class VMInteger(val intValue: Int) : StackEntry()
    class VMByte(val byteValue: Byte)

    object Null : StackEntry()
}

fun Int.toStackEntry() = StackEntry.VMInteger(this)

class VirtualMachine(info: LoadedInfo, heapSize: Int = 0) {
    private val functions = info.functions
    private val mainIndex = info.mainIndex
    private val bytecode = info.bytecode

    private var ip: Int = functions[mainIndex].address

    private val callStack = stack<StackFrame>()
    private val operandsStack = stack<StackEntry>()

    private val heap: Memory = MemoryBitTable(heapSize)

    fun run(args: List<StackEntry>) {
        args.forEach { push(it) }
        call(mainIndex)
        loop()
        log("Program finished")
    }

    private fun loop() {
        while (ip < bytecode.size) {
            val code = bytecode[ip]
            ++ip
            when (code) {
                CALL -> {
                    call(functionIndex = decodeNextInt())
                }
                RET -> {
                    if (callStack.size == 1) return
                    ret()
                }
                JMP -> ip = decodeNextInt()
                ADDI -> add()
                CONSTI -> push(decodeNextInt().toStackEntry())
                ALOADI -> argLoad()
                LLOADI -> localLoad()
                LSTORI -> localStore()
                MSTORI -> memoryStore()
                MLOADI -> memoryLoad()

                OUT -> out()

                ALLOC -> alloc()
                FREE -> free()

                else -> error("Unknown bytecode $code")
            }
        }
    }


    private fun memoryStore() {
        //TODO add checks
        val address = (pop() as StackEntry.VMInteger).intValue
        val offset = (pop() as StackEntry.VMInteger).intValue
        val argument = (pop() as StackEntry.VMInteger).intValue

        heap.writeInt(address + offset, argument)
    }

    private fun memoryLoad() {
        //TODO add checks
        val address = (pop() as StackEntry.VMInteger).intValue
        val offset = (pop() as StackEntry.VMInteger).intValue
        push(heap.readInt(address + offset).toStackEntry())
    }


    private fun alloc() {
        val argument = (pop() as? StackEntry.VMInteger) ?: error("alloc argument must be integer")
        val size = (argument).intValue
        if (size <= 0) error("alloc argument must be positive integer. Has $size.")

        push(heap.allocate(size).toStackEntry())
    }

    private fun free() {
        val argument = (pop() as? StackEntry.VMInteger) ?: error("free argument must be integer")
        val address = argument.intValue
        heap.free(address)
    }

    private fun argLoad() = callStack.peek().apply {
        val index = decodeNextInt()
        checkArgIndex(index)
        push(args[index])
    }

    private fun localLoad() = callStack.peek().apply {
        val index = decodeNextInt()
        checkLocalIndex(index)
        push(locals[index])
    }

    private fun localStore() = callStack.peek().apply {
        val index = decodeNextInt()
        checkLocalIndex(index)
        locals[index] = pop()
    }

    private fun decodeNextInt(): Int = bytecode.copyToInt(startIndex = ip).also { ip += 4 }

    private fun call(functionIndex: Int) {
        val function = functions[functionIndex]
        log("call from $ip function #$functionIndex: $function")
        val args = (0 until function.args).map { pop() }
        val locals = (0 until function.locals).map { StackEntry.Null }.toMutableList<StackEntry>()
        callStack.push(StackFrame(args, locals, ip))
        ip = function.address
        log("args = $args")
        log("locals = $locals")
    }

    private fun ret() {
        val frame = callStack.pop()
        ip = frame.returnAddress
        log("ret to $ip")
    }

    private fun pop() = operandsStack.pop()

    private fun push(entry: StackEntry) = operandsStack.push(entry)

    private fun add() {
        val first = pop()
        val second = pop()
        if (first !is StackEntry.VMInteger || second !is StackEntry.VMInteger) error("Can't add $first to $second")
        push((first.intValue + second.intValue).toStackEntry())
    }

    private fun out() = pop().let { entry ->
        return@let when (entry) {
            is StackEntry.VMInteger -> println(entry.intValue)
            is StackEntry.Null -> println("null")
        }
    }

    private fun StackFrame.checkArgIndex(index: Int) {
        if (index < 0 || index >= args.size) error("Invalid argument index: $index. Has ${args.size} args.")
    }

    private fun StackFrame.checkLocalIndex(index: Int) {
        if (index < 0 || index >= locals.size) error("Invalid variable index: $index. Has ${locals.size} locals.")
    }

    private fun log(message: String) = println(message)
}

