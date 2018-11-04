package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import org.junit.Assert.*
import org.junit.Test

class ZcParserTest {

    @Test
    fun test() {


        val text = """
            fn main(argc: int): int {
                val x = 1
                var y: int
                val z: byte = 2
                val a: int = z

                y = f() + g(x, y, z, a + 1 * ~!~!2 -3)[1][2 - 3] 

                return 3
            }

            fn self(i: Int) = self

            fn f() {
                return 0
            }
        """.trimIndent()

        val parser = ZcParser(PrintSpyLexer(ZcSequenceLexer(text.asSequence())))
        parser.program()

    }

}

class PrintSpyLexer(private val lexer: Lexer): Lexer {
    override fun nextToken() = lexer.nextToken().also { println(it) }

    override val currentLine
            get() = lexer.currentLine
}