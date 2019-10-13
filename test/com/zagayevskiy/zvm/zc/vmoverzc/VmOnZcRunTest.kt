package com.zagayevskiy.zvm.zc.vmoverzc

import com.zagayevskiy.zvm.assertEquals
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.memory.Memory
import com.zagayevskiy.zvm.vm.*
import com.zagayevskiy.zvm.zc.ZcCompiler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testdata.cases.AsmTestCases
import testdata.cases.VmTestCase
import testdata.cases.ZcTestCases
import testdata.sources.zc.bytecodeLoading
import testdata.sources.zc.vm.vmOverZc
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class VmOnZcRunTest(private val testCase: VmTestCase) {

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
        val testCode = compiler.compile(vmOverZc)
        val loader = BytecodeLoader(testCode)
        val info = loader.load() as LoadingResult.Success

        vm = VirtualMachine(info.info, heap = heap)

        testCaseLoadedInfo = (BytecodeLoader(testCase.bytecode).load() as LoadingResult.Success)?.info

    }

    @Test
    fun test() {
        vm.run(emptyList())

    }
}
