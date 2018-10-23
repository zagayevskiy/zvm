package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.common.Opcodes.ALOAD
import com.zagayevskiy.zvm.common.Opcodes.CALL
import com.zagayevskiy.zvm.common.Opcodes.IADD
import com.zagayevskiy.zvm.common.Opcodes.ICONST
import com.zagayevskiy.zvm.common.Opcodes.JMP
import com.zagayevskiy.zvm.common.Opcodes.LLOAD
import com.zagayevskiy.zvm.common.Opcodes.LSTORE
import com.zagayevskiy.zvm.common.Opcodes.OUT
import com.zagayevskiy.zvm.common.Opcodes.RET
import com.zagayevskiy.zvm.util.extensions.*


data class RuntimeFunction(val address: Address, val args: Int, val locals: Int)

private class StackFrame(val args: List<StackEntry>, val locals: MutableList<StackEntry>, val returnAddress: Address)

sealed class StackEntry {

    class Integer(defaultValue: Int) : StackEntry() {
        private var value: Int = defaultValue
        private fun mutate(newValue: Int) = this.run { value = newValue }

        companion object Pool : RuntimePool<Integer, Int> by RuntimePoolImpl(10, ::Integer, Integer::mutate)

        fun recycle(): Int = value.also { recycle(this) }
    }

    object Null : StackEntry()
}

fun Int.toStackEntry() = StackEntry.Integer.obtain(this)

class VirtualMachine(info: LoadedInfo) {
    private val functions = info.functions
    private val mainIndex = info.mainIndex
    private val bytecode = info.bytecode

    private var ip: Int = functions[mainIndex].address

    private val callStack = stack<StackFrame>()
    private val operandsStack = stack<StackEntry>()

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
                IADD -> add()
                ICONST -> push(decodeNextInt().toStackEntry())
                ALOAD -> argLoad()
                LLOAD -> localLoad()
                LSTORE -> localStore()


                OUT -> out()

                else -> error("Unknown bytecode $code")
            }
        }
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
        if (first !is StackEntry.Integer || second !is StackEntry.Integer) error("Can't add $first to $second")
        push((first.recycle() + second.recycle()).toStackEntry())
    }

    private fun out() = pop().let { entry ->
        return@let when (entry) {
            is StackEntry.Integer -> print(entry.recycle())
            is StackEntry.Null -> print("null")
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

