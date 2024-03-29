package com.zagayevskiy.zvm.zc.ast

import com.zagayevskiy.zvm.util.extensions.zipWithCondition
import com.zagayevskiy.zvm.zc.visitors.AstVisitor
import com.zagayevskiy.zvm.zc.visitors.visit

private class AstEqualityVisitor(private val other: Ast) : AstVisitor<Boolean> {
    override fun visit(ast: AstValInitialization) = ast.check { ast.valToInit == it.valToInit && ast.initializer == it.initializer }

    override fun visit(ast: AstProgram) = ast.check()

    override fun visit(ast: AstFunctionDeclaration) = ast.check { ast.name == it.name && ast.returnType == it.returnType && ast.args == it.args }

    override fun visit(ast: AstStructDeclaration) = ast.check { ast.name == it.name }

    override fun visit(ast: AstDefinedStruct) = ast.check { ast.name == it.name && ast.structType == it.structType }

    override fun visit(ast: AstStructFieldDereference) = ast.check { ast.name == it.name }

    override fun visit(ast: AstUnknownFunctionReference) = ast.check { ast.name == it.name }

    override fun visit(ast: AstSizeOf) = ast.check { ast.unresolvedType == it.unresolvedType }

    override fun visit(ast: AstHardCastExpr) = ast.check { ast.unresolvedCastType == it.unresolvedCastType }

    override fun visit(ast: AstDefinedFunction) = ast.check { ast.args.zipWithCondition(it) { left, right -> left == right } }

    override fun visit(ast: AstConstDeclaration) = ast.check { ast.name == it.name && ast.initializer == it.initializer && ast.declaredType == it.declaredType }

    override fun visit(ast: AstBlock) = ast.check()

    override fun visit(ast: AstStatementList) = ast.check()

    override fun visit(ast: AstAsmBlock) = ast.check { ast.body == it.body }

    override fun visit(ast: AstVarDecl) = ast.check { ast.varName == it.varName && ast.unresolvedType == it.unresolvedType }

    override fun visit(ast: AstValDecl) = ast.check { ast.valName == it.valName && ast.unresolvedType == it.unresolvedType }

    override fun visit(ast: AstForLoop) = ast.check()

    override fun visit(ast: AstWhileLoop) = ast.check()

    override fun visit(ast: AstIfElse) = ast.check()

    override fun visit(ast: AstFunctionReturn) = ast.check()

    override fun visit(ast: AstExpressionStatement) = ast.check()

    override fun visit(ast: AstAssignment) = ast.check()

    override fun visit(ast: AstDisjunction) = ast.check()

    override fun visit(ast: AstConjunction) = ast.check()

    override fun visit(ast: AstBitAnd) = ast.check()

    override fun visit(ast: AstBitOr) = ast.check()

    override fun visit(ast: AstBitXor) = ast.check()

    override fun visit(ast: AstBitShift.Left) = ast.check()

    override fun visit(ast: AstBitShift.Right) = ast.check()

    override fun visit(ast: AstEquals) = ast.check()

    override fun visit(ast: AstNotEquals) = ast.check()

    override fun visit(ast: AstLess) = ast.check()

    override fun visit(ast: AstLessEq) = ast.check()

    override fun visit(ast: AstGreat) = ast.check()

    override fun visit(ast: AstGreatEq) = ast.check()

    override fun visit(ast: AstSum) = ast.check()

    override fun visit(ast: AstDifference) = ast.check()

    override fun visit(ast: AstMul) = ast.check()

    override fun visit(ast: AstDiv) = ast.check()

    override fun visit(ast: AstMod) = ast.check()

    override fun visit(ast: AstVar) = ast.check { ast.name == it.name }

    override fun visit(ast: AstArrayIndexing) = ast.check()

    override fun visit(ast: AstFunctionCall) = ast.check()

    override fun visit(ast: AstConst.Integer) = ast.check()

    override fun visit(ast: AstConst.Byte) = ast.check()

    override fun visit(ast: AstConst.Boolean) = ast.check()

    override fun visit(ast: AstConst.StringLiteral) = ast.check { it.value == ast.value }

    override fun visit(ast: AstConst.Undefined) = ast.check()

    override fun visit(ast: AstConst.Void) = ast.check()

    override fun visit(ast: AstConst.DefaultValue) = ast.check()

    override fun visit(ast: AstLogicalNot) = ast.check()

    override fun visit(ast: AstBitNot) = ast.check()

    override fun visit(ast: AstFunctionArgument) = ast.check { ast.index == it.index && ast.name == it.name }

    override fun visit(ast: AstFunctionReference) = ast.check { ast.function eq it.function }

    override fun visit(ast: AstIdentifier) = ast.check { ast.name == it.name }

    override fun visit(ast: AstVal) = ast.check { ast.name == it.name }

    override fun visit(ast: AstCastExpr) = ast.check()

    override fun visit(ast: AstWhen) = ast.check()

    override fun visit(ast: AstWhenBranch) = ast.check()

    private inline fun <reified T : Ast> T.check(additionalCondition: (other: T) -> Boolean = { true }): Boolean {
        return other is T && zipWithCondition(other) { left, right -> left eq right } && additionalCondition(other)
    }

}

infix fun Ast.eq(other: Ast) = visit(AstEqualityVisitor(other), this)