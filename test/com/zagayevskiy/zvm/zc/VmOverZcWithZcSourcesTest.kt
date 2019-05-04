package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.asm.ParseResult
import com.zagayevskiy.zvm.common.BackingStruct
import com.zagayevskiy.zvm.common.sizeOf
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.vm.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testsrc.AsmTestData
import testsrc.ZcTestData
import testsrc.asmTestData
import testsrc.zc.includes.includeStdMem
import testsrc.zc.vmoverzc.includeBytecodeParser
import testsrc.zc.vmoverzc.vmOverZc
import testsrc.zcTestData
import kotlin.test.assertEquals

private fun compile(text: String): ByteArray {
    val compiler = ZcCompiler()
    return compiler.compile(text)
}

@RunWith(Parameterized::class)
internal class VmOverZcWithAsmSourcesTest(private val test: AsmTestData): AbsVirtualMachineOverZtTest() {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = asmTestData
    }

    override fun createRawTestBytecode(): ByteArray {
        val parser = AsmParser(AsmSequenceLexer(test.asmText.asSequence()), OpcodesMapping.opcodes)
        val result = parser.program()
        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        return generator.generate(assembler.generate())
    }

    override val runArgs: List<StackEntry>
        get() = test.runArgs

    override val expectedResult: StackEntry
        get() = test.expectedResult
}

@RunWith(Parameterized::class)
internal class VmOverZcWithZcSourcesTest(private val test: ZcTestData): AbsVirtualMachineOverZtTest() {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = zcTestData
    }

    override fun createRawTestBytecode() = compile(test.text)

    override val runArgs: List<StackEntry>
        get() = test.runArgs

    override val expectedResult: StackEntry
        get() = test.expectedResult
}

internal abstract class AbsVirtualMachineOverZtTest {

    abstract fun createRawTestBytecode(): ByteArray
    abstract val runArgs: List<StackEntry>
    abstract val expectedResult: StackEntry

    private var testProgramStartAddress: Int = 0

    private lateinit var testProgramRawBytecode: ByteArray

    private lateinit var heap: BitTableMemory

    private lateinit var vm: VirtualMachine

    @Before
    open fun setup() {
        heap = BitTableMemory(1024 * 1024)
        testProgramRawBytecode = createRawTestBytecode()

        testProgramStartAddress = heap.allocate(testProgramRawBytecode.size)
        heap.copyIn(testProgramRawBytecode, destination = testProgramStartAddress)

        val rawVirtualMachineBytecode = compile(vmOverZc)

        val loader = BytecodeLoader(rawVirtualMachineBytecode)
        val info = (loader.load() as LoadingResult.Success).info
        vm = VirtualMachine(info, heap)
    }

    @Test(timeout = 500L)
    fun testVmOverZc() {
        val mainArgsArray = heap.allocate(runArgs.size * 4)
        runArgs.withIndex().forEach { (index, value) ->
            heap.writeInt(mainArgsArray + index * 4, when (value) {
                is StackEntry.VMInteger -> value.intValue
                is StackEntry.VMByte -> value.byteValue.toInt()
                else -> error("impossible")
            })
        }

        val actualResult = vm.run(entries(testProgramStartAddress, testProgramRawBytecode.size, mainArgsArray, runArgs.size))

        assertEquals(expectedResult, actualResult)
    }


    @Test
    fun testBytecodeParsing() {
        val rawParsingBytecode = compile("""

            ${includeStdMem()}
            ${includeBytecodeParser()}

            fn main(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
                return parseBytecode(rawBytecode, rawBytecodeSize);
            }

        """.trimIndent())
        val loader = BytecodeLoader(rawParsingBytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val vm = VirtualMachine(info, heap)

        val parsedResultAddress = (vm.run(listOf(testProgramStartAddress.toStackEntry(), testProgramRawBytecode.size.toStackEntry())) as StackEntry.VMInteger).intValue

        val expected = (BytecodeLoader(testProgramRawBytecode).load() as LoadingResult.Success).info

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
