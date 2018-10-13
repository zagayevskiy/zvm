package com.zagayevskiy.zvm.common

import com.zagayevskiy.zvm.assertEquals
import com.zagayevskiy.zvm.common.Token.*
import com.zagayevskiy.zvm.util.extensions.toSequence
import com.zagayevskiy.zvm.util.extensions.toToken
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


private object TestToken : Token
private object Fun : Token
private object Label : Token
private object Plus : Token
private object PlusPlus : Token
private object PlusPlusPlus : Token
private object Minus : Token

private val keywords = mapOf(
        "t-e-s-t" to TestToken,
        "func" to Fun,
        "label" to Label
)

private val symbols = mapOf(
        "+" to Plus,
        "++" to PlusPlus,
        "+++" to PlusPlusPlus,
        "-" to Minus
)

@RunWith(Parameterized::class)
internal class SequenceLexerTest(private val test: com.zagayevskiy.zvm.common.Test) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = listOf(
                "b+e+s+t t-e-s-t" expects listOf("b".id, Plus, "e".id, Plus, "s".id, Plus, "t".id, TestToken, Eof),
                """
                    func func1
                    func func2
                """.trimIndent() expects listOf(
                        Fun, "func1".id, Eol,
                        Fun, "func2".id,
                        Eof
                ),
                "1 22 345" expects listOf(1.tkn, 22.tkn, 345.tkn, Eof),
                "a bb ccc" expects listOf("a".id, "bb".id, "ccc".id, Eof),
                "+ ++ +++ + ++ ++ ++ + + + +++ +++ +++" expects listOf(Plus, PlusPlus, PlusPlusPlus, Plus, PlusPlus, PlusPlus, PlusPlus, Plus, Plus, Plus, PlusPlusPlus, PlusPlusPlus, PlusPlusPlus, Eof),
                """
                    func 1 label
                    func 2 label
                    func 3 label
                    123++ + ++456

                    qwerty
             """.trimIndent() expects listOf(
                        Fun, 1.tkn, Label, Eol,
                        Fun, 2.tkn, Label, Eol,
                        Fun, 3.tkn, Label, Eol,
                        123.tkn, PlusPlus, Plus, PlusPlus, 456.tkn, Eol,
                        Eol,
                        "qwerty".id,
                        Eof
                ),
                "- - - t-e-s-t +++ ++ +" expects listOf(Minus, Minus, Minus, TestToken, PlusPlusPlus, PlusPlus, Plus, Eof),
                "a - 1-bc+2+d345" expects listOf("a".id, Minus, 1.tkn, Minus, "bc".id, Plus, 2.tkn, Plus, "d345".id, Eof),
                """
                    1
                    2
                    3
                    t-e-s-t""".trimIndent() expects listOf(
                        1.tkn, Eol,
                        2.tkn, Eol,
                        3.tkn, Eol,
                        TestToken,
                        Eof),
                "#1###2######3" expects listOf(1.tkn, 2.tkn, 3.tkn, Eof)
        )

    }

    @Test
    fun testTokenization() {
        val lexer = SequenceLexer(
                sequence = test.text.asSequence(),
                symbols = symbols,
                keywords = keywords,
                whitespace = { isWhitespace() || this == '#' },
                idStart = { isLetter() },
                idPart = { isLetterOrDigit() || this == '-' })
        val actual = lexer.toSequence().toList()
        assertEquals(actual, test.expected)
    }

}

val String.id
    get() = Identifier(this)
val Int.tkn
    get() = toToken()


internal data class Test(val text: String, val expected: List<Token>)

private infix fun String.expects(tokens: List<Token>) = Test(this, tokens)