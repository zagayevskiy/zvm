package com.zagayevskiy.zvm.zc.vmoverzc

import com.zagayevskiy.zvm.assertEquals
import com.zagayevskiy.zvm.common.BackingStruct
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.memory.Memory
import com.zagayevskiy.zvm.util.extensions.copyToInt
import com.zagayevskiy.zvm.vm.*
import com.zagayevskiy.zvm.zc.ZcCompiler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testdata.cases.AsmTestCases
import testdata.cases.VmTestCase
import testdata.cases.ZcTestCases
import testdata.sources.zc.vm.testsrc.bytecodeLoading
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
internal class BytecodeLoadingOnZcTest(private val testCase: VmTestCase) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = AsmTestCases + ZcTestCases

        private const val HEAP_SIZE = 65536
    }

    private lateinit var heap: Memory
    private lateinit var vm: VirtualMachine
    private lateinit var testCaseLoadedInfo: LoadedInfo
    private var bytecodeAddress: Int = 0

    @Before
    fun setup() {
        heap = BitTableMemory(HEAP_SIZE).apply {
            bytecodeAddress = allocate(testCase.bytecode.size)
            copyIn(testCase.bytecode, bytecodeAddress)
        }

        val compiler = ZcCompiler()
        val testCode = compiler.compile(bytecodeLoading)
        val loader = BytecodeLoader(testCode)
        val info = loader.load() as LoadingResult.Success

        vm = VirtualMachine(info.info, heap = heap)

        testCaseLoadedInfo = (BytecodeLoader(testCase.bytecode).load() as LoadingResult.Success)?.info

    }

    @Test
    fun test() {
        val parsedInfoAddress = vm.run(entries(bytecodeAddress, testCase.bytecode.size))
        val rawHeap = ByteArray(HEAP_SIZE)
        heap.copyOut(0, rawHeap)
        val actualInfo = TestProgramInfo(rawHeap, (parsedInfoAddress as StackEntry.VMInteger).intValue)

        assertEquals(testCaseLoadedInfo.bytecode.size, actualInfo.bytecodeSize)
        assertEquals(testCaseLoadedInfo.mainIndex, actualInfo.mainIndex)
        assertEquals(testCaseLoadedInfo.functions.size, actualInfo.functionTableSize)

        testCaseLoadedInfo.functions.zip(actualInfo.table()) { expected, actual ->
            assertEquals(expected.address, actual.address)
            assertEquals(expected.argsMemorySize, actual.argsMemorySize)
        }

        val actualConstantPool = ByteArray(actualInfo.constantPoolSize)
        heap.copyOut(actualInfo.constantPoolAddress, actualConstantPool)
        assertEquals(actualConstantPool.toList(), testCaseLoadedInfo.constantPool.toList())

        val actualBytecode = ByteArray(actualInfo.bytecodeSize)
        heap.copyOut(actualInfo.bytecodeAddress, actualBytecode)
        assertEquals(actualBytecode.toList(), testCaseLoadedInfo.bytecode.toList())
    }
}

/*
    struct ProgramInfo {
        var mainIndex: int;
        var functionsTable: [RuntimeFunction];
        var functionsTableSize: int;
        var constantPool: [byte];
        var constantPoolSize: int;
        var bytecode: [byte];
        var bytecodeSize: int;
    }

*/
private class TestProgramInfo(array: ByteArray, offset: Int): BackingStruct(array, offset) {
    val mainIndex by int
    val functionsTableAddress by int
    val functionTableSize by int
    val constantPoolAddress by int
    val constantPoolSize by int
    val bytecodeAddress by int
    val bytecodeSize by int

    fun table(): List<TestRuntimeFunction> {
        return (0 until functionTableSize).map { index ->
            val address = index*4 + functionsTableAddress
            val structAddress = array.copyToInt(address)
            TestRuntimeFunction(array, structAddress)
        }
    }
}

/*
struct RuntimeFunction {
    var address: int;
    var argsMemorySize: int;
}
 */
private class TestRuntimeFunction(array: ByteArray, offset: Int): BackingStruct(array, offset) {
    val address by int
    val argsMemorySize by int
}