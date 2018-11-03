package com.zagayevskiy.zvm.c

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.SequenceLexer
import com.zagayevskiy.zvm.common.Token

object CToken {
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
        "->" to CToken.Arrow,
        "=" to CToken.Assign,
        ":" to CToken.Colon,
        "," to CToken.Comma,
        "." to CToken.Dot,

        "+" to CToken.Plus,
        "++" to CToken.PlusPlus,
        "-" to CToken.Minus,
        "--" to CToken.MinusMinus,
        "*" to CToken.Asterisk,
        "/" to CToken.Slash,
        "%" to CToken.Percent,
        "==" to CToken.Equals,
        ">" to CToken.Great,
        ">=" to CToken.GreatEq,
        "<" to CToken.Less,
        "<=" to CToken.LessEq,

        "(" to CToken.ParenthesisOpen,
        ")" to CToken.ParenthesisClose,
        "[" to CToken.SquareBracketOpen,
        "]" to CToken.SquareBracketClose,
        "{" to CToken.CurlyBracketOpen,
        "}" to CToken.CurlyBracketClose
)

private val keywordsMap = mapOf(
        "fn" to CToken.Fun,
        "var" to CToken.Var,
        "val" to CToken.Val,
        "while" to CToken.While,
        "for" to CToken.For,
        "return" to CToken.Return,
        "if" to CToken.If,
        "else" to CToken.Else,
        "when" to CToken.When
)

class CSequenceLexer(sequence: Sequence<Char>) : Lexer by SequenceLexer(
        sequence = sequence,
        symbols = symbolsMap,
        keywords = keywordsMap,
        idStart = { isLetter() },
        idPart = { isLetterOrDigit() || this == '_' }
)