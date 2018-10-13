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
import com.zagayevskiy.zvm.asm.Command.Instruction.Operand
import com.zagayevskiy.zvm.asm.Command.*

sealed class Command {
    data class Func(val name: String, val args: Int = 0, val locals: Int = 0) : Command()
    data class Label(val label: String) : Command()
    data class Instruction(val opcode: String, val operands: List<Operand>) : Command() {
        sealed class Operand {
            data class Integer(val value: Int) : Operand()
            data class Id(val name: String) : Operand()
        }
    }
}

sealed class ParseResult {
    data class Success(val commands: List<Command>) : ParseResult()
    data class Failure(val line: Int, val message: String, val commands: List<Command>, val exception: ParseException) : ParseResult()
}

class AsmParser(private val lexer: Lexer) {

    private lateinit var token: Token

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
            return ParseResult.Failure(lexer.currentLine, e.message, commands, e)
        }

        return ParseResult.Success(commands)
    }

    private fun command() = (func() ?: label() ?: instruction())?.also { command ->
        commands.add(command)
    }

    private fun func(): Func? {
        if (token != Fun) return null
        nextToken()
        val name = (token as? Identifier)?.name ?: error()
        nextToken()
        var argsCount: Int? = null
        var localsCount: Int? = null
        if (token == AsmToken.Colon) {
            nextToken()
            argsCount = funcArgs()
            if (argsCount != null) {
                if (token == Comma) {
                    nextToken()
                    localsCount = funcLocals() ?: error()
                }
            } else {
                localsCount = funcLocals() ?: error()
            }
        }

        return Func(name, argsCount ?: 0, localsCount ?: 0)
    }

    private fun funcArgs(): Int? {
        if (token != Args) return null
        nextToken()
        if (token != Assign) error()
        nextToken()
        val count = (token as? Integer) ?: error()
        nextToken()
        return count.value

    }

    private fun funcLocals(): Int? {
        if (token != Locals) return null
        nextToken()
        if (token != Assign) error()
        nextToken()
        val count = (token as? Integer) ?: error()
        nextToken()
        return count.value
    }

    private fun label(): Label? {
        if (token != Arrow) return null
        nextToken()
        val label = (token as? Identifier)?.name ?: error()
        nextToken()

        return Label(label)
    }

    private fun instruction(): Instruction? {
        val opcode = (token as? Identifier)?.name ?: return null
        nextToken()

        val operands = instructionArgs()

        return Instruction(opcode, operands)
    }

    private fun instructionArgs(): List<Operand> = mutableListOf<Operand>().apply {
        add(operand() ?: return@apply)
        while (token == Comma) {
            nextToken()
            add(operand() ?: error())
        }
    }

    private fun operand(): Operand? = token.run {
        when (this) {
            is Identifier -> Operand.Id(name)
            is Integer -> Operand.Integer(value)
            is Minus -> {
                nextToken()
                Operand.Integer(-((token as? Integer)?.value ?: error()))
            }
            else -> null
        }
    }?.also { nextToken() }


    private fun nextToken() {
        token = lexer.nextToken().also { currentToken ->
            if (currentToken is Token.Error) {
                error("Lexical error at sequence ${currentToken.sequence}")
            }
        }
    }

    private fun error(message: String = "Syntax error at token $token"): Nothing = throw ParseException(message)
}

class ParseException(override val message: String) : Exception()