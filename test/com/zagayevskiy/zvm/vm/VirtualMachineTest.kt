package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.memory.BitTableMemory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testdata.cases.AsmTestCases
import testdata.cases.VmTestCase
import testdata.cases.ZcTestCases

@RunWith(Parameterized::class)
internal class VirtualMachineProgramsTest(private val testCase: VmTestCase) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = (ZcTestCases + AsmTestCases)
    }

    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        testCase.prepare()
        val loader = BytecodeLoader(testCase.bytecode)
        val info = (loader.load() as LoadingResult.Success).info
        vm = VirtualMachine(info, testCase.stackSize, BitTableMemory(testCase.heapSize), io = testCase.io)
    }

    @Test
    fun test() {
        val result = vm.run(testCase.runArgs)
        testCase.checkResult(result)
    }
}


