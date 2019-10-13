package testsrc

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.vm.*
import testsrc.asm.AsmFactorial
import testsrc.asm.AsmFibonacci
import testsrc.asm.AsmReverse
import testsrc.asm.AsmSimple.JustRet0
import testsrc.asm.AsmSimple.JustRetArg
import testsrc.asm.AsmSimple.Sum


object AsmTestCases : MutableList<AsmSourceTestCase> by mutableListOf() {
    init {
        source(JustRet0) {
            run(args = No, ret = 0)
        }

        source(JustRetArg) {
            run(arg = 100500, ret = 100500)
        }

        source(Sum) {
            run(args = entries(0, 0), ret = 0)
            run(args = entries(1, 2), ret = 3)
            run(args = entries(10000, -10000), ret = 0)
            run(args = entries(Int.MIN_VALUE, Int.MAX_VALUE), ret = Int.MIN_VALUE + Int.MAX_VALUE)
        }

        source(AsmFactorial.Recursive) {
            run(arg = 1, ret = 1)
            run(arg = 2, ret = 2)
            run(arg = 5, ret = 120)
            run(arg = 12, ret = 479001600)
        }

        source(AsmFactorial.Iterative) {
            run(arg = 1, ret= 1)
            run(arg = 2, ret= 2)
            run(arg = 5, ret= 120)
            run(arg = 12, ret= 479001600)
        }

        source(AsmFibonacci.Recursive) {
            run(arg = 1, ret = 1)
            run(arg = 5, ret = 5)
            run(arg = 9, ret = 34)
            run(arg = 12, ret = 144)
            run(arg = 33, ret = 3524578)
            run(arg = 41, ret = 165580141)
        }

        source(AsmFibonacci.Iterative) {
            run(arg = -100, ret = 1)
            run(arg = 1, ret = 1)
            run(arg = 5, ret = 5)
            run(arg = 9, ret = 34)
            run(arg = 12, ret = 144)
            run(arg = 33, ret = 3524578)
            run(arg = 41, ret = 165580141)
        }

        source(AsmReverse.IntBits) {
            run (arg = 0b01010101_11110000_11100110_11001100, ret = 0b00110011_01100111_00001111_10101010)
            run (arg = 0b00110011_01100111_00001111_10101010, ret = 0b01010101_11110000_11100110_11001100)
        }
    }


}


class AsmSourceTestCase(
        name: String,
        override val loadedProgram: LoadedInfo,
        override val runArgs: List<StackEntry>,
        override val expectedResult: StackEntry) : AbsVmTestCase(name)

private object No : List<StackEntry> by emptyList()

private class AsmRunBuilder(val source: Source) {
    private val precompiledProgram: LoadedInfo
    init {
        val parser = AsmParser(AsmSequenceLexer(source.text.asSequence()), OpcodesMapping.opcodes)
        val parsed = parser.program()
        val result = parsed as? ParseResult.Success ?: throw IllegalArgumentException("Failed to parse $parsed")
        val assembler = BytecodeAssembler(result.commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        val bytecode = generator.generate(assembler.generate())
        val loader = BytecodeLoader(bytecode)
        precompiledProgram = (loader.load() as LoadingResult.Success).info
    }

    fun run(arg: Int, ret: Int) {
        run(args = entries(arg), ret = ret)
    }

    fun run(args: List<StackEntry>, ret: Int) {
        AsmTestCases.add(AsmSourceTestCase(
                source.name + (args.map { (it as StackEntry.VMInteger).intValue }.takeIf { it.isNotEmpty() } ?: "") + " -> $ret",
                precompiledProgram,
                runArgs = args,
                expectedResult = ret.toStackEntry()
        ))

    }
}

private fun source(source: Source, block: AsmRunBuilder.() -> Unit) {
    AsmRunBuilder(source).block()
}