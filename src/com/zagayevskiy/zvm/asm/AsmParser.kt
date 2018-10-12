package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.asm.AsmToken.Arrow
import com.zagayevskiy.zvm.asm.AsmToken.Comma
import com.zagayevskiy.zvm.asm.AsmToken.Fun
import com.zagayevskiy.zvm.asm.AsmToken.Minus
import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.common.Token.*

class AsmParser(private val lexer: Lexer) {

    private lateinit var token: Token

    fun program() {
        nextToken()
        command()
        while (token != Eof) {
            while (token == Eol) nextToken()
            command()
        }
    }

    private fun command() {
        when (token) {
            Fun -> func()
            Arrow -> label()
            else -> instruction()
        }
    }

    private fun func() {
        nextToken()
        if (token !is Identifier) error()
    }

    private fun label() {
        nextToken()
        if (token !is Identifier) error()
    }

    private fun instruction() {
        if (token !is Identifier) error()
        nextToken()
        if (operand()) {
            while (token == Comma) {
                nextToken()
                if (!operand()) error()
            }
        }
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

    private fun error() {
        throw IllegalStateException()
    }
}