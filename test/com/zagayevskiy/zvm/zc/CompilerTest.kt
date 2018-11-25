package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.vm.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

internal data class CompilerTestData(val name: String, val text: String, val expectedResult: StackEntry, val runArgs: List<StackEntry>, val heapSize: Int)

private fun test(name: String, text: String, runArgs: List<StackEntry>, expectedResult: StackEntry, heapSize: Int = 0) = CompilerTestData(name, text, expectedResult, runArgs, heapSize)

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
                test("f(-100)", zcFibonacciIterative, entries(-100), 1.toStackEntry()),
                test("f(1)", zcFibonacciIterative, entries(1), 1.toStackEntry()),
                test("f(5)", zcFibonacciIterative, entries(5), 5.toStackEntry()),
                test("f(9)", zcFibonacciIterative, entries(9), 34.toStackEntry()),
                test("f(12)", zcFibonacciIterative, entries(12), 144.toStackEntry()),
                test("f(33)", zcFibonacciIterative, entries(33), 3524578.toStackEntry()),
                test("f(41)", zcFibonacciIterative, entries(41), 165580141.toStackEntry()),

                test("f_rec(-100)", zcFibonacciRecursive, entries(-100), 1.toStackEntry()),
                test("f_rec(1)", zcFibonacciRecursive, entries(1), 1.toStackEntry()),
                test("f_rec(5)", zcFibonacciRecursive, entries(5), 5.toStackEntry()),
                test("f_rec(9)", zcFibonacciRecursive, entries(9), 34.toStackEntry()),
                test("f_rec(12)", zcFibonacciRecursive, entries(12), 144.toStackEntry()),
                test("f_rec(33)", zcFibonacciRecursive, entries(33), 3524578.toStackEntry()),
                test("f_rec(41)", zcFibonacciRecursive, entries(41), 165580141.toStackEntry()),

                test("!1", zcFactorialIterative, entries(1), 1.toStackEntry()),
                test("!2", zcFactorialIterative, entries(2), 2.toStackEntry()),
                test("!5", zcFactorialIterative, entries(5), 120.toStackEntry()),
                test("!12", zcFactorialIterative, entries(12), 479001600.toStackEntry()),

                test("rec !1", zcFactorialRecursive, entries(1), 1.toStackEntry()),
                test("rec !2", zcFactorialRecursive, entries(2), 2.toStackEntry()),
                test("rec !5", zcFactorialRecursive, entries(5), 120.toStackEntry()),
                test("rec !12", zcFactorialRecursive, entries(12), 479001600.toStackEntry())
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