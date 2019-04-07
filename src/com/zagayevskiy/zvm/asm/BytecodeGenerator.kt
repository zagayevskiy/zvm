package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.BackingStruct
import com.zagayevskiy.zvm.common.sizeOf
import com.zagayevskiy.zvm.util.extensions.copyTo

private class ServiceInfoStruct(array: ByteArray, offset: Int) : BackingStruct(array, offset) {
    var mainIndex by int
    var functionsCount by int
    var globalsCount by int
}

private class FunctionTableRowStruct(array: ByteArray, offset: Int) : BackingStruct(array, offset) {
    var address by int
    var argsCount by int
    var localsCount by int
}

class BytecodeGenerator {

    val serviceInfoSize = sizeOf(::ServiceInfoStruct)
    val functionsTableRowSize = sizeOf(::FunctionTableRowStruct)

    fun generate(info: GenerationInfo): ByteArray {

        val functions = info.functions
        val bytecode = info.bytecode

        val result = ByteArray(serviceInfoSize + functionsTableRowSize * functions.size + bytecode.size)

        writeServiceInfo(info, result, 0)

        val functionsTableStart = serviceInfoSize
        functions.forEachIndexed { index, func ->
            writeFunctionTableRow(func, result, functionsTableStart + index * functionsTableRowSize)
        }

        val bytecodeStart = functionsTableStart + functionsTableRowSize * functions.size

        bytecode.copyTo(destination = result, destIndex = bytecodeStart)

        return result
    }

    private fun writeServiceInfo(info: GenerationInfo, array: ByteArray, offset: Int) {
        ServiceInfoStruct(array, offset).apply {
            mainIndex = info.functions.findMain()?.index ?: error("main function not found")
            functionsCount = info.functions.size
            globalsCount = info.globalsCount
        }
    }

    private fun writeFunctionTableRow(function: FunctionDefinition, array: ByteArray, offset: Int) {
        FunctionTableRowStruct(array, offset).apply {
            address = function.address
            argsCount = function.args
            localsCount = function.locals
        }
    }


    private fun error(message: String): Nothing = throw RuntimeException(message)
}

private fun List<FunctionDefinition>.findMain() = firstOrNull { it.name == "main" }