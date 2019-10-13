package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.vm.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testdata.ZcTestData
import testdata.zcTestData


@RunWith(Parameterized::class)
internal class CompilerTest(private val test: ZcTestData) {
    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = zcTestData

    }

    private lateinit var compiler: ZcCompiler

    @Before
    fun setup() {
        compiler = ZcCompiler()
    }

    @Test
    fun test() {
        val bytecode = compiler.compile(test.text)
        val loader = BytecodeLoader(bytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val vm = VirtualMachine(info, heap = BitTableMemory(test.heapSize))
        val actualResult = vm.run(test.runArgs)

        assertEquals(test.expectedResult, actualResult)
    }

}