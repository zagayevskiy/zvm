package com.zagayevskiy.zvm.zlisp

import com.zagayevskiy.zvm.common.AbsParser
import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.ParseException
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.zlisp.Sexpr.*

sealed class Sexpr {
    data class DotPair(val head: Sexpr, val tail: Sexpr): Sexpr() {
        override fun toString() = "($head . $tail)"
    }
    data class Atom(val name: String): Sexpr() {
        override fun toString() = name
    }
    data class Number(val value: Int): Sexpr() {
        override fun toString() = value.toString()
    }
    data class Str(val value: String): Sexpr() {
        override fun toString() =  """"$value""""
    }

    object Nil: Sexpr() {
        override fun toString() = "nil"
    }

    abstract class RuntimeOnly: Sexpr()
}

infix fun Sexpr.dot(tail: Sexpr) = DotPair(head = this, tail = tail)

sealed class LispParseResult {
    data class Success(val program: List<Sexpr>): LispParseResult()
    class Failure(val exception: ParseException) : LispParseResult()
}

class ZLispParser(override val lexer: Lexer) : AbsParser() {
    fun parse(): LispParseResult {
        return try {
            nextToken()
            val program = mutableListOf<Sexpr>().apply {
                while (token != Token.Eof) {
                    add(sexpr())
                }
            }

            LispParseResult.Success(program)
        } catch (e: ParseException) {
            LispParseResult.Failure(e)
        }
    }

    private fun sexpr(): Sexpr {
        maybe<Token.Integer>()?.value?.let { return Sexpr.Number(it) }
        maybe<Token.Identifier>()?.name?.let { atom -> return if (atom == "nil") Nil else Atom(atom) }
        maybe<Token.StringConst>()?.value?.let { return Sexpr.Str(it) }

        return dotPairOrList()
    }


    private fun dotPairOrList(): Sexpr {
        expect<ZLispToken.ParenthesisOpen>()

        maybe<ZLispToken.ParenthesisClose>()?.andThan {  return Nil }

        val head = sexpr()

        maybe<ZLispToken.Dot>()?.andThan {
            val tail = sexpr()
            expect<ZLispToken.ParenthesisClose>()
            return (head dot tail)
        }

        return (head dot consumeList())
    }

    private fun consumeList(): Sexpr {
        maybe<ZLispToken.ParenthesisClose>()?.andThan { return Nil }
        val head = sexpr()
        return (head dot consumeList())
    }
}