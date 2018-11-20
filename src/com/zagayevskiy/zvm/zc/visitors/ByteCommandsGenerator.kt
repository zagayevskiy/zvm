package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.zc.ZcToken
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.ZcType

class ByteCommandsGenerator(private val program: AstProgram) {

    private val commands = mutableListOf<Command>()
    private var nextId: Int = 0
        get() = field++

    fun generate(): List<Command> {
        program.declarations.forEach { topLevelSymbol ->
            return@forEach when (topLevelSymbol) {

                is AstFunctionDeclaration -> error("All functions must be resolved before")
                is AstStructDeclaration -> TODO()
                is AstDefinedFunction -> generate(topLevelSymbol)
            }
        }

        return commands
    }

    private fun generate(function: AstDefinedFunction) {
        function.apply {
            commands.add(Command.Func(
                    name = name,
                    args = args.size,
                    locals = totalVariablesCount))
            return@apply when (val body = body) {
                is AstBlock -> generate(body)
                is AstExpr -> TODO("Expression body not supported yet.")
                else -> error("Unknown function body.")
            }
        }
    }


    private fun generate(block: AstBlock) {
        block.statements.forEach { statement -> generate(statement) }
    }

    private fun generate(statement: AstStatement) {
        when (statement) {
            is AstVarDecl, is AstValDecl -> error("Variables declarations must be resolved before. $statement")
            is AstLoop -> TODO()
            is AstWhile -> TODO()
            is AstIfElse -> generate(statement)
            is AstFunctionReturn -> {
                generate(statement.expression)
                commands.add(Ret.instruction())
            }
            is AstExpressionStatement -> {
                generate(statement)
                commands.add(Pop.instruction())
            }
        }
    }

    private fun generate(ifElse: AstIfElse) {
        generate(ifElse.condition)
        val labelElse = "else_label_$nextId"
        val labelEnd = "end_label_$nextId"
        commands.add(JumpZero.instruction(labelElse.id))
        generate(ifElse.ifBody)
        commands.add(Jmp.instruction(labelEnd.id))
        commands.add(Command.Label(labelElse))
        generate(ifElse.elseBody)
        commands.add(Command.Label(labelEnd))


    }

    private fun generate(expression: AstExpr) {
        when(expression) {
            is AstFunctionReference -> TODO()
            is AstAssignment -> {
                generate(expression.left)
                generate(expression.right)
                @Suppress("WhenWithOnlyElse")
                when(expression.left) {
                    else -> TODO()
                }
            }
            is AstBinary -> generate(expression)
            is AstIdentifier -> TODO()
            is AstVar -> TODO()
            is AstVal -> TODO()
            is AstArrayIndexing -> TODO()
            is AstFunctionCall -> TODO()
            is AstFunctionArgument -> TODO()
            is AstConst.Integer -> commands.add(IntConst.instruction(expression.value.op))
            is AstConst.Byte -> TODO()
            is AstConst.Boolean -> TODO()
            AstConst.Undefined -> TODO()
            AstConst.Void -> TODO()
            is AstLogicalNot -> TODO()
            is AstBitNot -> commands.add(instructionByType(expression.type, IntNot, ByteNot))
            is AstCastExpr -> TODO()
        }
    }

    private fun generate(binary: AstBinary) {
        generate(binary.left)
        generate(binary.right)

        commands.add(when(binary) {
            is AstAssignment -> TODO()
            is AstDisjunction -> TODO()
            is AstConjunction -> TODO()
            is AstBitAnd -> instructionByType(binary.type, IntAnd, ByteAnd)
            is AstBitOr -> instructionByType(binary.type, IntOr, ByteOr)
            is AstBitXor -> instructionByType(binary.type, IntXor, ByteXor)
            is AstBitShift.Left -> TODO()
            is AstBitShift.Right -> TODO()
            is AstEquals -> TODO()
            is AstNotEquals -> TODO()
            is AstLess -> TODO()
            is AstLessEq -> TODO()
            is AstGreat -> TODO()
            is AstGreatEq -> TODO()
            is AstSum -> instructionByType(binary.type, IntAdd, ByteAdd)
            is AstDifference -> TODO()
            is AstMul -> instructionByType(binary.type, IntMul, ByteMul)
            is AstDiv -> instructionByType(binary.type, IntDiv, ByteDiv)
            is AstMod -> instructionByType(binary.type, IntMod, ByteMod)
        })
    }

    private fun instructionByType(type: ZcType, intOpcode: Opcode, byteOpcode: Opcode): Command.Instruction = when(type){
        ZcType.Integer -> intOpcode.instruction()
        ZcType.Byte -> byteOpcode.instruction()
        else -> error("Unwanted type") //TODO
    }
}

private fun Opcode.instruction(vararg operands: Command.Instruction.Operand) = Command.Instruction(this, listOf(*operands))
        .also { assert(it.opcode.operandCount == it.operands.size) }
private val Int.op
        get() = Command.Instruction.Operand.Integer(this)
private val String.id
        get() = Command.Instruction.Operand.Id(this)