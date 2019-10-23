package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.SequenceLexer
import com.zagayevskiy.zvm.common.Token

object AsmToken {
    object Args : Token
    object Arrow : Token
    object Assign : Token
    object Colon : Token
    object Comma : Token
    object Minus : Token
    object Fun : Token
    object Pool: Token
    object Locals : Token
    object Globals: Token
}

private val symbolsMap = mapOf(
        "->" to AsmToken.Arrow,
        "=" to AsmToken.Assign,
        ":" to AsmToken.Colon,
        "," to AsmToken.Comma,
        "-" to AsmToken.Minus
)

private val keywordsMap = mapOf(
        ".fun" to AsmToken.Fun,
        ".pool" to AsmToken.Pool,
        "args" to AsmToken.Args,
        "locals" to AsmToken.Locals,
        "globals" to AsmToken.Globals
)

class AsmSequenceLexer(sequence: Sequence<Char>) : Lexer by SequenceLexer(
        sequence = sequence,
        symbols = symbolsMap,
        keywords = keywordsMap,
        idStart = { isLetter() || this == '.' },
        idPart = { isLetterOrDigit() || this == '_' },
        stringConstDelimitator = { it.takeIf { limiter -> limiter == '"' } }
)