package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.memory.BitTableMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testsrc.AsmTestData
import testsrc.asmTestData

@RunWith(Parameterized::class)
internal class VirtualMachineProgramsTest(private val test: AsmTestData) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = asmTestData
    }

    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        val parser = AsmParser(AsmSequenceLexer(test.asmText.asSequence()), OpcodesMapping.opcodes)
        val result = parser.program()
        assertTrue(result.toString(), result is ParseResult.Success)
        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        val bytecode = generator.generate(assembler.generate())

        val loader = BytecodeLoader(bytecode)
        val loaded = loader.load() as LoadingResult.Success

        vm = VirtualMachine(loaded.info, heap = BitTableMemory(test.heapSize))
    }

    @Test
    fun test() {
        val result = vm.run(test.runArgs)
        assertEquals(test.expectedResult, result)
    }
}
