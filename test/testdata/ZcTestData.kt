package testdata

import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.vm.toStackEntry
import testdata.sources.zc.*

internal data class ZcTestData(val name: String, val text: String, val expectedResult: StackEntry, val runArgs: List<StackEntry>, val heapSize: Int)

private fun test(name: String, text: String, runArgs: List<StackEntry>, expectedResult: StackEntry, heapSize: Int = 2048) = ZcTestData(name, text, expectedResult, runArgs, heapSize)

private fun bytes(vararg values: Byte) = values.map { it.toStackEntry() }

private val True = 1.toByte().toStackEntry()
private val False = 0.toByte().toStackEntry()

private fun arraySum(size: Int, multiplier: Int) = (0 until size).map { it * multiplier }.reduce { acc, i -> acc + i }

private fun testArraySumOverAsmInsert(size: Int, multiplier: Int) = test(
        "array($size, $multiplier)",
        zcSumArrayOverAsmInsert,
        entries(size, multiplier),
        arraySum(size, multiplier).toStackEntry(),
        heapSize = size * 4 + 4)

internal val zcTestData = listOf(
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

        test("!1", zcFactorialIterative, entries(1), 1.toStackEntry()),
        test("!2", zcFactorialIterative, entries(2), 2.toStackEntry()),
        test("!5", zcFactorialIterative, entries(5), 120.toStackEntry()),
        test("!12", zcFactorialIterative, entries(12), 479001600.toStackEntry()),

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
        test("when(111)", whenTest, bytes(111), (111 * 123).toStackEntry()),
        test("when(-27)", whenTest, bytes(-27), (-27 * 123).toStackEntry())
)