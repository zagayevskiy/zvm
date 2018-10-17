package com.zagayevskiy.zvm.asm

import org.junit.Assert.*
import org.junit.Test

class BytecodeAssemblerTest {

    @Test
    fun test() {
        val text = """

            .fun main: args=2
            jmp lbl
            call f
            ->lbl
            ret

            .fun f
            push 123
            call g
            ret

            .fun g: args=1, locals=2
            ->lbl
            call f
            jmp lbl
            ret
        """.trimIndent()

        val parser = AsmParser(AsmSequenceLexer(text.asSequence()), byteOpcodes)
        val result = parser.program()
        assertTrue("$result", result is ParseResult.Success)
        val commands = (result as ParseResult.Success).commands

        val generator = BytecodeAssembler(commands)

        val info = generator.generate()

        assertEquals("${info.functions}", 3, info.functions.size)

    }
}