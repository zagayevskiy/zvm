package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.common.Opcodes.Call
import com.zagayevskiy.zvm.common.Opcodes.Ret
import com.zagayevskiy.zvm.util.extensions.copyToInt


data class RuntimeFunction(val address: Address, val args: Int, val locals: Int)

private class StackFrame(val args: List<Entry>, val locals: MutableList<Entry>, val returnAddress: Address)

sealed class Entry {
    data class Int(val value: Int) : Entry()

    object Null : Entry()
}

class VirtualMachine(info: LoadedInfo) {
    private val functions = info.functions
    private val mainIndex = info.mainIndex
    private val bytecode = info.bytecode

    private var ip: Int = functions[mainIndex].address

    private val callStack = stack<StackFrame>()
    private val operandsStack = stack<Entry>()

    fun run(args: List<Entry>) {
        args.forEach { push(it) }
        call(mainIndex)
        loop()
        log("Program finished")
    }

    private fun loop() {
        while (true) {
            val code = bytecode[ip]
            when (code) {
                Call -> {
                    ++ip
                    call(functionIndex = decodeNextInt())
                }
                Ret -> {
                    if (callStack.size == 1) return
                    ret()
                }
            }
        }
    }

    private fun decodeNextInt(): Int = bytecode.copyToInt(startIndex = ip).also { ip += 4 }

    private fun call(functionIndex: Int) {
        val function = functions[functionIndex]
        log("Call from $ip function #$functionIndex: $function")
        val args = (0 until function.args).map { pop() }
        val locals = (0 until function.locals).map { Entry.Null }.toMutableList<Entry>()
        callStack.push(StackFrame(args, locals, ip))
        ip = function.address
        log("args = $args")
        log("locals = $locals")
    }

    private fun ret() {
        val frame = callStack.pop()
        ip = frame.returnAddress
        log("Ret to $ip")
    }

    private fun pop() = operandsStack.pop()

    private fun push(entry: Entry) = operandsStack.push(entry)

    private fun log(message: String) = println(message)
}


private typealias Stack<T> = MutableList<T>

private fun <T> Stack<T>.push(value: T) = add(value)
private fun <T> Stack<T>.pop(): T = removeAt(size - 1)
private fun <T> Stack<T>.peek(): T = get(size - 1)

private fun <T> stack(): Stack<T> = mutableListOf()