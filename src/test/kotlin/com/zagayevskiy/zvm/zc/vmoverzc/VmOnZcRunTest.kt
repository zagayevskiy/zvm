package com.zagayevskiy.zvm.zc.vmoverzc

import com.zagayevskiy.zvm.common.preprocessing.JavaAssetsIncludesResolver
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.memory.Memory
import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import com.zagayevskiy.zvm.vm.BytecodeLoader
import com.zagayevskiy.zvm.vm.LoadingResult
import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.vm.VirtualMachine
import com.zagayevskiy.zvm.zc.ZcCompiler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testdata.cases.AsmTestCases
import testdata.cases.VmTestCase
import testdata.cases.ZcTestCases

@RunWith(Parameterized::class)
internal class VmOnZcRunTest(private val testCase: VmTestCase) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = ZcTestCases + AsmTestCases

        private const val HEAP_SIZE = 1024 * 65536
    }

    private lateinit var compiler: ZcCompiler
    private lateinit var heap: Memory
    private var bytecodeAddress = 0
    private var testcaseMainArgsAddress = 0
    private var testcastMainArgsByteSize = 0

    @Before
    fun setup() {
        heap = BitTableMemory(HEAP_SIZE).apply {
            bytecodeAddress = allocate(testCase.bytecode.size)
            copyIn(testCase.bytecode, bytecodeAddress)

            val args = testCase.runArgs.toByteArray()
            testcastMainArgsByteSize = args.size
            testcaseMainArgsAddress = allocate(testcastMainArgsByteSize)
            copyIn(args, testcaseMainArgsAddress)
        }

        compiler = ZcCompiler()
        testCase.prepare()
    }

    @Test
    fun runTestCasesInNestedVm() {
        val compiledVm = compiler.compile(JavaAssetsIncludesResolver("/includes/zc").resolve("zvm/vm_core.zc")!!)
        val vmLoader = BytecodeLoader(compiledVm)
        val loadedVm = vmLoader.load() as LoadingResult.Success

        val regularVm = VirtualMachine(loadedVm.info, heap = heap, io = testCase.io, crashHandler = testCase.crashHandler)

        val actualResult = regularVm.run(entries(bytecodeAddress, testCase.bytecode.size, testcaseMainArgsAddress, testcastMainArgsByteSize))

        testCase.checkResult(actualResult)
    }

    private fun List<StackEntry>.toByteArray(): ByteArray {
        val size = map { if (it is StackEntry.VMInteger) 4 else 1 }.sum()
        val result = ByteArray(size)
        if (size == 0) return result

        var cursor = 0
        forEach {
            when (it) {
                is StackEntry.VMInteger -> {
                    it.intValue.copyToByteArray(result, cursor)
                    cursor += 4
                }
                is StackEntry.VMByte -> {
                    result[cursor++] = it.byteValue
                }
            }
        }
        return result
    }
}
