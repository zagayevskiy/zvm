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
private val Int.op
    get() = Operand.Integer(this)
private val String.id
    get() = Operand.Id(this)
private val String.label
    get() = Command.Label(this)

private fun Opcode.instr(vararg operands: Operand) = Instruction(this, listOf(*operands))


abstract class TestOpcode(override val name: String, override val operandCount: Int = 0) : Opcode {
    init {
        @Suppress("LeakingThis")
        ALL.add(this)
    }

    companion object {
        val ALL = mutableListOf<Opcode>()
    }
}

object Ret : TestOpcode("ret")
object ILoad1 : TestOpcode("iload1", 1)
object ILoad2 : TestOpcode("iload2", 2)
object Sum : TestOpcode("sum", 1)
object Call : TestOpcode("call", 1)
object Jmp : TestOpcode("jmp", 1)
object Add1 : TestOpcode("add1", 1)
object Add2 : TestOpcode("add2", 2)
object Add3 : TestOpcode("add3", 3)
object Add4 : TestOpcode("add4", 4)

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
                        Ret.instr()
                ),

                """
                    .fun f: args = 3
                    iload1 0
                    iload2 1, 2
                    sum 3
                    ret
                """.trimIndent() expects listOf(
                        "f".func(args = 3),
                        ILoad1.instr(0.op),
                        ILoad2.instr(1.op, 2.op),
                        Sum.instr(3.op),
                        Ret.instr()
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
                    call -12345
                """.trimIndent() expects listOf(
                        "main123".func(args = 20, locals = 1000),
                        Add1.instr((-1).op),
                        Add2.instr(0.op, (-1).op),
                        Add3.instr(0.op, (-1).op, 2.op),
                        Add4.instr(0.op, 1.op, (-2).op, 3.op),
                        Sum.instr(4.op),
                        Call.instr("f".id),
                        "f".func(),
                        Call.instr((-12345).op)
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
                        Jmp.instr("label1".id),
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
        val parser = AsmParser(lexer, TestOpcode.ALL)

        val result = parser.program()
        assertEquals(test.expected, result)
    }
}