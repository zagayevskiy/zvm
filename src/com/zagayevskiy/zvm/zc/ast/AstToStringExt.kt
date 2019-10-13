package com.zagayevskiy.zvm.zc.ast

import com.zagayevskiy.zvm.zc.visitors.AstVisitor
import com.zagayevskiy.zvm.zc.visitors.visit

private object AstToStringVisitor : AstVisitor<String> {
    override fun visit(ast: AstProgram) = "program"

    override fun visit(ast: AstFunctionDeclaration) = "fn ${ast.name} : ${ast.returnType}"

    override fun visit(ast: AstStructDeclaration) = "struct ${ast.name}"

    override fun visit(ast: AstDefinedFunction) = "defined fn ${ast.name}"

    override fun visit(ast: AstDefinedStruct) = "defined struct ${ast.name}: ${ast.structType}"

    override fun visit(ast: AstStructFieldDereference) = "field .${ast.name} dereference"

    override fun visit(ast: AstSizeOf) = "sizeof ${ast.unresolvedType}"

    override fun visit(ast: AstHardCastExpr) = "hard cast to ${ast.unresolvedCastType}"

    override fun visit(ast: AstBlock) = "block"

    override fun visit(ast: AstWhen) = "when"

    override fun visit(ast: AstWhenBranch) = "when-branch"

    override fun visit(ast: AstAsmBlock) = "asm ${ast.body}"

    override fun visit(ast: AstVarDecl) = "var decl ${ast.varName}"

    override fun visit(ast: AstValDecl) = "val decl ${ast.valName}"

    override fun visit(ast: AstForLoop) = "for-loop"

    override fun visit(ast: AstWhileLoop) = "while-loop"

    override fun visit(ast: AstIfElse) = "if-else"

    override fun visit(ast: AstFunctionReturn) = "return"

    override fun visit(ast: AstExpressionStatement) = ";"

    override fun visit(ast: AstAssignment) = "="

    override fun visit(ast: AstDisjunction) = "||"

    override fun visit(ast: AstConjunction) = "&&"

    override fun visit(ast: AstBitAnd) = "&"

    override fun visit(ast: AstBitOr) = "|"

    override fun visit(ast: AstBitXor) = "^"

    override fun visit(ast: AstBitShift.Left) = "<<"

    override fun visit(ast: AstBitShift.Right) = ">>"

    override fun visit(ast: AstEquals) = "=="

    override fun visit(ast: AstNotEquals) = "!="

    override fun visit(ast: AstLess) = "<"

    override fun visit(ast: AstLessEq) = "<="

    override fun visit(ast: AstGreat) = ">"

    override fun visit(ast: AstGreatEq) = ">="

    override fun visit(ast: AstSum) = "+"

    override fun visit(ast: AstDifference) = "-"

    override fun visit(ast: AstMul) = "*"

    override fun visit(ast: AstDiv) = "/"

    override fun visit(ast: AstMod) = "%"

    override fun visit(ast: AstVar) = "var(${ast.name}) ${ast.name}"

    override fun visit(ast: AstArrayIndexing) = "[]"

    override fun visit(ast: AstFunctionCall) = "call"

    override fun visit(ast: AstConst.Integer) = "int:${ast.value}"

    override fun visit(ast: AstConst.Byte) = "byte:${ast.value}"

    override fun visit(ast: AstConst.Boolean) = "bool:${ast.value}"

    override fun visit(ast: AstConst.Undefined) = "undefined"

    override fun visit(ast: AstConst.Void) = "void"

    override fun visit(ast: AstConst.DefaultValue) = "default value for ${ast.type}"

    override fun visit(ast: AstLogicalNot) = "!"

    override fun visit(ast: AstBitNot) = "~"

    override fun visit(ast: AstFunctionArgument) = "arg(${ast.index}) ${ast.name}"

    override fun visit(ast: AstFunctionReference) = "fn ref ${ast.function.name}"

    override fun visit(ast: AstIdentifier) = "id ${ast.name}"

    override fun visit(ast: AstVal) = "val(${ast.name}) ${ast.name}"

    override fun visit(ast: AstCastExpr) = "cast"

    override fun visit(ast: AstValInitialization) = "init val"
}

fun Ast.stringValue() = visit(AstToStringVisitor, this)