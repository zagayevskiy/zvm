package com.zagayevskiy.zvm.util.extensions

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token

fun Int.toToken() = Token.Integer(this)

fun Lexer.toSequence(): Sequence<Token> = LexerIterator(this).asSequence()

private class LexerIterator(private val lexer: Lexer) : Iterator<Token> {
    private var eofProduced = false

    override fun hasNext() = !eofProduced

    override fun next() = lexer.nextToken().also { token ->
        if (eofProduced) throw NoSuchElementException()
        if (token == Token.Eof) {
            eofProduced = true
        }
    }
}
