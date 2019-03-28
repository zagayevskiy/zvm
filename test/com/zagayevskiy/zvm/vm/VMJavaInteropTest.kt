package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.asm.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


private class TestJavaInterop : JavaInterop {
    val objs = mutableMapOf<Int, Any?>()
    var objIndex = 1
    override fun put(obj: Any?): Int {
        val index = objIndex++
        objs[index] = obj
        return index
    }

    override fun get(index: Int): Any? = objs[index]

    override fun remove(index: Int) {
        objs.remove(index)
    }

}

internal class VMJavaInteropTest {

    private lateinit var javaInterop: TestJavaInterop
    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        val parser = AsmParser(AsmSequenceLexer(javaNewTest.asSequence()), OpcodesMapping.opcodes)
        val result = parser.program()
        Assert.assertTrue(result.toString(), result is ParseResult.Success)
        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        val bytecode = generator.generate(assembler.generate())

        val loader = BytecodeLoader(bytecode)
        val loaded = loader.load() as LoadingResult.Success

        javaInterop = TestJavaInterop()
        vm = VirtualMachine(loaded.info, 65536, javaInterop)
    }

    @Test
    fun test() {

        val result = vm.run(emptyList())
        assertEquals(result, 1.toStackEntry())
        assertTrue(javaInterop.get((result as StackEntry.VMInteger).intValue) is NewInteropTestClass)
        assertEquals(javaInterop.objs.size, 1)

    }

}