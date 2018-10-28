package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.Memory
import com.zagayevskiy.zvm.MemoryBitTable
import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.common.Opcodes.ADDB
import com.zagayevskiy.zvm.common.Opcodes.ALLOC
import com.zagayevskiy.zvm.common.Opcodes.ALOADI
import com.zagayevskiy.zvm.common.Opcodes.CALL
import com.zagayevskiy.zvm.common.Opcodes.FREE
import com.zagayevskiy.zvm.common.Opcodes.ADDI
import com.zagayevskiy.zvm.common.Opcodes.ANDB
import com.zagayevskiy.zvm.common.Opcodes.ANDI
import com.zagayevskiy.zvm.common.Opcodes.CONSTI
import com.zagayevskiy.zvm.common.Opcodes.DIVB
import com.zagayevskiy.zvm.common.Opcodes.DIVI
import com.zagayevskiy.zvm.common.Opcodes.JMP
import com.zagayevskiy.zvm.common.Opcodes.LLOADI
import com.zagayevskiy.zvm.common.Opcodes.LSTORI
import com.zagayevskiy.zvm.common.Opcodes.MLOADI
import com.zagayevskiy.zvm.common.Opcodes.MODB
import com.zagayevskiy.zvm.common.Opcodes.MODI
import com.zagayevskiy.zvm.common.Opcodes.MSTORI
import com.zagayevskiy.zvm.common.Opcodes.MULB
import com.zagayevskiy.zvm.common.Opcodes.MULI
import com.zagayevskiy.zvm.common.Opcodes.NOTB
import com.zagayevskiy.zvm.common.Opcodes.NOTI
import com.zagayevskiy.zvm.common.Opcodes.ORB
import com.zagayevskiy.zvm.common.Opcodes.ORI
import com.zagayevskiy.zvm.common.Opcodes.OUT
import com.zagayevskiy.zvm.common.Opcodes.RET
import com.zagayevskiy.zvm.common.Opcodes.XORB
import com.zagayevskiy.zvm.common.Opcodes.XORI
import com.zagayevskiy.zvm.util.extensions.*


data class RuntimeFunction(val address: Address, val args: Int, val locals: Int)

private class StackFrame(val args: List<StackEntry>, val locals: MutableList<StackEntry>, val returnAddress: Address)

sealed class StackEntry {

    class VMInteger(val intValue: Int) : StackEntry()
    class VMByte(val byteValue: Byte) : StackEntry()

    object Null : StackEntry()
}

fun Int.toStackEntry() = StackEntry.VMInteger(this)
fun Byte.toStackEntry() = StackEntry.VMByte(this)

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

                CONSTI -> push(decodeNextInt().toStackEntry())
                ALOADI -> argLoadInt()
                LLOADI -> localLoadInt()
                LSTORI -> localStoreInt()
                MSTORI -> memoryStoreInt()
                MLOADI -> memoryLoadInt()

                ADDI -> addi()
                MULI -> muli()
                DIVI -> divi()
                MODI -> modi()
                XORI -> xori()
                ANDI -> andi()
                ORI -> ori()
                NOTI -> noti()

                ADDB -> addb()
                MULB -> mulb()
                DIVB -> divb()
                MODB -> modb()
                XORB -> xorb()
                ANDB -> andb()
                ORB -> orb()
                NOTB -> notb()

                OUT -> out()

                ALLOC -> alloc()
                FREE -> free()

                else -> error("Unknown bytecode $code")
            }
        }
    }


    private fun memoryStoreInt() {
        //TODO add checks
        val address = (pop() as StackEntry.VMInteger).intValue
        val offset = (pop() as StackEntry.VMInteger).intValue
        val argument = (pop() as StackEntry.VMInteger).intValue

        heap.writeInt(address + offset, argument)
    }

    private fun memoryLoadInt() {
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

    private fun argLoadInt() = callStack.peek().apply {
        val index = decodeNextInt()
        checkArgIndex(index)
        push(args[index])
    }

    private fun localLoadInt() = callStack.peek().apply {
        val index = decodeNextInt()
        checkLocalIndex(index)
        push(locals[index])
    }

    private fun localStoreInt() = callStack.peek().apply {
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

    //region int exprs
    private fun addi() = binaryIntExpr { left, right -> left + right }

    private fun muli() = binaryIntExpr { left, right -> left * right }

    private fun divi() = binaryIntExpr { left, right -> left / right }

    private fun modi() = binaryIntExpr { left, right -> left % right }

    private fun xori() = binaryIntExpr { left, right -> left xor right }

    private fun andi() = binaryIntExpr { left, right -> left and right }

    private fun ori() = binaryIntExpr { left, right -> left or right }

    private fun noti() = unaryIntExpr { it.inv() }

    private inline fun unaryIntExpr(body: (Int) -> Int) = pop().let { value ->
        if (value !is StackEntry.VMInteger) error("Unary int expression can't be used with $value")
        push(body(value.intValue).toStackEntry())
    }

    private inline fun binaryIntExpr(body: (left: Int, right: Int) -> Int) {
        val left = pop()
        val right = pop()
        if (left !is StackEntry.VMInteger || right !is StackEntry.VMInteger) error("Binary int expression can't be used with $left and $right")
        push(body(left.intValue, right.intValue).toStackEntry())
    }
    //endregion

    //region byte exprs
    private fun addb() = binaryByteExpr { left, right -> left + right }

    private fun mulb() = binaryByteExpr { left, right -> left * right }

    private fun divb() = binaryByteExpr { left, right -> left / right }

    private fun modb() = binaryByteExpr { left, right -> left % right }

    private fun xorb() = binaryByteExpr { left, right -> left xor right }

    private fun andb() = binaryByteExpr { left, right -> left and right }

    private fun orb() = binaryByteExpr { left, right -> left or right }

    private fun notb() = unaryByteExpr { it.toInt().inv().toByte() }

    private inline fun unaryByteExpr(body: (Byte) -> Byte) = pop().let { value ->
        if (value !is StackEntry.VMByte) error("Unary int expression can't be used with $value")
        push(body(value.byteValue).toStackEntry())
    }

    private inline fun binaryByteExpr(body: (left: Byte, right: Byte) -> Int) {
        val left = pop()
        val right = pop()
        if (left !is StackEntry.VMByte || right !is StackEntry.VMByte) error("Binary byte expression can't be used with $left and $right")
        push(body(left.byteValue, right.byteValue).toByte().toStackEntry())
    }
    //endregion

    private fun out() = pop().let { entry ->
        return@let when (entry) {
            is StackEntry.VMInteger -> println(entry.intValue)
            is StackEntry.VMByte -> println(entry.byteValue)
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

