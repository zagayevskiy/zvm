package com.zagayevskiy.zvm.zlisp

import com.zagayevskiy.zvm.assertEquals
import com.zagayevskiy.zvm.zlisp.Sexpr.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


private val String.atom
    get() = Atom(this)

private fun slist(vararg sexprs: Sexpr): DotPair {
    return slist(sexprs.first(), sexprs.drop(1))
}

private fun slist(head: Sexpr, tail: List<Sexpr>): DotPair {
    if (tail.isEmpty()) return (head dot Nil)
    return head dot slist(tail.first(), tail.drop(1))
}

@RunWith(Parameterized::class)
class ZLispParserTest(private val testCase: Pair<String, List<Sexpr>>) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): List<Pair<String, List<Sexpr>>> = listOf(
                "nil" to listOf(Nil),
                "()" to listOf(Nil),
                "atm" to listOf(Atom("atm")),
                "(h . t)" to listOf(("h".atom dot "t".atom)),
                "(single)" to listOf(slist("single".atom)),
                "(h t)" to listOf(slist("h".atom, "t".atom)),
                "(a (b . c))" to listOf(slist("a".atom, ("b".atom dot "c".atom))),
                "(a b c d)" to listOf(slist("a".atom, "b".atom, "c".atom, "d".atom)),
                "(())" to listOf(slist(Nil)),
                "((()))" to listOf(slist(slist(Nil))),
                "(() ())" to listOf(slist(Nil, Nil)),
                "((a.b))" to listOf(slist("a".atom dot "b".atom)),
                "((a) . (b . c))" to listOf((slist("a".atom) dot ("b".atom dot "c".atom))),
                """
                    nil
                    ()
                    atom
                    (a.b)
                    (a (b (c (d (e (f (g)))))))
                    (a b c d e f g)
                """.trimIndent() to listOf(
                        Nil,
                        Nil,
                        Atom("atom"),
                        "a".atom dot "b".atom,
                        slist("a".atom, slist("b".atom, slist("c".atom, slist("d".atom, slist("e".atom, slist("f".atom, slist("g".atom))))))),
                        slist("a".atom, "b".atom, "c".atom, "d".atom, "e".atom, "f".atom, "g".atom)
                )
        )
    }

    private lateinit var parser: ZLispParser

    @Before
    fun setup() {
        val (source, _) = testCase
        parser = ZLispParser(ZLispLexer(source.asSequence()))
    }

    @Test
    fun test() {
        val (_, expected) = testCase

        val result = parser.parse() as LispParseResult.Success

        assertEquals(result.program, expected)
    }

}