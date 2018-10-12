package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.SequenceLexer
import com.zagayevskiy.zvm.common.Token

object AsmToken {
    object Comma : Token
    object Arrow : Token
    object Minus : Token
    object Fun : Token
}

private val symbolsMap = mapOf(
        "," to AsmToken.Comma,
        "->" to AsmToken.Arrow,
        "-" to AsmToken.Minus
)

private val keywordsMap = mapOf(
        ".fun" to AsmToken.Fun
)

class AsmSequenceLexer(sequence: Sequence<Char>) : Lexer by SequenceLexer(
        sequence = sequence,
        symbols = symbolsMap,
        keywords = keywordsMap,
        idStart = { isLetter() || this == '.' },
        idPart = { isLetter() || this == '_' }
)