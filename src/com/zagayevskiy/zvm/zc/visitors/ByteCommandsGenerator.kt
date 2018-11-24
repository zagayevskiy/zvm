package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.asm.*
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

    private fun generate(statement: AstStatement) {
        when (statement) {
            is AstBlock -> statement.statements.forEach { child -> generate(child) }
            is AstVarDecl, is AstValDecl -> error("Variables declarations must be resolved before. $statement")
            is AstForLoop -> generate(statement)
            is AstWhileLoop -> generate(statement)
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

    private fun generate(loop: AstForLoop) {
        val conditionLabel = "l_${nextId}_for_condition"
        val endLabel = "l_${nextId}_for_end"

        generate(loop.initializer)
        commands.add(Command.Label(conditionLabel))
        generate(loop.condition)
        commands.add(JumpZero.instruction(endLabel.id))
        generate(loop.body)
        generate(loop.step)
        commands.add(Jmp.instruction(conditionLabel.id))
        commands.add(Command.Label(endLabel))
    }

    private fun generate(loop: AstWhileLoop) {
        val conditionLabel = "l_${nextId}_while_condition"
        val endLabel = "l_${nextId}_while_end"

        commands.add(Command.Label(conditionLabel))
        generate(loop.condition)
        commands.add(JumpZero.instruction(endLabel.id))
        generate(loop.body)
        commands.add(Jmp.instruction(conditionLabel.id))
        commands.add(Command.Label(endLabel))
    }

    private fun generate(ifElse: AstIfElse) {
        generate(ifElse.condition)
        val beforeElse = "l_${nextId}_before__else"
        val afterElse = "l_${nextId}_after_else"
        commands.add(JumpZero.instruction(beforeElse.id))
        generate(ifElse.ifBody)
        commands.add(Jmp.instruction(afterElse.id))
        commands.add(Command.Label(beforeElse))
        generate(ifElse.elseBody)
        commands.add(Command.Label(afterElse))
    }

    private fun generate(expression: AstExpr) {
        when (expression) {
            is AstFunctionReference -> TODO()
            is AstBinary -> generate(expression)
            is AstIdentifier -> error("All identifiers must be resolved before. Why $expression don't?")
            is AstVar -> TODO()
            is AstVal -> TODO()
            is AstArrayIndexing -> {
                generate(expression.array)
                generate(expression.index)
                commands.add(instructionByType(expression.type, MemoryLoadInt, MemoryLoadByte))
            }
            is AstFunctionCall -> TODO()
            is AstFunctionArgument -> TODO()
            is AstConst.Integer -> commands.add(IntConst.instruction(expression.value.op))
            is AstConst.Byte -> TODO()
            is AstConst.Boolean -> TODO()
            AstConst.Undefined -> TODO()
            AstConst.Void -> TODO()
            is AstLogicalNot -> commands.add(ByteLogicalNot.instruction())
            is AstBitNot -> commands.add(instructionByType(expression.type, IntNot, ByteNot))
            is AstCastExpr -> TODO()
        }
    }

    private fun generate(binary: AstBinary) {
        if (binary is AstAssignment) {

            when (val left = binary.left) {
                is AstVar -> {
                    generate(binary.right)
                    TODO("check type")
                    commands.add(LocalStoreInt.instruction(left.varIndex.op))
                }
                is AstArrayIndexing -> {
                    generate(left)
                    generate(binary.right)
                    TODO("check type")
                    commands.add(MemoryStoreInt.instruction())

                }
            }
            return
        }

        generate(binary.left)
        generate(binary.right)

        commands.add(when (binary) {
            is AstAssignment -> error("Wtf, we check it earlier.")
            is AstDisjunction -> ByteOr.instruction()
            is AstConjunction -> ByteAnd.instruction()
            is AstBitAnd -> instructionByType(binary.type, IntAnd, ByteAnd)
            is AstBitOr -> instructionByType(binary.type, IntOr, ByteOr)
            is AstBitXor -> instructionByType(binary.type, IntXor, ByteXor)
            is AstBitShift.Left -> IntShl.instruction()
            is AstBitShift.Right -> IntShr.instruction()
            is AstEquals -> instructionByType(binary.type, IntCmp, ByteCmp)
            is AstNotEquals -> TODO()
            is AstLess -> instructionByType(binary.type, IntLess, ByteLess)
            is AstLessEq -> instructionByType(binary.type, IntLessEq, ByteLessEq)
            is AstGreat -> instructionByType(binary.type, IntGreater, ByteGreater)
            is AstGreatEq -> instructionByType(binary.type, IntGreaterEq, ByteGreaterEq)
            is AstSum -> instructionByType(binary.type, IntAdd, ByteAdd)
            is AstDifference -> instructionByType(binary.type, IntSub, ByteSub)
            is AstMul -> instructionByType(binary.type, IntMul, ByteMul)
            is AstDiv -> instructionByType(binary.type, IntDiv, ByteDiv)
            is AstMod -> instructionByType(binary.type, IntMod, ByteMod)
        })
    }

    private fun instructionByType(type: ZcType, intOpcode: Opcode, byteOpcode: Opcode): Command.Instruction = when (type) {
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