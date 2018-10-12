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

data class Function(val name: String, val address: Int, val args: Int = 0, val locals: Int = 0)

class AsmParser(private val lexer: Lexer) {

    private lateinit var token: Token

    private val ip = 0

    fun program() {
        nextToken()
        command()
        while (token != Eof) {
            if (token != Eol) error()
            while (token == Eol) nextToken()
            command()
        }
    }

    private fun command() = func() || label() || instruction()

    private fun func(): Boolean {
        if (token != Fun) return false
        nextToken()
        val name = (token as? Identifier)?.name ?: return error()
        nextToken()
        if (token == AsmToken.Colon) {
            nextToken()
            if (args()) {
                if (token == Comma) {
                    nextToken()
                    if (!locals()) error()
                }
            } else if (!locals()) {
                error()
            }
        }
        return true
    }

    private fun args(): Boolean {
        if (token != Args) return false
        nextToken()
        if (token != Assign) error()
        nextToken()
        if (token !is Integer) error()
        nextToken()
        return true

    }

    private fun locals(): Boolean {
        if (token != Locals) return false
        nextToken()
        if (token != Assign) error()
        nextToken()
        if (token !is Integer) error()
        nextToken()
        return true
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
        token = lexer.nextToken()
    }

    private fun error(): Nothing {
        throw IllegalStateException()
    }
}