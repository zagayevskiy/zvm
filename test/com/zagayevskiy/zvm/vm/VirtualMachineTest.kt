package com.zagayevskiy.zvm.vm

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testdata.cases.AsmTestCases
import testdata.cases.ZcTestCases
import testdata.cases.VmTestCase

@RunWith(Parameterized::class)
internal class VirtualMachineProgramsTest(private val testCase: VmTestCase) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = ZcTestCases + AsmTestCases
    }

    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        val loader = BytecodeLoader(testCase.bytecode)
        val info = (loader.load() as LoadingResult.Success).info
        vm = testCase.createVm(info)

    }

    @Test
    fun test() {
        val result = vm.run(testCase.runArgs)
        assertEquals(testCase.expectedResult, result)
    }
}


