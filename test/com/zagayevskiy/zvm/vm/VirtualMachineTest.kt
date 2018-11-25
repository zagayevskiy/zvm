package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.asm.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

internal data class Tst(val name: String, val asmText: String, val runArgs: List<StackEntry>, val expectedResult: StackEntry, val heapSize: Int = 0)

private fun entries(vararg values: Int): List<StackEntry> {
    return values.map { it.toStackEntry() }
}

@RunWith(Parameterized::class)
internal class VirtualMachineProgramsTest(private val test: Tst) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = listOf(
                Tst("0*0", asmMulByRecursiveAdd, entries(0, 0), 0.toStackEntry()),
                Tst("0*3", asmMulByRecursiveAdd, entries(0, 3), 0.toStackEntry()),
                Tst("11*0", asmMulByRecursiveAdd, entries(11, 0), 0.toStackEntry()),
                Tst("5*7", asmMulByRecursiveAdd, entries(5, 7), (5 * 7).toStackEntry()),
                Tst("17 * -29", asmMulByRecursiveAdd, entries(17, -29), (17 * -29).toStackEntry()),
                Tst("-17 * -29", asmMulByRecursiveAdd, entries(-17, -29), (-17 * -29).toStackEntry()),
                Tst("-17 * 9", asmMulByRecursiveAdd, entries(-17, 9), (-17 * 9).toStackEntry()),

                Tst("f(-100)", asmFibonacciIterative, entries(-100), 1.toStackEntry()),
                Tst("f(1)", asmFibonacciIterative, entries(1), 1.toStackEntry()),
                Tst("f(5)", asmFibonacciIterative, entries(5), 5.toStackEntry()),
                Tst("f(9)", asmFibonacciIterative, entries(9), 34.toStackEntry()),
                Tst("f(12)", asmFibonacciIterative, entries(12), 144.toStackEntry()),
                Tst("f(33)", asmFibonacciIterative, entries(33), 3524578.toStackEntry()),
                Tst("f(41)", asmFibonacciIterative, entries(41), 165580141.toStackEntry()),

                Tst("fr(1)", asmFibonacciRecursive, entries(1), 1.toStackEntry()),
                Tst("fr(5)", asmFibonacciRecursive, entries(5), 5.toStackEntry()),
                Tst("fr(9)", asmFibonacciRecursive, entries(9), 34.toStackEntry()),
                Tst("fr(12)", asmFibonacciRecursive, entries(12), 144.toStackEntry()),
                Tst("fr(33)", asmFibonacciRecursive, entries(33), 3524578.toStackEntry()),
                Tst("fr(41)", asmFibonacciRecursive, entries(41), 165580141.toStackEntry()),

                Tst("!1", asmFactorialIterative, entries(1), 1.toStackEntry()),
                Tst("!2", asmFactorialIterative, entries(2), 2.toStackEntry()),
                Tst("!5", asmFactorialIterative, entries(5), 120.toStackEntry()),
                Tst("!12", asmFactorialIterative, entries(12), 479001600.toStackEntry()),

                Tst("rec!1", asmFactorialRecursive, entries(1), 1.toStackEntry()),
                Tst("rec!2 ", asmFactorialRecursive, entries(2), 2.toStackEntry()),
                Tst("rec!5", asmFactorialRecursive, entries(5), 120.toStackEntry()),
                Tst("rec!12", asmFactorialRecursive, entries(12), 479001600.toStackEntry()),

                Tst("1+..+5", asmSumOfArray, entries(6), 15.toStackEntry(), 10),
                Tst("1+..+1000", asmSumOfArray, entries(1001), (1000 * (1 + 1000) / 2).toStackEntry(), 4000),

                Tst("rbit 0x0a1b2c3d", asmReverseIntBytesViaBitManipulations, entries(0x0a1b2c3d), 0x3d2c1b0a.toStackEntry()),
                Tst("rbit 0b01010101_11110000_00001111_00110011", asmReverseIntBytesViaBitManipulations, entries(0b01010101_11110000_00001111_00110011), 0b00110011_00001111_11110000_01010101.toStackEntry()),

                Tst("rmem 0x0a1b2c3d", asmReverseIntBytesViaMemory, entries(0x0a1b2c3d), 0x3d2c1b0a.toStackEntry(), 4),
                Tst("rmem 0b01010101_11110000_00001111_00110011", asmReverseIntBytesViaMemory, entries(0b01010101_11110000_00001111_00110011), 0b00110011_00001111_11110000_01010101.toStackEntry(), 4),

                Tst("rev 0b01010101_11110000_00001111_11001100", asmReverseIntBits, entries(0b01010101_11110000_11100110_11001100), 0b00110011_01100111_00001111_10101010.toStackEntry())
        )
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

        vm = VirtualMachine(loaded.info, test.heapSize)
    }

    @Test
    fun test() {
        val result = vm.run(test.runArgs)
        assertEquals(test.expectedResult, result)
    }
}
