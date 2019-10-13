package com.zagayevskiy.zvm.vm

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testsrc.AsmTestCases

@RunWith(Parameterized::class)
internal class VirtualMachineProgramsTest(private val testCase: VmTestCase) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = AsmTestCases
    }

    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        vm = testCase.createVm(testCase.loadedProgram)
    }

    @Test
    fun test() {
        val result = vm.run(testCase.runArgs)
        assertEquals(testCase.expectedResult, result)
    }
}


