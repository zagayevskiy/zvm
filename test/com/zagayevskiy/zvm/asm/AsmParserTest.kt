package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.asm.Command.Func
import com.zagayevskiy.zvm.asm.Command.Instruction
import com.zagayevskiy.zvm.asm.Command.Instruction.Operand
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

internal class TestData(val text: String, val expected: ParseResult)

private infix fun String.expects(expected: List<Command>) = TestData(this, ParseResult.Success(expected))
private fun String.func(args: Int = 0, locals: Int = 0) = Func(this, args, locals)
private fun String.instr(vararg operands: Operand) = Instruction(this, listOf(*operands))
private val Int.op
    get() = Operand.Integer(this)
private val String.id
    get() = Operand.Id(this)
private val String.label
    get() = Command.Label(this)


@RunWith(Parameterized::class)
internal class AsmParserTest(private val test: TestData) {
    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
                """
                    .fun main
                    ret
                """.trimIndent() expects listOf(
                        "main".func(),
                        "ret".instr()
                ),

                """
                    .fun f: args = 3
                    iload 0
                    iload 1, 2
                    sum 3
                    ret
                """.trimIndent() expects listOf(
                        "f".func(args = 3),
                        "iload".instr(0.op),
                        "iload".instr(1.op, 2.op),
                        "sum".instr(3.op),
                        "ret".instr()
                ),

                """
                    .fun main123: args=20, locals = 1000
                    add1 -1
                    add2 0, -1
                    add3 0, -1, 2
                    add4 0, 1, -2, 3
                    sum 4
                    call f

                    .fun f
                    call f, -12345
                """.trimIndent() expects listOf(
                        "main123".func(args = 20, locals = 1000),
                        "add1".instr((-1).op),
                        "add2".instr(0.op, (-1).op),
                        "add3".instr(0.op, (-1).op, 2.op),
                        "add4".instr(0.op, 1.op, (-2).op, 3.op),
                        "sum".instr(4.op),
                        "call".instr("f".id),
                        "f".func(),
                        "call".instr("f".id, (-12345).op)
                ),

                """
                    .fun f
                    .fun g: locals = 1
                    jmp label1
                    .fun k: args = 2
                    .fun t: args = 3, locals = 4
                    ->label1
                    ->label2
                    ->label3
                """.trimIndent() expects listOf(
                        "f".func(),
                        "g".func(locals = 1),
                        "jmp".instr("label1".id),
                        "k".func(args = 2),
                        "t".func(args = 3, locals = 4),
                        "label1".label,
                        "label2".label,
                        "label3".label
                )
        )
    }


    @Test
    fun test() {
        val lexer = AsmSequenceLexer(test.text.asSequence())
        val parser = AsmParser(lexer)

        val result = parser.program()
        assertEquals(test.expected, result)
    }
}