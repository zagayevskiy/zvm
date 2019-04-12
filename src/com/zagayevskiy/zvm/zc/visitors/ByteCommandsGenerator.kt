package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.zc.ZcToken
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.ZcType

class ByteCommandsGenerator(private val program: AstProgram, private val asmParserFactory: (String) -> AsmParser) {

    private val commands = mutableListOf<Command>()
    private var nextId: Int = 0
        get() = field++

    fun generate(): List<Command> {
        program.declarations.forEach { topLevelSymbol ->
            return@forEach when (topLevelSymbol) {
                is AstFunctionDeclaration -> error("All functions must be resolved before")
                is AstStructDeclaration, is AstDefinedStruct -> {
                }//TODO("Structs not supported yet.")
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
        return when (statement) {
            is AstBlock -> statement.statements.forEach { child -> generate(child) }
            is AstAsmBlock -> generate(statement)
            is AstVarDecl, is AstValDecl -> error("Variables ($statement) declarations must be resolved before.")
            is AstValInitialization -> Unit.also {
                generate(statement.initializer)
                val index = statement.valToInit.valIndex.op
                commands.add(instructionByType(statement.valToInit.type,
                        int = { LocalStoreInt.instruction(index) },
                        byte = { LocalStoreByte.instruction(index) }))
            }
            is AstForLoop -> generate(statement)
            is AstWhileLoop -> generate(statement)
            is AstIfElse -> generate(statement)
            is AstFunctionReturn -> Unit.also {
                generate(statement.expression)
                commands.add(Ret.instruction())
            }
            is AstExpressionStatement -> Unit.also {
                generate(statement.expression)
                commands.add(Pop.instruction())
            }
        }
    }

    private fun generate(asm: AstAsmBlock) {
        val asmParser = asmParserFactory(asm.body)
        val asmParseResult = asmParser.program()
        val asmCommands = when (asmParseResult) {
            is ParseResult.Failure -> error("Failed to insert asm ${asmParseResult.message}")
            is ParseResult.Success -> asmParseResult.commands
        }
        commands.addAll(asmCommands)
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
            is AstFunctionReference -> TODO("Dynamic function references not implemented yet.")
            is AstBinary -> generate(expression)
            is AstIdentifier -> error("All identifiers must be resolved before. Why $expression don't?")
            is AstFunctionArgument -> {
                val index = expression.index.op
                commands.add(instructionByType(expression.type,
                        int = { ArgLoadInt.instruction(index) },
                        byte = { ArgLoadByte.instruction(index) }))
            }
            is AstVar -> {
                val index = expression.varIndex.op
                commands.add(instructionByType(expression.type,
                        int = { LocalLoadInt.instruction(index) },
                        byte = { LocalLoadByte.instruction(index) }))
            }
            is AstVal -> {
                val index = expression.valIndex.op
                commands.add(instructionByType(expression.type,
                        int = { LocalLoadInt.instruction(index) },
                        byte = { LocalLoadByte.instruction(index) }))
            }
            is AstArrayIndexing -> {
                generate(expression.array)
                generate(expression.index)
                commands.add(instructionByType(expression.type, MemoryLoadInt, MemoryLoadByte))
            }
            is AstStructFieldDereference -> {
                val resolvedField = expression.structType.findField(expression.name) ?: error("Field must be resolved before.")
                generate(expression.structInstance)
                commands.add(IntConst.instruction(resolvedField.offset.op))
                commands.add(instructionByType(expression.type, MemoryLoadInt, MemoryLoadByte))
            }

            is AstAssignment -> generate(expression)
            is AstFunctionCall -> generate(expression)
            is AstConst.Integer -> commands.add(IntConst.instruction(expression.value.op))
            is AstConst.Byte -> commands.add(ByteConst.instruction(expression.value.op))
            is AstConst.Boolean -> commands.add(ByteConst.instruction((if (expression.value) 1 else 0).op))
            AstConst.Undefined -> TODO("What to do with undefined?")
            AstConst.Void -> TODO("What to do with void?")
            is AstLogicalNot -> commands.add(ByteLogicalNot.instruction())
            is AstBitNot -> commands.add(instructionByType(expression.type, IntNot, ByteNot))
            is AstCastExpr -> generate(expression)
        }
    }

    private fun generate(binary: AstBinary) {
        generate(binary.left)
        generate(binary.right)

        commands.add(when (binary) {
            is AstDisjunction -> ByteOr.instruction()
            is AstConjunction -> ByteAnd.instruction()
            is AstBitAnd -> instructionByType(binary.type, IntAnd, ByteAnd)
            is AstBitOr -> instructionByType(binary.type, IntOr, ByteOr)
            is AstBitXor -> instructionByType(binary.type, IntXor, ByteXor)
            is AstBitShift.Left -> IntShl.instruction()
            is AstBitShift.Right -> IntShr.instruction()
            is AstEquals -> instructionByType(binary.left.type, IntEq, ByteEq)
            is AstNotEquals -> instructionByType(binary.left.type, IntNotEq, ByteNotEq)
            is AstLess -> instructionByType(binary.left.type, IntLess, ByteLess)
            is AstLessEq -> instructionByType(binary.left.type, IntLessEq, ByteLessEq)
            is AstGreat -> instructionByType(binary.left.type, IntGreater, ByteGreater)
            is AstGreatEq -> instructionByType(binary.left.type, IntGreaterEq, ByteGreaterEq)
            is AstSum -> instructionByType(binary.type, IntAdd, ByteAdd)
            is AstDifference -> instructionByType(binary.type, IntSub, ByteSub)
            is AstMul -> instructionByType(binary.type, IntMul, ByteMul)
            is AstDiv -> instructionByType(binary.type, IntDiv, ByteDiv)
            is AstMod -> instructionByType(binary.type, IntMod, ByteMod)
        })
    }

    private fun generate(call: AstFunctionCall) {
        call.params.forEach { expr -> generate(expr) }
        when (val ref = call.function) {
            is AstFunctionReference -> {
                commands.add(Call.instruction(ref.function.name.id))
            }
            else -> TODO("Dynamic calls not implemented yet.")
        }
    }

    private fun generate(assignment: AstAssignment) {
        when (val left = assignment.assignable) {
            is AstVar -> {
                generate(assignment.assignation)
                commands.add(Dup.instruction())
                val index = left.varIndex.op

                commands.add(instructionByType(left.type,
                        int = { LocalStoreInt.instruction(index) },
                        byte = { LocalStoreByte.instruction(index) }))
            }
            is AstArrayIndexing -> {
                generate(left.array)
                generate(left.index)
                generate(assignment.assignation)
                commands.add(Dup.instruction()) //FIXME may be we don't need to do that?
                commands.add(instructionByType(left.type, MemoryStoreInt, MemoryStoreByte))
            }
            is AstStructFieldDereference -> {
                val field = left.structType.findField(left.name) ?: error("Field must be resolved before.")
                generate(left.structInstance)
                commands.add(IntConst.instruction(field.offset.op))
                generate(assignment.assignation)
                commands.add(instructionByType(left.type, MemoryStoreInt, MemoryStoreByte))
            }
        }
    }

    private fun generate(cast: AstCastExpr) {
        generate(cast.expression)
        when (cast.expression.type) {
            ZcType.Integer -> when (cast.type) {
                ZcType.Integer -> Unit
                ZcType.Byte,
                ZcType.Boolean -> IntToByte.instruction()
                else -> error("${cast.expression} can't be casted to ${cast.type}")
            }
            ZcType.Byte,
            ZcType.Boolean -> when (cast.type) {
                ZcType.Integer -> ByteToInt.instruction()
                ZcType.Byte,
                ZcType.Boolean -> Unit
                else -> error("${cast.expression} can't be casted to ${cast.type}")
            }

            is ZcType.Array -> Unit

            ZcType.Void,
            ZcType.Unknown -> error("${cast.expression} can't be casted to ${cast.type}")
        }
    }
}

private fun instructionByType(type: ZcType, int: () -> Command.Instruction, byte: () -> Command.Instruction): Command.Instruction = when (type) {
    ZcType.Integer -> int()
    is ZcType.Array -> int()
    is ZcType.Struct -> int()
    ZcType.Byte -> byte()
    ZcType.Boolean -> byte()
    else -> error("Unwanted type $type")
}

private fun instructionByType(type: ZcType, intOpcode: Opcode, byteOpcode: Opcode): Command.Instruction = when (type) {
    ZcType.Integer -> intOpcode.instruction()
    is ZcType.Array -> intOpcode.instruction()
    ZcType.Byte -> byteOpcode.instruction()
    ZcType.Boolean -> byteOpcode.instruction()
    else -> error("Unwanted type $type")
}

private fun Opcode.instruction(vararg operands: Command.Instruction.Operand) = Command.Instruction(this, listOf(*operands))
        .also { assert(it.opcode.operandCount == it.operands.size) }

private val Int.op
    get() = Command.Instruction.Operand.Integer(this)
private val Byte.op
    get() = Command.Instruction.Operand.Integer(toInt())
private val String.id
    get() = Command.Instruction.Operand.Id(this)