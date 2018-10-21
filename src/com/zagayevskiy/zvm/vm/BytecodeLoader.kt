package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.util.extensions.copyTo
import com.zagayevskiy.zvm.util.extensions.copyToInt

sealed class LoadingResult {
    class Success(val info: LoadedInfo) : LoadingResult()
    class Failure(val message: String) : LoadingResult()
}

class LoadedInfo(val functions: List<RuntimeFunction>, val mainIndex: Int, val bytecode: ByteArray)

class BytecodeLoader(private val rawBytecode: ByteArray) {

    private fun load(): LoadingResult {
        val serviceInfoSize = 8 //FIXME hardcode
        val functionRowSize = 12 //FIXME hardcode
        val rawBytecodeSize = rawBytecode.size

        if (rawBytecodeSize < serviceInfoSize + 1) return LoadingResult.Failure("Bytecode too small: ${rawBytecode.size} bytes")
        val mainIndex = rawBytecode.copyToInt(0)
        val functionsCount = rawBytecode.copyToInt(4)

        if (functionsCount <= 0) return LoadingResult.Failure("Functions count must be positive. Has $functionsCount.")
        if (mainIndex < 0 || mainIndex >= functionsCount) return LoadingResult.Failure("Invalid main index ($mainIndex). Has $functionsCount} functions")

        val minSizeWithFuncs = serviceInfoSize + functionsCount * functionRowSize + 1
        if (rawBytecodeSize < minSizeWithFuncs) return LoadingResult.Failure("Bytecode too small: ${rawBytecode.size} bytes. Must be at least $minSizeWithFuncs bytes. Declared $functionsCount functions.")

        @Suppress("UnnecessaryVariable")
        val functionTableStart = serviceInfoSize


        val functions = (0 until functionsCount).map { index ->
            RuntimeFunction(
                    address = rawBytecode.copyToInt(functionTableStart + index * functionRowSize),
                    args = rawBytecode.copyToInt(functionTableStart + index * functionRowSize + 4),
                    locals = rawBytecode.copyToInt(functionTableStart + index * functionRowSize + 8))
        }

        val bytecodeStart = functionTableStart + functionsCount * functionRowSize
        val bytecodeSize = rawBytecodeSize - bytecodeStart

        val checked = checkFunctions(functions, bytecodeSize)
        if (checked != null) return LoadingResult.Failure(checked)

        val bytecode = ByteArray(bytecodeSize)
        rawBytecode.copyTo(destination = bytecode, sourceIndex = bytecodeStart, count = bytecode.size)

        return LoadingResult.Success(LoadedInfo(functions, mainIndex, bytecode))
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