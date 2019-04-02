package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.util.extensions.copyTo
import com.zagayevskiy.zvm.util.extensions.copyToByteArray

class BytecodeGenerator {

    val serviceInfoSize = 12 //FIXME hardcode
    val functionsTableRowSize = 4 * 3 //FIXME hardcode

    fun generate(info: GenerationInfo): ByteArray {

        val globalsCount = info.globalsCount
        val functions = info.functions
        val bytecode = info.bytecode

        val result = ByteArray(serviceInfoSize + functionsTableRowSize * functions.size + bytecode.size)

        val main = functions.findMain() ?: error("main function not found")

        var currentIndex = 0
        main.index.copyToByteArray(result, currentIndex)
        currentIndex += 4
        functions.size.copyToByteArray(result, currentIndex)
        currentIndex += 4
        globalsCount.copyToByteArray(result, currentIndex)
        currentIndex += 4

        val functionsTableStart = currentIndex
        functions.forEachIndexed { index, func ->
            func.address.copyToByteArray(result, functionsTableStart + index * functionsTableRowSize)
            func.args.copyToByteArray(result, functionsTableStart + index * functionsTableRowSize + 4)
            func.locals.copyToByteArray(result, functionsTableStart + index * functionsTableRowSize + 8)
        }

        val bytecodeStart = functionsTableStart + functionsTableRowSize * functions.size

        bytecode.copyTo(destination = result, destIndex = bytecodeStart)

        return result
    }

    private fun error(message: String): Nothing = throw RuntimeException(message)

    private fun List<FunctionDefinition>.findMain() = firstOrNull { it.name == "main" }
}