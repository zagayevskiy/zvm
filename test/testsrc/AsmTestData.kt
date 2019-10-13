package testsrc

import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.vm.toStackEntry
import testsrc.asm.*
import java.lang.IllegalArgumentException

internal data class AsmTestData(val name: String, val asmText: String, val runArgs: List<StackEntry>, val expectedResult: StackEntry, val heapSize: Int)

private fun test(name: String, asmText: String, runArgs: List<StackEntry>, expectedResult: Any, heapSize: Int = 0) = 
        AsmTestData(name, asmText, runArgs, when(expectedResult) {
            is Int -> expectedResult.toStackEntry()
            is Byte ->expectedResult.toStackEntry()
            else -> throw IllegalArgumentException("unknown $expectedResult")
        }, heapSize)

internal val asmTestData = listOf(
        test("0*0", asmMulByRecursiveAdd, entries(0, 0), 0),
        test("0*3", asmMulByRecursiveAdd, entries(0, 3), 0),
        test("11*0", asmMulByRecursiveAdd, entries(11, 0), 0),
        test("5*7", asmMulByRecursiveAdd, entries(5, 7), 5 * 7),
        test("17 * -29", asmMulByRecursiveAdd, entries(17, -29), 17 * -29),
        test("-17 * -29", asmMulByRecursiveAdd, entries(-17, -29), -17 * -29),
        test("-17 * 9", asmMulByRecursiveAdd, entries(-17, 9), -17 * 9),

        test("1+..+5", asmSumOfArray, entries(6), 15, heapSize = 10),
        test("1+..+1000", asmSumOfArray, entries(1001), 1000 * (1 + 1000) / 2, heapSize = 4000),

        test("rbit 0x0a1b2c3d", asmReverseIntBytesViaBitManipulations, entries(0x0a1b2c3d), 0x3d2c1b0a),
        test("rbit 0b01010101_11110000_00001111_00110011", asmReverseIntBytesViaBitManipulations, entries(0b01010101_11110000_00001111_00110011), 0b00110011_00001111_11110000_01010101),

        test("rmem 0x0a1b2c3d", asmReverseIntBytesViaMemoryManipulations, entries(0x0a1b2c3d), 0x3d2c1b0a, heapSize = 4),
        test("rmem 0b01010101_11110000_00001111_00110011", asmReverseIntBytesViaMemoryManipulations, entries(0b01010101_11110000_00001111_00110011), 0b00110011_00001111_11110000_01010101, heapSize = 4),

        test("globals", countersInGlobals, emptyList(), ((1 + 1 * 3) * (1 + 2 * 3)))
)