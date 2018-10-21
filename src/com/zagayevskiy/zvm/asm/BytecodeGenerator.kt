package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.util.extensions.copyTo
import com.zagayevskiy.zvm.util.extensions.copyToByteArray

class BytecodeGenerator {

    val serviceInfoSize = 8
    val functionsTableRowSize = 4 * 3

    fun generate(info: GenerationInfo): ByteArray {

        val (functions, bytecode) = info

        val result = ByteArray(serviceInfoSize + functionsTableRowSize * functions.size + bytecode.size)

        val main = functions.findMain() ?: error("main function not found")
        main.index.copyToByteArray(result, 0)
        functions.size.copyToByteArray(result, 4)
        val functionsTableStart = 8
        functions.forEachIndexed { index, func ->
            func.address.copyToByteArray(result, functionsTableStart + index * functionsTableRowSize)
            func.args.copyToByteArray(result, functionsTableStart + index * functionsTableRowSize + 4)
            func.locals.copyToByteArray(result, functionsTableStart + index * functionsTableRowSize + 8)
        }

        val bytecodeStart = functionsTableStart + functionsTableRowSize * functions.size

        bytecode.copyTo(result, bytecodeStart)

        return result
    }

    private fun error(message: String): Nothing = throw RuntimeException(message)

    private fun List<FunctionDefinition>.findMain() = firstOrNull { it.name == "main" }
}