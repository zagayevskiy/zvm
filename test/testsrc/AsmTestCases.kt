package testsrc

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.vm.*
import testsrc.asm.AsmFactorial
import testsrc.asm.AsmFibonacci
import testsrc.asm.AsmSimple.JustRet0
import testsrc.asm.AsmSimple.JustRetArg
import testsrc.asm.AsmSimple.Sum


object AsmTestCases : MutableList<AsmSourceTestCase> by mutableListOf() {
    init {
        source(JustRet0) {
            run(args = No, ret = 0)
        }

        source(JustRetArg) {
            run(args = entries(100500), ret = 100500)
        }

        source(Sum) {
            run(args = entries(0, 0), ret = 0)
            run(args = entries(1, 2), ret = 3)
            run(args = entries(10000, -10000), ret = 0)
            run(args = entries(Int.MIN_VALUE, Int.MAX_VALUE), ret = Int.MIN_VALUE + Int.MAX_VALUE)
        }

        source(AsmFactorial.Recursive) {
            run(args = entries(1), ret = 1)
            run(args = entries(2), ret = 2)
            run(args = entries(5), ret = 120)
            run(args = entries(12), ret = 479001600)
        }

        source(AsmFibonacci.Recursive) {
            run(args = entries(1), ret = 1)
            run(args = entries(5), ret = 5)
            run(args = entries(9), ret = 34)
            run(args = entries(12), ret = 144)
            run(args = entries(33), ret = 3524578)
            run(args = entries(41), ret = 165580141)
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
        val result = parser.program() as ParseResult.Success
        val assembler = BytecodeAssembler(result.commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        val bytecode = generator.generate(assembler.generate())
        val loader = BytecodeLoader(bytecode)
        precompiledProgram = (loader.load() as LoadingResult.Success).info
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