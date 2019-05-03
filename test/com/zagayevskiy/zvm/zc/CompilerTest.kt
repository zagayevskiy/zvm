package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.MemoryBitTable
import com.zagayevskiy.zvm.vm.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

internal data class CompilerTestData(val name: String, val text: String, val expectedResult: StackEntry, val runArgs: List<StackEntry>, val heapSize: Int)

private fun test(name: String, text: String, runArgs: List<StackEntry>, expectedResult: StackEntry, heapSize: Int = 0) = CompilerTestData(name, text, expectedResult, runArgs, heapSize)

internal fun entries(vararg values: Int) = values.map { it.toStackEntry() }
private fun bytes(vararg  values: Byte) = values.map { it.toStackEntry() }

private val True = 1.toByte().toStackEntry()
private val False = 0.toByte().toStackEntry()

private fun arraySum(size: Int, multiplier: Int) = (0 until size).map { it * multiplier }.reduce { acc, i -> acc + i }

private fun testArraySumOverAsmInsert(size: Int, multiplier: Int) = test(
        "array($size, $multiplier)",
        zcSumArrayOverAsmInsert,
        entries(size, multiplier),
        arraySum(size, multiplier).toStackEntry(),
        heapSize = size * 4 + 4)

@RunWith(Parameterized::class)
internal class CompilerTest(private val test: CompilerTestData) {
    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = listOf(
                test("+", simpleBinaryInt("+"), entries(10, 20), (10 + 20).toStackEntry()),
                test("-", simpleBinaryInt("-"), entries(123, 456), (123 - 456).toStackEntry()),
                test("*", simpleBinaryInt("*"), entries(1234, 4321), (1234 * 4321).toStackEntry()),
                test("/", simpleBinaryInt("/"), entries(1000, 50), (1000 / 50).toStackEntry()),
                test("%", simpleBinaryInt("%"), entries(1000, 50), (1000 % 50).toStackEntry()),
                test(">>", simpleBinaryInt(">>"), entries(10203040, 11), (10203040 shr 11).toStackEntry()),
                test("<<", simpleBinaryInt("<<"), entries(17181929, 19), (17181929 shl 19).toStackEntry()),
                test("stack", stackTest, emptyList(), 0.toStackEntry(), heapSize = 1024),
                testArraySumOverAsmInsert(10, 123),
                testArraySumOverAsmInsert(100, 123),
                testArraySumOverAsmInsert(1000, 213456),
                test("f(-100)", zcFibonacciIterative, entries(-100), 1.toStackEntry()),
                test("f(1)", zcFibonacciIterative, entries(1), 1.toStackEntry()),
                test("f(5)", zcFibonacciIterative, entries(5), 5.toStackEntry()),
                test("f(9)", zcFibonacciIterative, entries(9), 34.toStackEntry()),
                test("f(12)", zcFibonacciIterative, entries(12), 144.toStackEntry()),
                test("f(33)", zcFibonacciIterative, entries(33), 3524578.toStackEntry()),
                test("f(41)", zcFibonacciIterative, entries(41), 165580141.toStackEntry()),

                test("f_asm(-100)", zcFibonacciIterativeByAsmInsert, entries(-100), 1.toStackEntry()),
                test("f_asm(1)", zcFibonacciIterativeByAsmInsert, entries(1), 1.toStackEntry()),
                test("f_asm(5)", zcFibonacciIterativeByAsmInsert, entries(5), 5.toStackEntry()),
                test("f_asm(9)", zcFibonacciIterativeByAsmInsert, entries(9), 34.toStackEntry()),
                test("f_asm(12)", zcFibonacciIterativeByAsmInsert, entries(12), 144.toStackEntry()),
                test("f_asm(33)", zcFibonacciIterativeByAsmInsert, entries(33), 3524578.toStackEntry()),
                test("f_asm(41)", zcFibonacciIterativeByAsmInsert, entries(41), 165580141.toStackEntry()),

//                test("f_rec(-100)", zcFibonacciRecursive, entries(-100), 1.toStackEntry()),
//                test("f_rec(1)", zcFibonacciRecursive, entries(1), 1.toStackEntry()),
//                test("f_rec(5)", zcFibonacciRecursive, entries(5), 5.toStackEntry()),
//                test("f_rec(9)", zcFibonacciRecursive, entries(9), 34.toStackEntry()),
//                test("f_rec(12)", zcFibonacciRecursive, entries(12), 144.toStackEntry()),
//                test("f_rec(33)", zcFibonacciRecursive, entries(33), 3524578.toStackEntry()),
//                test("f_rec(41)", zcFibonacciRecursive, entries(41), 165580141.toStackEntry()),

                test("!1", zcFactorialIterative, entries(1), 1.toStackEntry()),
                test("!2", zcFactorialIterative, entries(2), 2.toStackEntry()),
                test("!5", zcFactorialIterative, entries(5), 120.toStackEntry()),
                test("!12", zcFactorialIterative, entries(12), 479001600.toStackEntry()),

//                test("rec !1", zcFactorialRecursive, entries(1), 1.toStackEntry()),
//                test("rec !2", zcFactorialRecursive, entries(2), 2.toStackEntry()),
//                test("rec !5", zcFactorialRecursive, entries(5), 120.toStackEntry()),
//                test("rec !12", zcFactorialRecursive, entries(12), 479001600.toStackEntry()),

                test("rbit 0x0a1b2c3d", zcReverseIntBytesViaBitManipulations, entries(0x0a1b2c3d), 0x3d2c1b0a.toStackEntry()),
                test("rbit 0b01010101_11110000_00001111_00110011", zcReverseIntBytesViaBitManipulations, entries(0b01010101_11110000_00001111_00110011), 0b00110011_00001111_11110000_01010101.toStackEntry()),

                test("rev 0b01010101_11110000_00001111_11001100", zcReverseIntBits, entries(0b00110011_01100111_00001111_10101010), 0b01010101_11110000_11100110_11001100.toStackEntry()),

                test("isPrime(2)", zcIsPrime, entries(2), True),
                test("isPrime(3)", zcIsPrime, entries(3), True),
                test("isPrime(4)", zcIsPrime, entries(4), False),
                test("isPrime(5)", zcIsPrime, entries(5), True),
                test("isPrime(35)", zcIsPrime, entries(35), False),
                test("isPrime(104729)", zcIsPrime, entries(104729), True),
                test("isPrime(7919*7919)", zcIsPrime, entries(7919 * 7919), False),

                test("when(0)", whenTest, bytes(0), 0.toStackEntry()),
                test("when(1)", whenTest, bytes(1), 100.toStackEntry()),
                test("when(2)", whenTest, bytes(2), 200.toStackEntry()),
                test("when(5)", whenTest, bytes(5), 500.toStackEntry()),
                test("when(-6)", whenTest, bytes(-6), (-600).toStackEntry()),
                test("when(10)", whenTest, bytes(10), 1000.toStackEntry()),
                test("when(111)", whenTest, bytes(111), (111*123).toStackEntry()),
                test("when(-27)", whenTest, bytes(-27), (-27*123).toStackEntry())
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
        val vm = VirtualMachine(info, MemoryBitTable(test.heapSize))
        val actualResult = vm.run(test.runArgs)

        assertEquals(test.expectedResult, actualResult)
    }

}