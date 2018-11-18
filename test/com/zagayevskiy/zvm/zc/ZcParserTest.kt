package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class TestData(val text: String, val expected: AstProgram)

class ZcParserTest {


    val text = """
        fn  main(i: int) {
        }
    """.trimIndent()
    val expected = AstProgram(mutableListOf(
            AstFunctionDeclaration(
                    "main1",
                    listOf(FunctionArgumentDeclaration("i", UnresolvedType.Simple("int"))),
                    null,
                    body = AstBlock(emptyList())
            )
    ))

    @Test
    fun t() {
        val parser = ZcParser(PrintSpyLexer(ZcSequenceLexer(text.asSequence())))
        val result = parser.program() as ParseResult.Success
        assertEquals(expected, result.program)
    }

    @Test
    fun test() {


        val text = """
            fn map(i: int, mapper: (int) -> byte) : byte {
                return mapper(i);
            }

            struct str {
                val x = 1;
                var y: int;
            }

            fn main(argc: int): int {
                f(1, 2, 3, 4)[5 + 6];
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

class PrintSpyLexer(private val lexer: Lexer) : Lexer {
    override fun nextToken() = lexer.nextToken().also { println(it) }

    override val currentLine
        get() = lexer.currentLine
}