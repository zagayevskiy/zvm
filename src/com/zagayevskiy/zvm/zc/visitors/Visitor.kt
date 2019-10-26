package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.zc.ast.*
import java.lang.IllegalStateException
import java.lang.RuntimeException

interface AstVisitor<R> {

    fun visit(ast: AstProgram): R

    fun visit(ast: AstFunctionDeclaration): R

    fun visit(ast: AstStructDeclaration): R

    fun visit(ast: AstDefinedFunction): R

    fun visit(ast: AstBlock): R

    fun visit(ast: AstStatementList): R

    fun visit(ast: AstAsmBlock): R

    fun visit(ast: AstVarDecl): R

    fun visit(ast: AstValDecl): R

    fun visit(ast: AstForLoop): R

    fun visit(ast: AstWhileLoop): R

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

    fun visit(ast: AstVar): R

    fun visit(ast: AstArrayIndexing): R

    fun visit(ast: AstFunctionCall): R

    fun visit(ast: AstConst.Integer): R

    fun visit(ast: AstConst.Byte): R

    fun visit(ast: AstConst.Boolean): R

    fun visit(ast: AstConst.StringLiteral): R

    fun visit(ast: AstConst.Undefined): R

    fun visit(ast: AstConst.Void): R

    fun visit(ast: AstConst.DefaultValue): R

    fun visit(ast: AstLogicalNot): R

    fun visit(ast: AstBitNot): R
    fun visit(ast: AstFunctionArgument): R
    fun visit(ast: AstFunctionReference): R
    fun visit(ast: AstIdentifier): R
    fun visit(ast: AstVal): R
    fun visit(ast: AstCastExpr): R
    fun visit(ast: AstValInitialization): R
    fun visit(ast: AstDefinedStruct): R
    fun visit(ast: AstStructFieldDereference): R
    fun visit(ast: AstUnknownFunctionReference): R
    fun visit(ast: AstSizeOf): R
    fun visit(ast: AstHardCastExpr): R
    fun visit(ast: AstWhen): R
    fun visit(ast: AstWhenBranch): R
}

fun <R> visit(visitor: AstVisitor<R>, ast: Ast) = when (ast) {
    is AstProgram -> visitor.visit(ast)
    is AstFunctionDeclaration -> visitor.visit(ast)
    is AstStructDeclaration -> visitor.visit(ast)
    is AstDefinedFunction -> visitor.visit(ast)
    is AstAsmBlock -> visitor.visit(ast)
    is AstBlock -> visitor.visit(ast)
    is AstStatementList -> visitor.visit(ast)
    is AstVarDecl -> visitor.visit(ast)
    is AstValDecl -> visitor.visit(ast)
    is AstLocal -> when (ast) {
        is AstVar -> visitor.visit(ast)
        is AstVal -> visitor.visit(ast)
    }
    is AstForLoop -> visitor.visit(ast)
    is AstWhileLoop -> visitor.visit(ast)
    is AstIfElse -> visitor.visit(ast)
    is AstSizeOf -> visitor.visit(ast)
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
    is AstArrayIndexing -> visitor.visit(ast)
    is AstFunctionCall -> visitor.visit(ast)
    is AstConst.Integer -> visitor.visit(ast)
    is AstConst.Byte -> visitor.visit(ast)
    is AstConst.Boolean -> visitor.visit(ast)
    is AstConst.StringLiteral -> visitor.visit(ast)
    is AstConst.Undefined -> visitor.visit(ast)
    is AstConst.Void -> visitor.visit(ast)
    is AstConst.DefaultValue -> visitor.visit(ast)
    is AstLogicalNot -> visitor.visit(ast)
    is AstBitNot -> visitor.visit(ast)
    is AstFunctionArgument -> visitor.visit(ast)
    is AstFunctionReference -> visitor.visit(ast)
    is AstIdentifier -> visitor.visit(ast)
    is AstCastExpr -> visitor.visit(ast)
    is AstValInitialization -> visitor.visit(ast)
    is AstDefinedStruct -> visitor.visit(ast)
    is AstStructFieldDereference -> visitor.visit(ast)
    is AstUnknownFunctionReference -> visitor.visit(ast)
    is AstHardCastExpr -> visitor.visit(ast)

    is AstWhen -> visitor.visit(ast)
    is AstWhenBranch -> visitor.visit(ast)
}