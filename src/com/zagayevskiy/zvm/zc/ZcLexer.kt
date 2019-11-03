package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.SequenceLexer
import com.zagayevskiy.zvm.common.Token

object ZcToken {
    object Arrow : Token
    object Assign : Token
    object Colon : Token
    object QuadDot: Token
    object Comma : Token
    object Dot : Token
    object Semicolon : Token

    object Plus : Token
    object PlusPlus : Token
    object Minus : Token
    object MinusMinus : Token
    object Asterisk : Token
    object Slash : Token
    object Percent : Token

    object Disjunction : Token
    object Conjunction : Token
    object LogicalNot : Token

    object BitOr : Token
    object BitXor : Token
    object BitAnd : Token
    object BitNot : Token
    object BitShiftLeft : Token
    object BitShiftRight : Token

    object Equals : Token
    object NotEquals : Token
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

    object Asm : Token
    object Struct : Token
    object Fun : Token
    object Var : Token
    object Val : Token
    object For : Token
    object While : Token
    object Return : Token
    object If : Token
    object Else : Token
    object When : Token
    object SizeOf: Token
    object Cast: Token
    object Const: Token
}

private val symbolsMap = mapOf(
        "->" to ZcToken.Arrow,
        "=" to ZcToken.Assign,
        ":" to ZcToken.Colon,
        "::" to ZcToken.QuadDot,
        "," to ZcToken.Comma,
        "." to ZcToken.Dot,
        ";" to ZcToken.Semicolon,

        "+" to ZcToken.Plus,
        "++" to ZcToken.PlusPlus,
        "-" to ZcToken.Minus,
        "--" to ZcToken.MinusMinus,
        "*" to ZcToken.Asterisk,
        "/" to ZcToken.Slash,
        "%" to ZcToken.Percent,

        "||" to ZcToken.Disjunction,
        "&&" to ZcToken.Conjunction,
        "!" to ZcToken.LogicalNot,

        "|" to ZcToken.BitOr,
        "&" to ZcToken.BitAnd,
        "^" to ZcToken.BitXor,
        "~" to ZcToken.BitNot,
        "<<" to ZcToken.BitShiftLeft,
        ">>" to ZcToken.BitShiftRight,

        "==" to ZcToken.Equals,
        "!=" to ZcToken.NotEquals,
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
        "asm" to ZcToken.Asm,
        "struct" to ZcToken.Struct,
        "fn" to ZcToken.Fun,
        "var" to ZcToken.Var,
        "val" to ZcToken.Val,
        "while" to ZcToken.While,
        "for" to ZcToken.For,
        "return" to ZcToken.Return,
        "if" to ZcToken.If,
        "else" to ZcToken.Else,
        "when" to ZcToken.When,
        "sizeof" to ZcToken.SizeOf,
        "cast" to ZcToken.Cast,
        "const" to ZcToken.Const
)

class ZcSequenceLexer(sequence: Sequence<Char>) : Lexer by SequenceLexer(
        sequence = sequence,
        symbols = symbolsMap,
        keywords = keywordsMap,
        idStart = { isLetter() },
        idPart = { isLetterOrDigit() || this == '_' },
        eolAsToken = false,
        stringConstDelimitator = { it.takeIf { limiter -> limiter == '"' || limiter == '\'' } }
)