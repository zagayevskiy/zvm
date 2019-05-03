package com.zagayevskiy.zvm.zc.vmoverzc

import com.zagayevskiy.zvm.MemoryBitTable
import com.zagayevskiy.zvm.common.BackingStruct
import com.zagayevskiy.zvm.common.sizeOf
import com.zagayevskiy.zvm.vm.*
import com.zagayevskiy.zvm.zc.CompilerTest
import com.zagayevskiy.zvm.zc.CompilerTestData
import com.zagayevskiy.zvm.zc.ZcCompiler
import com.zagayevskiy.zvm.zc.includes.includeStdMem
import com.zagayevskiy.zvm.zc.vmoverzc.src.includeBytecodeParser
import com.zagayevskiy.zvm.zc.vmoverzc.src.vmOverZc
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class VirtualMachineOverZcTest(private val test: CompilerTestData) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = CompilerTest.data()
    }

    private lateinit var compiler: ZcCompiler
    private lateinit var rawTestBytecode: ByteArray
    private lateinit var heap: MemoryBitTable
    private var testProgramStartAddress: Int = 0
    private var testProgramSize: Int = 0
    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        compiler = ZcCompiler()
        rawTestBytecode = compiler.compile(test.text)
        heap = MemoryBitTable(1024 * 1024)
        testProgramStartAddress = heap.allocate(rawTestBytecode.size)
        testProgramSize = rawTestBytecode.size
        heap.copyIn(rawTestBytecode, destination = testProgramStartAddress)
        compiler = ZcCompiler()
    }

    @Test
    fun testVmOverZc() {
        val rawVirtualMachineBytecode = compiler.compile(vmOverZc)
        val loader = BytecodeLoader(rawVirtualMachineBytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val vm = VirtualMachine(info, heap)

        val mainArgsArray = heap.allocate(test.runArgs.size*4)
        test.runArgs.withIndex().forEach { (index, value) ->
            heap.writeInt(mainArgsArray + index*4, when(value) {
                is StackEntry.VMInteger -> value.intValue
                is StackEntry.VMByte -> value.byteValue.toInt()
                else -> error("impossible")
            })
        }

        val actualResult = vm.run(entries(testProgramStartAddress, testProgramSize, mainArgsArray, test.runArgs.size))

        assertEquals(test.expectedResult, actualResult)
    }

    @Test
    fun testBytecodeParsing() {
        val rawParsingBytecode = compiler.compile("""

            ${includeStdMem()}
            ${includeBytecodeParser()}

            fn main(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
                return parseBytecode(rawBytecode, rawBytecodeSize);
            }

        """.trimIndent())
        val loader = BytecodeLoader(rawParsingBytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val vm = VirtualMachine(info, heap)

        val parsedResultAddress = (vm.run(listOf(testProgramStartAddress.toStackEntry(), testProgramSize.toStackEntry())) as StackEntry.VMInteger).intValue

        val expected = (BytecodeLoader(rawTestBytecode).load() as LoadingResult.Success).info

        val readableHeap = ByteArray(heap.size)
        heap.copyOut(source = 0, destination = readableHeap)

        val parsedProgramInfo = ProgramInfoStruct(readableHeap, parsedResultAddress)
        val parsedServiceInfo = ServiceInfoStruct(readableHeap, offset = parsedProgramInfo.serviceInfoPointer)

        assertEquals(expected.mainIndex, parsedServiceInfo.mainIndex)
        assertEquals(expected.globalsCount, parsedServiceInfo.globalsCount)
        assertEquals(expected.functions.size, parsedServiceInfo.functionsCount)

        val functionsTableAddress = parsedProgramInfo.functionsTablePointer
        expected.functions.forEachIndexed { index, expectedFunction ->
            val functionAddress = heap.readInt(functionsTableAddress + 4 * index)
            val parsedFunction = FunctionTableRowStruct(readableHeap, offset = functionAddress)

            assertEquals(expectedFunction.address, parsedFunction.address)
            assertEquals(expectedFunction.args, parsedFunction.argsCount)
            assertEquals(expectedFunction.locals, parsedFunction.localsCount)
        }

        val expectedBytecodeStart = testProgramStartAddress + sizeOf(::ServiceInfoStruct) + sizeOf(::FunctionTableRowStruct) * expected.functions.size

        assertEquals(expectedBytecodeStart, parsedProgramInfo.bytecodeAddress)
        assertEquals(expected.bytecode.size, parsedProgramInfo.bytecodeSize)
    }


    //Same as struct ProgramInfo
    private class ProgramInfoStruct(array: ByteArray, offset: Int) : BackingStruct(array, offset) {
        var serviceInfoPointer by int
        var functionsTablePointer by int
        var bytecodeAddress by int
        var bytecodeSize by int
    }

}


class StackTest {

}