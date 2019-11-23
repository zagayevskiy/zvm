package com.zagayevskiy.zvm.common

abstract class AbsParser {

    @Suppress("PropertyName")
    protected val NotMatched = null

    protected abstract val lexer: Lexer

    protected lateinit var token: Token

    protected fun nextToken() {
        token = lexer.nextToken().also { currentToken ->
            (currentToken as? Token.Error)?.let { error("Lexical error at sequence ${it.sequence}") }
        }
    }

    protected fun error(message: String = "Syntax error at token $token"): Nothing = throw ParseException("Line [${lexer.currentLineNumber}](${lexer.currentLine}): $message")

    protected inline fun <reified T : Token> expect() = maybe<T>()
            ?: error("Unexpected token $token. Token of type ${T::class.simpleName} expected.")

    protected inline fun <reified T : Token> maybe() = (token as? T)?.also { nextToken() }

    protected inline fun <T : Token, R> T.andThan(block: (T) -> R): R = block(this)

    protected inline fun <reified T : Token, R : Any> matchList(element: () -> R?): List<R>? {
        val first = element() ?: return NotMatched
        return mutableListOf(first).apply {
            while (maybe<T>() != null) add(element() ?: error())
        }
    }

}


class ParseException(override val message: String) : Exception()