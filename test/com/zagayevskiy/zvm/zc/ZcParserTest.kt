package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import org.junit.Assert.*
import org.junit.Test

class ZcParserTest {

    @Test
    fun test() {


        val text = """

            struct str {
                val x = 1;
                var y: int;
            }

            fn main(argc: int): int {
                val a = 1;
                var b = 2;
                var c: int;
                var d: byte = 3;
                val e: int  = 4;
                c = (a + b) - (d[e]*e[d]);
                if(true){
                    c;
                    while(1) {
                        for(;;){}
                    }
                }
                return 6;
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