package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
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
                e = 1 || 2 || 3 || 4 & 5 & 6 & 7 || 8 || 9;
                e = e + e +e + e +e -e -e -e + e +e -e * a* b *c *c /d /d /d/d/d;
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
        val result = parser.program()

        println(result)

    }

}

class PrintSpyLexer(private val lexer: Lexer): Lexer {
    override fun nextToken() = lexer.nextToken().also { println(it) }

    override val currentLine
            get() = lexer.currentLine
}