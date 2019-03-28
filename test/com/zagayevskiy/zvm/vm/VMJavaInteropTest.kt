package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.asm.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals


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

internal data class TstI(val asmText: String, val args: List<StackEntry>, val javaResult: List<Any>)

@RunWith(Parameterized::class)
internal class VMJavaInteropTest(private val test: TstI) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = listOf(
                TstI(javaCasts, listOf(777777.toStackEntry()), listOf(777777, 1234567, "Hello! Привет! Сәлем! Γεια σας!", (-17).toByte(), -1234567)),
                TstI(simpleNewInstance, emptyList(), listOf(EmptyConstructorData())),
                TstI(constructorWithArguments, listOf(987.toStackEntry(), 10.toByte().toStackEntry()), listOf(987, 10.toByte(), "Some String ზოგიერთი სიმებიანი", DataClass(987, 10.toByte(), "Some String ზოგიერთი სიმებიანი"))),
                TstI(overloadedConstructor, emptyList(), listOf(
                        OverloadDataClass(),
                        111, OverloadDataClass(111),
                        222, (-22).toByte(), OverloadDataClass(222, -22),
                        -333, 33.toByte(), "안녕하세요 낯선", OverloadDataClass(-333, 33, "안녕하세요 낯선"),
                        444, 44.toByte(), 100000, EmptyConstructorData(100000), OverloadDataClass(444, 44, "안녕하세요 낯선", EmptyConstructorData(100000))
                ))
        )
    }

    private lateinit var javaInterop: TestJavaInterop
    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        val parser = AsmParser(AsmSequenceLexer(test.asmText.asSequence()), OpcodesMapping.opcodes)
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

        val result = vm.run(test.args)
        assertEquals(test.javaResult, javaInterop.objs.values.toList())


    }

}