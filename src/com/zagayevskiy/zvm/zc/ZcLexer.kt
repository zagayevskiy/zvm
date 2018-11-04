package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.SequenceLexer
import com.zagayevskiy.zvm.common.Token

object ZcToken {
    object Arrow : Token
    object Assign : Token
    object Colon : Token
    object Comma : Token
    object Dot : Token

    object Plus : Token
    object PlusPlus : Token
    object Minus : Token
    object MinusMinus : Token
    object Asterisk : Token
    object Slash : Token
    object Percent : Token

    object Equals : Token
    object Great : Token
    object GreatEq : Token
    object Less : Token
    object LessEq : Token

    object ParenthesisOpen : Token
    object ParenthesisClose : Token
    object SquareBracketOpen : Token
    object SquareBracketClose : Token
    object CurlyBracketOpen : Token
    object CurlyBracketClose : Token

    object Struct : Token
    object Fun : Token
    object Var : Token
    object Val : Token
    object For : Token
    object While : Token
    object Return : Token
    object If: Token
    object Else: Token
    object When: Token
}

private val symbolsMap = mapOf(
        "->" to ZcToken.Arrow,
        "=" to ZcToken.Assign,
        ":" to ZcToken.Colon,
        "," to ZcToken.Comma,
        "." to ZcToken.Dot,

        "+" to ZcToken.Plus,
        "++" to ZcToken.PlusPlus,
        "-" to ZcToken.Minus,
        "--" to ZcToken.MinusMinus,
        "*" to ZcToken.Asterisk,
        "/" to ZcToken.Slash,
        "%" to ZcToken.Percent,
        "==" to ZcToken.Equals,
        ">" to ZcToken.Great,
        ">=" to ZcToken.GreatEq,
        "<" to ZcToken.Less,
        "<=" to ZcToken.LessEq,

        "(" to ZcToken.ParenthesisOpen,
        ")" to ZcToken.ParenthesisClose,
        "[" to ZcToken.SquareBracketOpen,
        "]" to ZcToken.SquareBracketClose,
        "{" to ZcToken.CurlyBracketOpen,
        "}" to ZcToken.CurlyBracketClose
)

private val keywordsMap = mapOf(
        "struct" to ZcToken.Struct,
        "fn" to ZcToken.Fun,
        "var" to ZcToken.Var,
        "val" to ZcToken.Val,
        "while" to ZcToken.While,
        "for" to ZcToken.For,
        "return" to ZcToken.Return,
        "if" to ZcToken.If,
        "else" to ZcToken.Else,
        "when" to ZcToken.When
)

class ZcSequenceLexer(sequence: Sequence<Char>) : Lexer by SequenceLexer(
        sequence = sequence,
        symbols = symbolsMap,
        keywords = keywordsMap,
        idStart = { isLetter() },
        idPart = { isLetterOrDigit() || this == '_' }
)