package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.asm.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class VirtualMachineTest {

    private val text = """
        .fun f
        call t
        ret
        .fun g: locals = 1
        call f
        ret
        .fun main
        call g
        ret
        .fun t
        ret
    """.trimIndent()

    private lateinit var vm: VirtualMachine

    @Before
    fun setup() {
        val parser = AsmParser(AsmSequenceLexer(text.asSequence()), OpcodesMapping.opcodes)
        val result = parser.program()
        assertTrue(result.toString(), result is ParseResult.Success)
        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        val bytecode = generator.generate(assembler.generate())

        val loader = BytecodeLoader(bytecode)
        val loaded = loader.load() as LoadingResult.Success

        vm = VirtualMachine(loaded.info)
    }

    @Test
    fun test() {
        vm.run(emptyList())
    }


    @Test
    fun test2() {
        val asm = """
            .fun sum3: args = 3
            aload 0
            aload 1
            iadd
            aload 2
            iadd
            ret
            .fun main: args = 3, locals = 2
            iconst 10
            iconst 20
            lstore 0
            lstore 1
            lload 1
            lload 0
            out
            out
            aload 0
            aload 1
            aload 2
            call sum3
            out
        """.trimIndent()
        val parser = AsmParser(AsmSequenceLexer(asm.asSequence()), OpcodesMapping.opcodes)
        val result = parser.program()
        assertTrue(result.toString(), result is ParseResult.Success)
        val assembler = BytecodeAssembler((result as ParseResult.Success).commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        val bytecode = generator.generate(assembler.generate())

        val loader = BytecodeLoader(bytecode)
        val loaded = loader.load() as LoadingResult.Success

        vm = VirtualMachine(loaded.info)
        vm.run(listOf(1.toStackEntry(), 2.toStackEntry(), 10000.toStackEntry()))
    }
}