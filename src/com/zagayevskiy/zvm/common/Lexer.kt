package com.zagayevskiy.zvm.common

import com.zagayevskiy.zvm.util.extensions.toToken

interface Token {
    object Eof : Token
    object Eol : Token
    data class Error(val lineNumber: Int, val sequence: String) : Token
    data class Identifier(val name: String) : Token
    data class Integer(val value: Int) : Token
}

interface Lexer {
    fun nextToken(): Token

    val currentLine: Int
}

class SequenceLexer(private val sequence: Sequence<Char>,
                    private val symbols: Map<String, Token>,
                    private val keywords: Map<String, Token>,
                    private val whitespace: Char.() -> Boolean = { isWhitespace() },
                    private val idStart: Char.() -> Boolean = { isJavaIdentifierStart() },
                    private val idPart: Char.() -> Boolean = { isJavaIdentifierPart() },
                    private val eolAsToken: Boolean = true) : Lexer {

    override var currentLine = 0

    private val sortedSymbols = symbols.keys.sorted()

    private val iterator by lazy { sequence.iterator() }
    private var currentChar: Char? = null
    private var parsingStarted = false


    override fun nextToken(): Token {
        if (!parsingStarted) {
            parsingStarted = true
            nextChar()
        }
        var current = currentChar ?: return Token.Eof

        if (eolAsToken) {
            if (current.isEol()) {
                return consumeEol()
            }
        } else {
            while (currentChar?.isEol() == true) {
                consumeEol()
            }
            current = currentChar ?: return Token.Eof
        }

        while (current.whitespace()) {
            current = nextChar() ?: return Token.Eof
        }

        if (current.isDigit()) {
            return consumeNumber()
        }

        sortedSymbols.tail(current).takeIf { it.isNotEmpty() }?.let { tail -> return consumeSymbol(tail) }

        if (current.idStart()) {
            val builder = StringBuilder().append(current)

            current = nextChar() ?: return keywordOrId(builder.toString())
            while (current.idPart()) {
                builder.append(current)
                current = nextChar() ?: return keywordOrId(builder.toString())
            }

            return keywordOrId(builder.toString())
        }


        return Token.Error(currentLine, current.toString())
    }

    private fun keywordOrId(buffer: String) = keywords[buffer] ?: Token.Identifier(buffer)

    private fun consumeSymbol(precomputedTail: List<String>): Token {
        var current = currentChar ?: throw IllegalStateException("currentChar must not be null at this point")
        val builder = StringBuilder().append(current)
        var tail = precomputedTail
        var prevTail = tail
        while (tail.isNotEmpty()) {
            current = nextChar() ?: return symbolOrError(builder.toString(), tail.first())
            builder.append(current)
            prevTail = tail
            tail = tail.tail(builder)
        }

        val mayBeFound = prevTail.first()
        if (mayBeFound.length == builder.length - 1) {
            return symbols[mayBeFound]!!
        }

        return Token.Error(currentLine, builder.toString())
    }

    private fun symbolOrError(buffer: String, possibleSymbol: String) = when {
        (buffer == possibleSymbol) -> symbols[possibleSymbol]!!
        else -> Token.Error(currentLine, buffer)
    }

    private fun consumeEol(): Token {
        val prev = currentChar
        nextChar()
        if ((currentChar == '\n' && prev == '\r') || (currentChar == '\r' && prev == '\r')) {
            nextChar()
        }
        ++currentLine

        return Token.Eol
    }

    private fun consumeNumber(): Token {
        var current = currentChar
        var int = 0
        while (current != null && current.isDigit()) {
            int = int * 10 + current.toString().toInt()
            current = nextChar()
        }
        return int.toToken()
    }

    private fun nextChar(): Char? = (if (iterator.hasNext()) iterator.next() else null).also { currentChar = it }
}

private fun Char.isEol() = this == '\n' || this == '\r'

private fun List<String>.tail(predicate: CharSequence.() -> Boolean) = indexOfFirst { it.predicate() }.takeIf { it >= 0 }
        ?.let { index -> subList(index, size) } ?: emptyList()

private fun List<String>.tail(value: CharSequence): List<String> = tail { startsWith(value) }
private fun List<String>.tail(value: Char): List<String> = tail { startsWith(value) }