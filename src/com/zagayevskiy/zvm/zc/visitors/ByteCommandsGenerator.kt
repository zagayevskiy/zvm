package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.util.extensions.toSizePrefixedByteArray
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.scopes.LocalScope
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
                    args = args.map { arg ->
                        Command.Func.Arg(arg.name, arg.type.toAsmType())
                    }
            ))
            generateLocalScope(function) {
                when (val body = body) {
                    is AstBlock -> generate(body)
                    is AstExpr -> TODO("Expression body not supported yet.")
                    else -> error("Unknown function body.")
                }
            }

            if (function.retType is ZcType.Void) {
                commands.add(Ret.instruction())
            }
        }
    }

    private fun ZcType.toAsmType() = when (this) {
        ZcType.Void -> TODO()
        ZcType.Integer -> "int"
        ZcType.Byte -> "byte"
        ZcType.Boolean -> "byte"
        ZcType.Unknown -> TODO()
        is ZcType.Struct -> "int"
        is ZcType.Array -> "int"
        is ZcType.Function -> "int"
    }

    private fun generate(statement: AstStatement) {
        return when (statement) {
            is AstBlock -> generateLocalScope(statement) {
                statements.forEach { child -> generate(child) }
            }
            is AstStatementList -> statement.statements.forEach { child -> generate(child) }
            is AstAsmBlock -> generate(statement)
            is AstVarDecl, is AstValDecl -> error("Variables ($statement) declarations must be resolved before.")
            is AstValInitialization -> Unit.also {
                generate(statement.initializer)
                val offset = statement.valToInit.offset.op
                commands.add(instructionByType(statement.valToInit.type,
                        int = { LocalStoreInt.instruction(offset) },
                        byte = { LocalStoreByte.instruction(offset) }))
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
                if (statement.expression.type !is ZcType.Void) {
                    commands.add(Pop.instruction())
                }
            }
            is AstWhen -> generate(statement)
        }
    }

    private fun <S : LocalScope> generateLocalScope(scope: S, generator: S.() -> Unit) {
        val offset = scope.localsOffset
        val localsSize = offset - ((scope.enclosingScope as? LocalScope)?.localsOffset ?: 0)
        when (localsSize) {
            0 -> generator(scope) //no locals in this scope
            1 -> {
                commands.add(IncStackPointerByte.instruction())
                generator(scope)
                commands.add(DecStackPointerByte.instruction())
            }
            4 -> {
                commands.add(IncStackPointerInt.instruction())
                generator(scope)
                commands.add(DecStackPointerInt.instruction())
            }
            else -> {
                commands.add(AddStackPointer.instruction(localsSize.op))
                generator(scope)
                commands.add(AddStackPointer.instruction((-localsSize).op))
            }
        }
    }

    private fun generate(whenStatement: AstWhen) {
        generate(whenStatement.checkValue)
        val endLabel = "l_${nextId}_when_end"
        val thisWhenId = nextId

        fun branchLabel(index: Int) = "l_${thisWhenId}_branch_$index"

        whenStatement.branches.forEachIndexed { index, branch ->
            val currentBranchLabel = branchLabel(index)
            val nextBranchLabel = branchLabel(index + 1)
            commands.add(Command.Label(currentBranchLabel))
            commands.add(Dup.instruction())
            generate(branch.case)
            commands.add(instructionByType(whenStatement.checkValue.type, IntEq, ByteEq))
            commands.add(JumpZero.instruction(nextBranchLabel.id))
            commands.add(Pop.instruction())
            generate(branch.branch)
            commands.add(Jmp.instruction(endLabel.id))
        }
        val elseLabel = branchLabel(whenStatement.branches.size)
        commands.add(Command.Label(elseLabel))
        commands.add(Pop.instruction())
        generate(whenStatement.elseStatement)
        commands.add(Command.Label(endLabel))


    }

    private fun generate(asm: AstAsmBlock) {
        val asmParser = asmParserFactory(asm.body)
        val asmParseResult = asmParser.program()
        val asmCommands = when (asmParseResult) {
            is ParseResult.Failure -> error("Failed to insert asm: ${asmParseResult.message}")
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
            is AstFunctionReference -> {
                commands.add(IntConst.instruction(expression.function.index.op))
            }
            is AstBinary -> generate(expression)
            is AstIdentifier -> error("All identifiers must be resolved before. Why $expression don't?")
            is AstFunctionArgument -> {
                val name = expression.name.id
                commands.add(instructionByType(expression.type,
                        int = { LocalLoadInt.instruction(name) },
                        byte = { LocalLoadByte.instruction(name) }))
            }
            is AstLocal -> {
                val offset = expression.offset.op
                commands.add(instructionByType(expression.type,
                        int = { LocalLoadInt.instruction(offset) },
                        byte = { LocalLoadByte.instruction(offset) }))
            }
            is AstArrayIndexing -> {
                generate(expression.array)
                generate(expression.index)
                commands.add(IntConst.instruction((expression.array.type as ZcType.Array).itemType.sizeOf.op))
                commands.add(IntMul.instruction())
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
            is AstConst.StringLiteral -> {
                val poolEntryName = "zc_pool_entry@$nextId"
                commands.add(Command.PoolEntry(poolEntryName, expression.value.toSizePrefixedByteArray()))
                commands.add(PushConstantPool.instruction())
                commands.add(IntConst.instruction(poolEntryName.id))
                commands.add(IntAdd.instruction())
            }
            is AstConst.DefaultValue -> commands.add(instructionByType(expression.type,
                    int = { IntConst.instruction(0.op) },
                    byte = { ByteConst.instruction(0.op) }))
            AstConst.Undefined -> TODO("What to do with undefined?")
            AstConst.Void -> TODO("What to do with void?")
            is AstLogicalNot -> {
                generate(expression.expression)
                commands.add(ByteLogicalNot.instruction())
            }
            is AstBitNot -> {
                generate(expression.expression)
                commands.add(instructionByType(expression.type, IntNot, ByteNot))
            }
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
            else -> {
                generate(call.function)
                commands.add(Invoke.instruction())
            }
        }
    }

    private fun generate(assignment: AstAssignment) {
        when (val left = assignment.assignable) {
            is AstVar -> {
                generate(assignment.assignation)
                val offset = left.offset.op

                commands.add(instructionByType(left.type,
                        int = { LocalStoreInt.instruction(offset) },
                        byte = { LocalStoreByte.instruction(offset) }))
            }
            is AstArrayIndexing -> {
                generate(left.array)
                generate(left.index)
                commands.add(IntConst.instruction((left.array.type as ZcType.Array).itemType.sizeOf.op))
                commands.add(IntMul.instruction())
                generate(assignment.assignation)
                commands.add(instructionByType(left.type, MemoryStoreInt, MemoryStoreByte))
            }
            is AstStructFieldDereference -> {
                val field = left.structType.findField(left.name) ?: error("Field must be resolved before.")
                generate(left.structInstance)
                commands.add(IntConst.instruction(field.offset.op))
                generate(assignment.assignation)
                commands.add(instructionByType(left.type, MemoryStoreInt, MemoryStoreByte))
            }
            else -> error("$left isn't lvalue.")
        }
    }

    private fun generate(cast: AstCastExpr) {
        generate(cast.expression)
        val castInstruction = when (cast.expression.type) {
            ZcType.Integer -> when (cast.type) {
                ZcType.Integer, is ZcType.Array, is ZcType.Struct, is ZcType.Function -> null
                ZcType.Byte,
                ZcType.Boolean -> IntToByte.instruction()
                else -> error("${cast.expression} can't be casted to ${cast.type}")
            }
            ZcType.Byte,
            ZcType.Boolean -> when (cast.type) {
                ZcType.Integer -> ByteToInt.instruction()
                ZcType.Byte,
                ZcType.Boolean,
                is ZcType.Array -> null
                else -> error("${cast.expression} can't be casted to ${cast.type}")
            }

            is ZcType.Struct, is ZcType.Array -> null

            is ZcType.Function,
            ZcType.Void,
            ZcType.Unknown -> error("${cast.expression} can't be casted to ${cast.type}")
        }
        castInstruction?.let { commands.add(it) }
    }
}

private fun instructionByType(type: ZcType, int: () -> Command.Instruction, byte: () -> Command.Instruction): Command.Instruction = when (type) {
    ZcType.Integer -> int()
    is ZcType.Array -> int()
    is ZcType.Struct -> int()
    is ZcType.Function -> int()
    ZcType.Byte -> byte()
    ZcType.Boolean -> byte()
    else -> error("Unwanted type $type")
}

private fun instructionByType(type: ZcType, intOpcode: Opcode, byteOpcode: Opcode): Command.Instruction = when (type) {
    ZcType.Integer -> intOpcode.instruction()
    is ZcType.Array -> intOpcode.instruction()
    is ZcType.Struct -> intOpcode.instruction()
    is ZcType.Function -> intOpcode.instruction()
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