package com.zagayevskiy.zvm.asm.test

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import org.junit.Assert.*
import org.junit.Test

private val OpcodeImpl.byte
    get() = listOf(OpcodesMapping.mapping[this])
private val Int.bytes
    get() = ByteArray(4).also { dest -> copyToByteArray(dest) }.toList()


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

        val bytes = listOf(
                Jmp.byte, 10.bytes, //1st lbl
                Call.byte, 1.bytes, //1st function
                Ret.byte,
                Push.byte,
                123.bytes,
                Call.byte, 2.bytes, //2nd function
                Ret.byte,
                Call.byte, 1.bytes, //1st function
                Jmp.byte, 22.bytes, //2nd lbl
                Ret.byte
        ).flatMap { it }

        val result = AsmParser(AsmSequenceLexer(text.asSequence()), OpcodesMapping.opcodes).program()
        val commands = (result as ParseResult.Success).commands
        val generator = BytecodeAssembler(commands, OpcodesMapping.mapping)
        val info = generator.generate()

        assertEquals("${info.functions}", 3, info.functions.size)
        assertEquals(bytes, info.bytecode.toList())
    }
}