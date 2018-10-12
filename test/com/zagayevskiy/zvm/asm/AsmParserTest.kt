package com.zagayevskiy.zvm.asm

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AsmParserTest(val test: String) {
    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
                """
                    .fun main
                    push 1, -2, 3
                    jmp lbl
                    pop
                    ->lbl
                    ret

                    .fun some_test
                    ret
                """.trimIndent()
        )
    }


    @Test
    fun test() {
        val lexer = AsmSequenceLexer(test.asSequence())
        val parser = AsmParser(lexer)

        parser.program()
    }
}