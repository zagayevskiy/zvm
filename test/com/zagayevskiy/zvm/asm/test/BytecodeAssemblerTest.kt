package com.zagayevskiy.zvm.asm.test

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.asm.FunctionDefinition.Arg
import com.zagayevskiy.zvm.asm.FunctionDefinition.Type.DefByte
import com.zagayevskiy.zvm.asm.FunctionDefinition.Type.DefInt
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
            .fun main: x: int, y: byte;
            jmp lbl
            call f
            ->lbl
            ret

            .fun f
            consti 123
            call g
            ret

            .fun g: z: int;
            ->lbl
            call f
            jmp lbl
            ret
        """.trimIndent()

        val bytes = listOf(
                Jmp.byte, 10.bytes, //1st lbl
                Call.byte, 1.bytes, //1st function
                Ret.byte,
                IntConst.byte,
                123.bytes,
                Call.byte, 2.bytes, //2nd function
                Ret.byte,
                Call.byte, 1.bytes, //1st function
                Jmp.byte, 22.bytes, //2nd lbl
                Ret.byte
        ).flatten()

        val result = AsmParser(AsmSequenceLexer(text.asSequence()), OpcodesMapping.opcodes).program()
        val commands = (result as ParseResult.Success).commands
        val generator = BytecodeAssembler(commands, OpcodesMapping.mapping)
        val info = generator.generate()

        val expectedDefinedFunctions = listOf(
                FunctionDefinition("main", 0, true, 0, args = listOf(
                        Arg("x", DefInt, -5),
                        Arg("y", DefByte, offset = -1))),
                FunctionDefinition("f", 1, true, 11, args = emptyList()),
                FunctionDefinition("g", 2, true, 22, args = listOf(
                        Arg("z", DefInt, offset = -4)))
                )

        assertEquals (expectedDefinedFunctions, info.functions)
        assertEquals(bytes, info.bytecode.toList())
    }
}