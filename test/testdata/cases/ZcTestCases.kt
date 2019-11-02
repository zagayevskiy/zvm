package testdata.cases

import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.vm.*
import com.zagayevskiy.zvm.zc.ZcCompiler
import testdata.sources.zc.*
import testdata.sources.zc.lisp.testsrc.AutoMemoryTestSource
import testdata.sources.zc.lisp.testsrc.LispObjectTestSource
import testdata.sources.zc.lisp.testsrc.RbTreeTestSource

internal object ZcTestCases : MutableList<VmTestCase> by mutableListOf() {
    val Sources = mutableListOf<TestSource>()


    init {

        source(RbTreeTestSource.MakeNode) {
            run(emptyList(), 0)
        }

        source(RbTreeTestSource.IsBstCrashedOnNoBst) {
            run(emptyList(), prints = listOf("value less then min"), crashCode = 1)
        }

        source(RbTreeTestSource.AllPuttedKeysExistsAndValuesCorrect) {
            run(emptyList(), 0)
        }

        source((RbTreeTestSource.PutGet)) {
            run(emptyList(), 0)
        }

        source(RbTreeTestSource.RedBlackRequirements) {
            run(emptyList(), 0)
        }

        source(AutoMemoryTestSource.Test) {
            run(emptyList(), 0)
        }

        source(LispObjectTestSource.Test) {
            run(emptyList(), 0)
        }

        source(Print.HelloStringLiteral) {
            run(args = emptyList(), prints = Print.HelloStrings)
        }

        source(TestSource("invoke", """

            fn main(i: int): int {
                val doit = ::apply;
                return doit(::inc, i);
            }

            fn apply(f: (int) -> int, arg: int): int {
                return f(arg);
            }

            fn inc(i: int): int {
                return i + 1;
            }

        """.trimIndent())) {
            run(arg = -1, ret = 0)
            run(arg = 100, ret = 101)
            run(arg = Int.MIN_VALUE, ret = Int.MIN_VALUE + 1)
        }

        source(TestSource("When", """
            fn main(code: byte): int {
                when(code) {
                    0 -> return 1;
                    1 -> return 1;
                    2 -> return 2;
                    3 -> return 6;
                    4 -> return 24;
                    else -> return 0;
                }
            }
        """.trimIndent())) {
            run(args = entries(0.toByte()), ret = 1)
            run(args = entries(1.toByte()), ret = 1)
            run(args = entries(2.toByte()), ret = 2)
            run(args = entries(3.toByte()), ret = 6)
            run(args = entries(4.toByte()), ret = 24)
            run(args = entries(5.toByte()), ret = 0)
            run(args = entries(112.toByte()), ret = 0)
            run(args = entries((-59).toByte()), ret = 0)
        }

        source(stackTest) {
            run(args = emptyList(), ret = 0)
        }

        source(ZcFactorial.Recursive) {
            run(arg = 1, ret = 1)
            run(arg = 2, ret = 2)
            run(arg = 5, ret = 120)
            run(arg = 12, ret = 479001600)
        }

        source(ZcFactorial.Iterative) {
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

        source(ZcFibonacci.Iterative.ForLoop) {
            run(arg = 1, ret = 1)
            run(arg = 5, ret = 5)
            run(arg = 9, ret = 34)
            run(arg = 12, ret = 144)
            run(arg = 33, ret = 3524578)
            run(arg = 41, ret = 165580141)
        }

        source(ZcReverseInt.Bytes) {
            run(arg = 0x0a1b2c3d, ret = 0x3d2c1b0a)
            run(arg = 0x3d2c1b0a, ret = 0x0a1b2c3d)
            run(arg = 0b01010101_11110000_00001111_00110011, ret = 0b00110011_00001111_11110000_01010101)
            run(arg = 0b00110011_00001111_11110000_01010101, ret = 0b01010101_11110000_00001111_00110011)
        }

        source((ZcReverseInt.Bits)) {
            run(arg = 0b01010101_11110000_11100110_11001100, ret = 0b00110011_01100111_00001111_10101010)
            run(arg = 0b00110011_01100111_00001111_10101010, ret = 0b01010101_11110000_11100110_11001100)
        }
    }
}


private class ZcRunBuilder(val source: TestSource) {
    private val precompiledProgram: ByteArray

    init {
        val compiler = ZcCompiler()
        precompiledProgram = compiler.compile(source.text)
    }

    fun run(arg: Int, ret: Int) = run(args = entries(arg), ret = ret.toStackEntry())

    fun run(args: List<StackEntry>, ret: Int) = run(args, ret.toStackEntry())

    fun run(args: List<StackEntry>, ret: StackEntry) {
        ZcTestCases.add(SimpleVmTestCase(
                """Zc ${source.name} ${(args.map { it.toString() }.takeIf { it.isNotEmpty() } ?: "")} -> $ret"""",
                precompiledProgram,
                runArgs = args,
                expectedResult = ret
        ))
    }

    fun run(args: List<StackEntry>, prints: List<String>, crashCode: Int? = null) {
        ZcTestCases.add(PrintVmTestCase(
                """Zc print ${source.name} ${(args.map { it.toString() }.takeIf { it.isNotEmpty() } ?: "")}"""",
                precompiledProgram,
                runArgs = args,
                expectPrinted = prints,
                expectCrashCode = crashCode
        ))
    }
}

private fun source(source: TestSource, block: ZcRunBuilder.() -> Unit) {
    ZcTestCases.Sources.add(source)
    ZcRunBuilder(source).block()
}
