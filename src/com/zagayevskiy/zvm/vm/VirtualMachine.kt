package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.common.Address


class RuntimeFunction(val address: Address, val args: Int, val locals: Int)

class StackFrame

class VirtualMachine(info: LoadedInfo) {
    private val functions = info.functions
    private val mainIndex = info.mainIndex
    private val bytecode = info.bytecode

    private var ip: Int = functions[mainIndex].address

    fun run() {

    }
}