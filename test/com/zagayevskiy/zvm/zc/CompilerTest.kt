package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.vm.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

internal data class CompilerTestData(val text: String, val expectedResult: StackEntry, val runArgs: List<StackEntry>, val heapSize: Int)

private fun test(text: String, runArgs: List<StackEntry>, expectedResult: StackEntry, heapSize: Int = 0) = CompilerTestData(text, expectedResult, runArgs, heapSize)

private fun entries(vararg values: Int) = values.map { it.toStackEntry() }

/*
Tst("f(-100)", fibonacci, entries(-100), 1.toStackEntry()),
                Tst("f(1)", fibonacci, entries(1), 1.toStackEntry()),
                Tst("f(5)", fibonacci, entries(5), 5.toStackEntry()),
                Tst("f(9)", fibonacci, entries(9), 34.toStackEntry()),
                Tst("f(12)", fibonacci, entries(12), 144.toStackEntry()),
                Tst("f(33)", fibonacci, entries(33), 3524578.toStackEntry()),
                Tst("f(41)", fibonacci, entries(41), 165580141.toStackEntry()),
 */

@RunWith(Parameterized::class)
internal class CompilerTest(val test: CompilerTestData) {
    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = listOf(
            test(zcFibonacchiByLoop, entries(5), 5.toStackEntry())
        )

    }

    private lateinit var compiler: ZcCompiler

    @Before
    fun setup() {
        compiler = ZcCompiler()

    }

    @Test
    fun test() {
        val bytecode = compiler.compile(test.text)
        val loader = BytecodeLoader(bytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val vm = VirtualMachine(info, test.heapSize)
        val actualResult = vm.run(test.runArgs)

        assertEquals(test.expectedResult, actualResult)
    }

}