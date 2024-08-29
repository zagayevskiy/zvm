package com.zagayevskiy.zvm.zc.ast

import com.zagayevskiy.zvm.zc.types.ZcType
import com.zagayevskiy.zvm.zc.visitors.AstVisitor
import com.zagayevskiy.zvm.zc.visitors.visit

fun Ast.asProgramText() {

}

private class AsProgramTextWalker {
    private val builder = StringBuilder()

    private fun append(s: String) {

    }

    private fun appendLine(line: String)

    fun asProgramText(ast: AstProgram) {
        ast.declarations.forEach(::topLevel)
    }

    private fun topLevel(declaration: TopLevelDeclaration) {
        when (declaration) {
            is AstConstDeclaration -> append("const ${declaration.name}: ${declaration.declaredType} = ${declaration.initializer.asHumanReadable()}")
            is AstDefinedFunction -> definedFunction(declaration)
            is AstDefinedStruct -> definedStruct(declaration)
            is AstFunctionDeclaration -> TODO()
            is AstStructDeclaration -> TODO()
        }
    }

    private fun definedFunction(fn: AstDefinedFunction) {
        append("fn ${fn.name}(${fn.args.map { it.asHumanReadable() }})")
        block(fn.body as AstBlock)
    }

    private fun block(block: AstBlock) {
        appendLine("{")
        block.statements.forEach(::statement)
        appendLine("}")
    }

    private fun statement(statement: AstStatement) {
        when(statement) {
            is AstAsmBlock -> {
                append("""
                    asm {"
                        ${statement.body}
                    "}
                """.trimIndent())
            }
            is AstBlock -> block(statement)
            is AstExpressionStatement -> {
                expression(statement.expression)
                appendLine(";")
            }
            is AstForLoop -> forLoop(statement)
            is AstFunctionReturn -> TODO()
            is AstIfElse -> TODO()
            is AstStatementList -> TODO()
            is AstValDecl -> TODO()
            is AstValInitialization -> TODO()
            is AstVarDecl -> TODO()
            is AstWhen -> TODO()
            is AstWhileLoop -> TODO()
        }
    }

    private fun forLoop(loop: AstForLoop) {
        appendLine("for(${loop.initializer}; ${loop.condition}); ${loop.step}")
        statement(loop.body)
    }

    private fun expression(expr: AstExpr) {
        when(expr) {
            is AstArrayIndexing -> {
                expression(expr.array)
                append("[")
                expression(expr.index)
                append("]")
            }
            is AstAssignment -> {
                expression(expr.assignable)
                append(" = ")
                expression(expr.assignation)
            }
            is AstDifference -> {
                expression()
            }
            is AstDiv -> TODO()
            is AstMod -> TODO()
            is AstMul -> TODO()
            is AstSum -> TODO()
            is AstBitAnd -> TODO()
            is AstBitOr -> TODO()
            is AstBitShift.Left -> TODO()
            is AstBitShift.Right -> TODO()
            is AstBitXor -> TODO()
            is AstEquals -> TODO()
            is AstGreat -> TODO()
            is AstGreatEq -> TODO()
            is AstLess -> TODO()
            is AstLessEq -> TODO()
            is AstNotEquals -> TODO()
            is AstConjunction -> TODO()
            is AstDisjunction -> TODO()
            is AstBitNot -> TODO()
            is AstCastExpr -> TODO()
            AstConst.Boolean.False -> TODO()
            AstConst.Boolean.True -> TODO()
            is AstConst.Byte -> TODO()
            is AstConst.DefaultValue -> TODO()
            is AstConst.Integer -> TODO()
            is AstConst.StringLiteral -> TODO()
            AstConst.Undefined -> TODO()
            AstConst.Void -> TODO()
            is AstFunctionArgument -> TODO()
            is AstFunctionCall -> TODO()
            is AstFunctionReference -> TODO()
            is AstHardCastExpr -> TODO()
            is AstIdentifier -> TODO()
            is AstVal -> TODO()
            is AstVar -> TODO()
            is AstLogicalNot -> TODO()
            is AstSizeOf -> TODO()
            is AstStructFieldDereference -> TODO()
            is AstUnknownFunctionReference -> TODO()
        }
    }

    private fun definedStruct(struct: AstDefinedStruct) {
        val fieldsReadable = struct.structType.fields.joinToString(separator = ";\n") { field ->
            "    var ${field.name}: ${field.type.asHumanReadable()}"
        }
        append(
            """
            struct ${struct.name} {
            $fieldsReadable
            }
        """.trimIndent()
        )
    }

}

private fun AstFunctionArgument.asHumanReadable() = "$name: ${type.asHumanReadable()}"

private fun ZcType.asHumanReadable(): String = when (this) {
    is ZcType.Array -> "[${itemType.asHumanReadable()}]"
    ZcType.Boolean -> "bool"
    ZcType.Byte -> "byte"
    is ZcType.Function -> "(${argTypes.map { it.asHumanReadable() }})->${retType.asHumanReadable()}"
    ZcType.Integer -> "int"
    is ZcType.Struct -> structName
    ZcType.Unknown -> "unknown"
    ZcType.Void -> "void"
}


private fun AstConst.asHumanReadable() = when (this) {
    AstConst.Boolean.False -> "false"
    AstConst.Boolean.True -> "true"
    is AstConst.Byte -> value.toString()
    is AstConst.DefaultValue -> "default"
    is AstConst.Integer -> value.toString()
    is AstConst.StringLiteral -> """"$value""""
    AstConst.Undefined -> "UNDEFINED"
    AstConst.Void -> "void"
}