package com.zagayevskiy.zvm.asm.test

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.common.BackingStruct
import com.zagayevskiy.zvm.common.sizeOf
import com.zagayevskiy.zvm.util.extensions.copyToInt
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class TestRuntimeFunc(array: ByteArray, offset: Int) : BackingStruct(array, offset) {
    var address by int
    var argsCount by int
    var argsDescription by long

    constructor(addr: Int, count: Int, args: Long) : this(ByteArray(sizeOf(::TestRuntimeFunc)), 0) {
        address = addr
        argsCount = count
        argsDescription = args
    }
}

class BytecodeGeneratorTest {
    private val text = """
            .fun f
            ret
            .fun main: argc: int, argv: int;
            ret
            ret
            .fun g: x: int;
            ret
            ret
            ret
            .fun k: b1: byte, b2: byte, i: int;
            ret
            ret
            .fun last
            ret
        """.trimIndent()

    private val expectedFuncs = listOf(
            TestRuntimeFunc(0, 0, 0b0L),
            TestRuntimeFunc(1, 2, 0b1010L),
            TestRuntimeFunc(3, 1, 0b10L),
            TestRuntimeFunc(6, 3, 0b100101L),
            TestRuntimeFunc(8, 0, 0b0L)
    )

    private val retByteCode = 0x17.toByte()
    private val runtimeFunctionSize = sizeOf(::TestRuntimeFunc)

    private lateinit var generator: BytecodeGenerator
    private lateinit var bytecode: ByteArray

    @Before
    fun setup() {
        val parser = AsmParser(AsmSequenceLexer(text.asSequence()), listOf(Ret)) //Accept only ret instruction
        val result = parser.program()
        assertTrue(result.toString(), result is ParseResult.Success)

        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, mapOf(Ret to retByteCode))
        generator = BytecodeGenerator()
        bytecode = generator.generate(assembler.generate())
    }

    @Test
    fun generatedByteCodeSizeCorrect() {
        assertEquals(generator.serviceInfoSize + expectedFuncs.size * runtimeFunctionSize + 9, //9 ret-s
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
            TestRuntimeFunc(bytecode, generator.serviceInfoSize + index * runtimeFunctionSize)
        }

        assertEquals(expectedFuncs, actualFuncs)
    }

    @Test
    fun bytecodeBodyGeneratedCorrect() {
        val bytecodeStart = generator.serviceInfoSize + runtimeFunctionSize * expectedFuncs.size

        (bytecodeStart until bytecodeStart + 9).forEach { ip ->
            assertEquals(retByteCode, bytecode[ip])
        }
    }
}