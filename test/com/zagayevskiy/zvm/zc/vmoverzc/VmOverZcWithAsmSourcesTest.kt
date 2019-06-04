package com.zagayevskiy.zvm.zc.vmoverzc

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.asm.ParseResult
import com.zagayevskiy.zvm.vm.StackEntry
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testsrc.AsmTestData
import testsrc.asmTestData

@RunWith(Parameterized::class)
internal class VmOverZcWithAsmSourcesTest(private val test: AsmTestData): AbsVirtualMachineOverZtTest() {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = asmTestData
    }

    override fun createRawTestBytecode(): ByteArray {
        val parser = AsmParser(AsmSequenceLexer(test.asmText.asSequence()), OpcodesMapping.opcodes)
        val result = parser.program()
        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        return generator.generate(assembler.generate())
    }

    override val runArgs: List<StackEntry>
        get() = test.runArgs

    override val expectedResult: StackEntry
        get() = test.expectedResult
}