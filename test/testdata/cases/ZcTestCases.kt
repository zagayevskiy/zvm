package testdata.cases

import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.vm.*
import com.zagayevskiy.zvm.zc.ZcCompiler
import testdata.sources.zc.ZcFactorial
import testdata.sources.zc.ZcFibonacci
import testdata.sources.zc.ZcReverseInt

internal object ZcTestCases: MutableList<VmTestCase> by mutableListOf() {
    val Sources = mutableListOf<TestSource>()

    init {

        source(ZcFactorial.Recursive) {
            run(arg = 1, ret = 1)
            run(arg = 2, ret = 2)
            run(arg = 5, ret = 120)
            run(arg = 12, ret = 479001600)
        }

        source(ZcFibonacci.Recursive) {
            run(arg = 1, ret = 1)
            run(arg = 5, ret = 5)
            run(arg = 9, ret = 34)
            run(arg = 12, ret = 144)
            run(arg = 33, ret = 3524578)
            run(arg = 41, ret = 165580141)
        }

        source(ZcFibonacci.Iterative.AsmInsertImpl) {
            run(arg = 1, ret = 1)
            run(arg = 5, ret = 5)
            run(arg = 9, ret = 34)
            run(arg = 12, ret = 144)
            run(arg = 33, ret = 3524578)
            run(arg = 41, ret = 165580141)
        }

        source(ZcReverseInt.BytesViaBitOps) {
            run(arg = 0x0a1b2c3d, ret = 0x3d2c1b0a)
            run(arg = 0x3d2c1b0a, ret = 0x0a1b2c3d)
            run(arg = 0b01010101_11110000_00001111_00110011, ret = 0b00110011_00001111_11110000_01010101)
            run(arg = 0b00110011_00001111_11110000_01010101, ret = 0b01010101_11110000_00001111_00110011)
        }
    }
}


private class ZcRunBuilder(val source: TestSource) {
    private val precompiledProgram: LoadedInfo

    init {
        val compiler = ZcCompiler()
        val bytecode = compiler.compile(source.text)
        val loader = BytecodeLoader(bytecode)
        precompiledProgram = (loader.load() as LoadingResult.Success).info
    }

    fun run(arg: Int, ret: Int) = run(args = entries(arg), ret = ret.toStackEntry())

    fun run(args: List<StackEntry>, ret: Int) = run(args, ret.toStackEntry())

    fun run(args: List<StackEntry>, ret: StackEntry) {
        ZcTestCases.add(SimpleVmTestCase(
                """Zc ${source.name} ${(args.map { it.toString() }.takeIf { it.isNotEmpty() } ?: "") } -> $ret"""",
                precompiledProgram,
                runArgs = args,
                expectedResult = ret
        ))
    }
}

private fun source(source: TestSource, block: ZcRunBuilder.() -> Unit) {
    ZcTestCases.Sources.add(source)
    ZcRunBuilder(source).block()
}
