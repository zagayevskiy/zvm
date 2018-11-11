package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.zc.ast.*

interface AstVisitor<R> {

    fun visit(ast: StubAst): R

    fun visit(ast: AstProgram): R

    fun visit(ast: AstFunctionDeclaration): R

    fun visit(ast: AstStructDeclaration): R

    fun visit(ast: AstDefinedFunction): R

    fun visit(ast: AstBlock): R

    fun visit(ast: AstVarDecl): R

    fun visit(ast: AstValDecl): R

    fun visit(ast: AstLoop): R

    fun visit(ast: AstWhile): R

    fun visit(ast: AstIfElse): R

    fun visit(ast: AstFunctionReturn): R

    fun visit(ast: AstExpressionStatement): R

    fun visit(ast: AstAssignment): R

    fun visit(ast: AstDisjunction): R

    fun visit(ast: AstConjunction): R

    fun visit(ast: AstBitAnd): R

    fun visit(ast: AstBitOr): R

    fun visit(ast: AstBitXor): R

    fun visit(ast: AstBitShift.Left): R

    fun visit(ast: AstBitShift.Right): R

    fun visit(ast: AstEquals): R

    fun visit(ast: AstNotEquals): R

    fun visit(ast: AstLess): R

    fun visit(ast: AstLessEq): R

    fun visit(ast: AstGreat): R

    fun visit(ast: AstGreatEq): R

    fun visit(ast: AstSum): R

    fun visit(ast: AstDifference): R

    fun visit(ast: AstMul): R

    fun visit(ast: AstDiv): R

    fun visit(ast: AstMod): R

    fun visit(ast: AstVariable): R

    fun visit(ast: AstArrayIndexing): R

    fun visit(ast: AstFunctionCall): R

    fun visit(ast: AstConst.Integer): R

    fun visit(ast: AstConst.Byte): R

    fun visit(ast: AstConst.Boolean): R

    fun visit(ast: AstConst.Undefined): R

    fun visit(ast: AstConst.Void): R

    fun visit(ast: AstLogicalNot): R

    fun visit(ast: AstBitNot): R
}

fun <R> visit(visitor: AstVisitor<R>, ast: Ast) = when (ast) {
    is StubAst -> visitor.visit(ast)
    is AstProgram -> visitor.visit(ast)
    is AstFunctionDeclaration -> visitor.visit(ast)
    is AstStructDeclaration -> visitor.visit(ast)
    is AstDefinedFunction -> visitor.visit(ast)
    is AstBlock -> visitor.visit(ast)
    is AstVarDecl -> visitor.visit(ast)
    is AstValDecl -> visitor.visit(ast)
    is AstLoop -> visitor.visit(ast)
    is AstWhile -> visitor.visit(ast)
    is AstIfElse -> visitor.visit(ast)
    is AstFunctionReturn -> visitor.visit(ast)
    is AstExpressionStatement -> visitor.visit(ast)
    is AstAssignment -> visitor.visit(ast)
    is AstDisjunction -> visitor.visit(ast)
    is AstConjunction -> visitor.visit(ast)
    is AstBitAnd -> visitor.visit(ast)
    is AstBitOr -> visitor.visit(ast)
    is AstBitXor -> visitor.visit(ast)
    is AstBitShift.Left -> visitor.visit(ast)
    is AstBitShift.Right -> visitor.visit(ast)
    is AstEquals -> visitor.visit(ast)
    is AstNotEquals -> visitor.visit(ast)
    is AstLess -> visitor.visit(ast)
    is AstLessEq -> visitor.visit(ast)
    is AstGreat -> visitor.visit(ast)
    is AstGreatEq -> visitor.visit(ast)
    is AstSum -> visitor.visit(ast)
    is AstDifference -> visitor.visit(ast)
    is AstMul -> visitor.visit(ast)
    is AstDiv -> visitor.visit(ast)
    is AstMod -> visitor.visit(ast)
    is AstVariable -> visitor.visit(ast)
    is AstArrayIndexing -> visitor.visit(ast)
    is AstFunctionCall -> visitor.visit(ast)
    is AstConst.Integer -> visitor.visit(ast)
    is AstConst.Byte -> visitor.visit(ast)
    is AstConst.Boolean -> visitor.visit(ast)
    is AstConst.Undefined -> visitor.visit(ast)
    is AstConst.Void -> visitor.visit(ast)
    is AstLogicalNot -> visitor.visit(ast)
    is AstBitNot -> visitor.visit(ast)
}