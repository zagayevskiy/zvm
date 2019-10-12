package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.common.BackingStruct
import com.zagayevskiy.zvm.common.sizeOf
import com.zagayevskiy.zvm.util.extensions.copyTo

sealed class LoadingResult {
    class Success(val info: LoadedInfo) : LoadingResult()
    class Failure(val message: String) : LoadingResult()
}

class LoadedInfo(val globalsCount: Int, val functions: List<RuntimeFunction>, val mainIndex: Int, val bytecode: ByteArray)

internal class ServiceInfoStruct(array: ByteArray, offset: Int) : BackingStruct(array, offset) {
    var mainIndex by int
    var functionsCount by int
    var globalsCount by int
}

internal class FunctionTableRowStruct(array: ByteArray, offset: Int) : BackingStruct(array, offset) {
    var address by int
    var argsCount by int
    var argsDescription by long
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
                    argTypes = functionInfo.readArgsTypes())
        }

        val bytecodeStart = functionTableStart + functionsCount * functionRowSize
        val bytecodeSize = rawBytecodeSize - bytecodeStart

        val checked = checkFunctions(functions, bytecodeSize)
        if (checked != null) return LoadingResult.Failure(checked)

        val bytecode = ByteArray(bytecodeSize)
        rawBytecode.copyTo(destination = bytecode, sourceIndex = bytecodeStart, count = bytecodeSize)

        return LoadingResult.Success(LoadedInfo(globalsCount, functions, mainIndex, bytecode))
    }

    //TODO see BytecodeGenerator and may be merge types somehow
    private fun FunctionTableRowStruct.readArgsTypes(): List<RuntimeType> {
        val count = argsCount.takeIf { it >= 0 } ?: throw java.lang.IllegalStateException("Arg count negative: $argsCount")
        val description = argsDescription
        return (0 until count).map { index ->
            val arg = (description shr (index*2)) and 0xffL
            when (arg) {
                0b01L -> RuntimeType.RuntimeByte
                0b10L -> RuntimeType.RuntimeInt
                else -> throw IllegalStateException("Unknown type 0b${arg.toString(2)} in description 0b${description.toString(2)}")
            }
        }
    }

    private fun checkFunctions(functions: List<RuntimeFunction>, bytecodeSize: Int): String? = functions
            .mapIndexed { index, f ->
                when {
                    f.address < 0 -> "Function #$index address is ${f.address}. Must be positive."
                    f.address >= bytecodeSize -> "Function #$index address is ${f.address}. Must be less than size of bytecode($bytecodeSize)."
                    else -> null
                }
            }
            .firstOrNull()

}