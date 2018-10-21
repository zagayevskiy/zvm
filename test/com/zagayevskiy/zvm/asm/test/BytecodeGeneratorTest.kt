package com.zagayevskiy.zvm.asm.test

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.util.extensions.copyToInt
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private data class TestRuntimeFunc(val address: Int, val args: Int, val locals: Int)

class BytecodeGeneratorTest {
    private val text = """
            .fun f: args = 11
            ret
            .fun main: args = 7, locals=3
            ret
            ret
            .fun g: args = 13
            ret
            ret
            ret
            .fun k: locals = 5
            ret
            ret
            .fun last
            ret
        """.trimIndent()

    private val expectedFuncs = listOf(
            TestRuntimeFunc(0, 11, 0),
            TestRuntimeFunc(1, 7, 3),
            TestRuntimeFunc(3, 13, 0),
            TestRuntimeFunc(6, 0, 5),
            TestRuntimeFunc(8, 0, 0)
    )

    private val retByteCode = 0x17.toByte()

    private lateinit var generator: BytecodeGenerator
    private lateinit var bytecode: ByteArray

    @Before
    fun setup() {
        val parser = AsmParser(AsmSequenceLexer(text.asSequence()), listOf(Ret)) //Accept only Ret instruction
        val result = parser.program()
        assertTrue(result.toString(), result is ParseResult.Success)

        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, mapOf(Ret to retByteCode))
        generator = BytecodeGenerator()
        bytecode = generator.generate(assembler.generate())
    }

    @Test
    fun generatedByteCodeSizeCorrect() {
        assertEquals(generator.serviceInfoSize + expectedFuncs.size * generator.functionsTableRowSize + 9, //9 Ret-s
                bytecode.size)
    }

    @Test
    fun mainIndexCorrect() {
        val mainIndex = bytecode.copyToInt(0)

        assertEquals(1, mainIndex)
    }

    @Test
    fun functionsTableGeneratedCorrect() {
        val functionsCount = bytecode.copyToInt(4)

        assertEquals(5, functionsCount)

        val actualFuncs = (0 until functionsCount).map { index ->
            val address = bytecode.copyToInt(generator.serviceInfoSize + index * generator.functionsTableRowSize)
            val args = bytecode.copyToInt(generator.serviceInfoSize + index * generator.functionsTableRowSize + 4)
            val locals = bytecode.copyToInt(generator.serviceInfoSize + index * generator.functionsTableRowSize + 8)
            TestRuntimeFunc(address, args, locals)
        }

        assertEquals(expectedFuncs, actualFuncs)
    }

    @Test
    fun bytecodeBodyGeneratedCorrect() {
        val bytecodeStart = generator.serviceInfoSize + generator.functionsTableRowSize * expectedFuncs.size

        (bytecodeStart until bytecodeStart + 9).forEach { ip ->
            assertEquals(retByteCode, bytecode[ip])
        }
    }
}