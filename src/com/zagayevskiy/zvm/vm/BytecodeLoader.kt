package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.common.BackingStruct
import com.zagayevskiy.zvm.common.sizeOf
import com.zagayevskiy.zvm.util.extensions.copyTo

sealed class LoadingResult {
    class Success(val info: LoadedInfo) : LoadingResult()
    class Failure(val message: String) : LoadingResult()
}

class LoadedInfo(val globalsCount: Int, val functions: List<RuntimeFunction>, val mainIndex: Int, val bytecode: ByteArray)

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

class BytecodeLoader(private val rawBytecode: ByteArray) {

    fun load(): LoadingResult {
        val serviceInfoSize = sizeOf(::ServiceInfoStruct)
        val functionRowSize = sizeOf(::FunctionTableRowStruct)
        val rawBytecodeSize = rawBytecode.size

        if (rawBytecodeSize < serviceInfoSize + 1) return LoadingResult.Failure("Bytecode too small: ${rawBytecode.size} bytes")

        val serviceInfo = ServiceInfoStruct(rawBytecode, 0)
        val mainIndex = serviceInfo.mainIndex
        val functionsCount = serviceInfo.functionsCount
        val globalsCount = serviceInfo.globalsCount

        if (functionsCount <= 0) return LoadingResult.Failure("Functions count must be positive. Has $functionsCount.")
        if (mainIndex < 0 || mainIndex >= functionsCount) return LoadingResult.Failure("Invalid main index ($mainIndex). Has $functionsCount} functions")

        val minSizeWithFuncs = serviceInfoSize + functionsCount * functionRowSize + 1
        if (rawBytecodeSize < minSizeWithFuncs) return LoadingResult.Failure("Bytecode too small: ${rawBytecode.size} bytes. Must be at least $minSizeWithFuncs bytes. Declared $functionsCount functions.")

        @Suppress("UnnecessaryVariable")
        val functionTableStart = serviceInfoSize

        val functions = (0 until functionsCount).map { index ->
            val functionInfo = FunctionTableRowStruct(rawBytecode, functionTableStart + index * functionRowSize)
            RuntimeFunction(
                    address = functionInfo.address,
                    args = functionInfo.argsCount,
                    locals = functionInfo.localsCount)
        }

        val bytecodeStart = functionTableStart + functionsCount * functionRowSize
        val bytecodeSize = rawBytecodeSize - bytecodeStart

        val checked = checkFunctions(functions, bytecodeSize)
        if (checked != null) return LoadingResult.Failure(checked)

        val bytecode = ByteArray(bytecodeSize)
        rawBytecode.copyTo(destination = bytecode, sourceIndex = bytecodeStart, count = bytecodeSize)

        return LoadingResult.Success(LoadedInfo(globalsCount, functions, mainIndex, bytecode))
    }


    private fun checkFunctions(functions: List<RuntimeFunction>, bytecodeSize: Int): String? = functions
            .mapIndexed { index, f ->
                when {
                    f.address < 0 -> "Function #$index address is ${f.address}. Must be positive."
                    f.address >= bytecodeSize -> "Function #$index address is ${f.address}. Must be less than size of bytecode($bytecodeSize)."
                    f.args < 0 -> "Function #$index arguments count(${f.args}) must be not less than 0."
                    f.locals < 0 -> "Function #$index locals count(${f.locals}) must be not less than 0."
                    else -> null
                }
            }
            .firstOrNull()

}