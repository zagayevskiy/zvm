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
                val a: int = 13
                return 3
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