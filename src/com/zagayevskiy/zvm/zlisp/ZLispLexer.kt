package com.zagayevskiy.zvm.zlisp

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.SequenceLexer
import com.zagayevskiy.zvm.common.Token

object ZLispToken {
    object ParenthesisOpen : Token
    object ParenthesisClose : Token
    object Dot: Token
}

private val symbolsMap = mapOf(
        "(" to ZLispToken.ParenthesisOpen,
        ")" to ZLispToken.ParenthesisClose,
        "." to ZLispToken.Dot
)

private val idSymbols = setOf('-', '+', '*', '/', '!')

class ZLispLexer(sequence: Sequence<Char>) : Lexer by SequenceLexer(sequence, symbolsMap, emptyMap(),
        idStart = { isJavaIdentifierStart() },
        idPart = { isJavaIdentifierStart() || this in idSymbols },
        eolAsToken = false,
        stringConstDelimitator = { symbol -> symbol.takeIf { it == '"' } })