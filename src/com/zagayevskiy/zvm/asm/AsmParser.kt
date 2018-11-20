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
    data class Instruction(val opcode: Opcode, val operands: List<Operand> = emptyList()) : Command() {
        init {
            if (opcode.operandCount != operands.size) throw IllegalArgumentException("""
                operands size must be equals to declared opcode.operandCount
                opcode: $opcode
                operands: $operands
            """.trimIndent())
        }

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

interface Opcode {
    val name: String
    val operandCount: Int
}

class AsmParser(private val lexer: Lexer, supportedOpcodes: Iterable<Opcode>) {

    private val opcodes = supportedOpcodes.map { it.name to it }.toMap()
            .also { map -> if (map.size != supportedOpcodes.count()) throw IllegalArgumentException("OpcodesMapping names must be different") }

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
        val opcodeName = (token as? Identifier)?.name ?: return null
        val opcode = opcodes[opcodeName] ?: error("Unsupported opcode $opcodeName")
        nextToken()

        val operands = instructionArgs()
        if (operands.size != opcode.operandCount) error("Opcode $opcodeName must have ${opcode.operandCount} operand. Has $operands")
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