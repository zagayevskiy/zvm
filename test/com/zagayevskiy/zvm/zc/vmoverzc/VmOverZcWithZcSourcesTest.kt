package com.zagayevskiy.zvm.zc.vmoverzc

import com.zagayevskiy.zvm.vm.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testdata.ZcTestData
import testdata.zcTestData

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

