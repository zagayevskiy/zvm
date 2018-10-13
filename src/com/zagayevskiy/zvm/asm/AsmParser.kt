package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.asm.AsmToken.Args
import com.zagayevskiy.zvm.asm.AsmToken.Arrow
import com.zagayevskiy.zvm.asm.AsmToken.Assign
import com.zagayevskiy.zvm.asm.AsmToken.Comma
import com.zagayevskiy.zvm.asm.AsmToken.Fun
import com.zagayevskiy.zvm.asm.AsmToken.Locals
import com.zagayevskiy.zvm.asm.AsmToken.Minus
import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.common.Token.*

sealed class Command

data class Function(val name: String, val address: Int, val args: Int = 0, val locals: Int = 0) : Command()

sealed class ParseResult {
    data class Success(val commands: List<Command>) : ParseResult()
    data class Failure(val line: Int, val message: String) : ParseResult()
}

class AsmParser(private val lexer: Lexer) {

    private lateinit var token: Token

    private val ip = 0

    private val commands = mutableListOf<Command>()

    fun program(): ParseResult {
        try {
            nextToken()
            command()
            while (token != Eof) {
                if (token != Eol) error()
                while (token == Eol) nextToken()
                command()
            }
        } catch (e: ParseException) {
            return ParseResult.Failure(lexer.currentLine, e.message)
        }

        return ParseResult.Success(commands)
    }

    private fun command() = func() || label() || instruction()

    private fun func(): Boolean {
        if (token != Fun) return false
        nextToken()
        val name = (token as? Identifier)?.name ?: error()
        nextToken()
        var argsCount: Int? = null
        var localsCount: Int? = null
        if (token == AsmToken.Colon) {
            nextToken()
            argsCount = args()
            if (argsCount != null) {
                if (token == Comma) {
                    nextToken()
                    localsCount = locals() ?: error()
                }
            } else {
                localsCount = locals() ?: error()
            }
        }
        commands.add(Function(name, ip, argsCount ?: 0, localsCount ?: 0))
        return true
    }

    private fun args(): Int? {
        if (token != Args) return null
        nextToken()
        if (token != Assign) error()
        nextToken()
        val count = (token as? Integer) ?: error()
        nextToken()
        return count.value

    }

    private fun locals(): Int? {
        if (token != Locals) return null
        nextToken()
        if (token != Assign) error()
        nextToken()
        val count = (token as? Integer) ?: error()
        nextToken()
        return count.value
    }

    private fun label(): Boolean {
        if (token != Arrow) return false
        nextToken()
        if (token !is Identifier) error()
        nextToken()

        return true
    }

    private fun instruction(): Boolean {
        if (token !is Identifier) return false
        nextToken()
        if (operand()) {
            while (token == Comma) {
                nextToken()
                if (!operand()) error()
            }
        }

        return true
    }

    private fun operand(): Boolean {
        return when (token) {
            is Identifier -> {
                nextToken()
                true
            }
            is Integer -> {
                nextToken()
                true
            }
            is Minus -> {
                nextToken()
                if (token !is Integer) error()
                nextToken()
                true
            }
            else -> false
        }
    }


    private fun nextToken() {
        token = lexer.nextToken().also { currentToken ->
            if (currentToken is Token.Error) {
                error("Lexical error at sequence ${currentToken.sequence}")
            }
        }
    }

    private fun error(message: String = "Syntax error at token $token"): Nothing = throw ParseException(message)
}

private class ParseException(override val message: String) : Exception()