package com.zagayevskiy.zvm.zc.ast

import com.zagayevskiy.zvm.zc.scopes.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType
import com.zagayevskiy.zvm.zc.types.ZcType
import kotlin.reflect.KProperty


sealed class Ast(var type: ZcType = ZcType.Unknown) : MutableIterable<Ast> {
    private val children: MutableList<Ast> = mutableListOf()
    protected fun <T : Ast> child(defaultValue: T) = ChildDelegate(defaultValue, children)
    protected fun <T : Ast, L : List<T>> childList(defaultValue: L) = ChildListDelegate(defaultValue, children)

    protected class ChildDelegate<T : Ast>(defaultValue: T, private val list: MutableList<Ast>) {
        private val index = list.size

        init {
            list.add(defaultValue)
        }

        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Ast, property: KProperty<*>): T = list[index] as T

        operator fun setValue(thisRef: Ast, property: KProperty<*>, value: T) {
            list[index] = value
        }
    }

    protected class ChildListDelegate<T : Ast, L : List<T>>(private val defaultValue: L, private val list: MutableList<Ast>) {
        private val beginIndex = list.size

        init {
            list.addAll(defaultValue)
        }

        @Suppress("UNCHECKED_CAST")
        private val delegatedSubList: L
            get() = (list
                    .takeIf { defaultValue.isNotEmpty() }
                    ?.subList(beginIndex, beginIndex + defaultValue.size) ?: mutableListOf()) as L

        operator fun getValue(thisRef: Ast, property: KProperty<*>): L = delegatedSubList
    }

    override fun iterator(): MutableListIterator<Ast> = children.listIterator()

    override fun toString() = if (children.isEmpty()) {
        "${stringValue()}"
    } else {
        "(${stringValue()}, children=$children)"
    }

    override fun equals(other: Any?) = other is Ast && this eq other

    override fun hashCode() = toString().hashCode() //It's ok for our purpose

    fun isLeaf() = children.isEmpty()
}

class AstProgram(declarations: MutableList<TopLevelDeclaration>) : Ast() {
    val declarations by childList(declarations)
}

sealed class TopLevelDeclaration(type: ZcType = ZcType.Unknown) : Ast(type)

data class FunctionArgumentDeclaration(val name: String, val type: UnresolvedType)

class AstFunctionDeclaration(val name: String, val args: List<FunctionArgumentDeclaration>, val returnType: UnresolvedType?, body: Ast) : TopLevelDeclaration() {
    val body by child(body)
}

class AstStructDeclaration(val name: String, fieldsDeclarations: List<AstVarDecl>) : TopLevelDeclaration() {
    val fieldsDeclarations by childList(fieldsDeclarations)
}

class AstDefinedFunction(val name: String, val index: Int, val args: List<AstFunctionArgument>, val retType: ZcType, body: Ast, enclosingScope: Scope)
    : TopLevelDeclaration(type = ZcType.Function(argTypes = args.map { it.type }, retType = retType)),
        FunctionScope by FunctionScopeDelegate(enclosingScope, args) {
    val body by child(body)
}

class AstDefinedStruct(val name: String, val structType: ZcType.Struct) : TopLevelDeclaration(structType)

class AstUnknownFunctionReference(val name: String) : AstExpr()
class AstFunctionReference(val function: AstDefinedFunction) : AstExpr(type = function.type)

sealed class AstStatement : Ast(type = ZcType.Void)

data class AstAsmBlock(val body: String) : AstStatement()

class AstStatementList(statements: List<AstStatement> = emptyList()) : AstStatement() {
    val statements by childList(statements)

    companion object {
        val Empty = AstStatementList()
    }
}

class AstBlock(statements: List<AstStatement> = emptyList(), enclosingScope: Scope? = null) : AstStatement(), BlockScope by BlockScopeDelegate(enclosingScope) {
    val statements by childList(statements)

    companion object {
        val Empty = AstBlock()
    }
}

class AstVarDecl(val varName: String, val unresolvedType: UnresolvedType?, initializer: AstExpr?) : AstStatement() {
    var initializer by child(initializer ?: AstConst.Undefined)
}

class AstValDecl(val valName: String, val unresolvedType: UnresolvedType?, initializer: AstExpr) : AstStatement() {
    var initializer by child(initializer)
}

class AstForLoop(initializer: AstStatement, condition: AstExpr, step: AstStatement, body: AstStatement) : AstStatement() {
    var initializer by child(initializer)
    var condition by child(condition)
    var step by child(step)
    var body by child(body)
}

class AstWhileLoop(condition: AstExpr, body: AstStatement) : AstStatement() {
    var condition by child(condition)
    var body by child(body)
}

class AstIfElse(condition: AstExpr, ifBody: AstStatement, elseBody: AstStatement?) : AstStatement() {
    var condition by child(condition)
    var ifBody by child(ifBody)
    var elseBody by child(elseBody ?: AstBlock.Empty)
}

class AstWhen(checkValue: AstExpr, branches: List<AstWhenBranch>, elseStatement: AstStatement?) : AstStatement() {
    var checkValue by child(checkValue)
    val branches: MutableList<AstWhenBranch> by childList(branches.toMutableList())
    var elseStatement by child(elseStatement ?: AstBlock.Empty)
}

class AstWhenBranch(case: AstExpr, branch: AstStatement) : Ast() {
    var case by child(case)
    var branch by child(branch)
}

class AstFunctionReturn(expression: AstExpr?) : AstStatement() {
    var expression by child(expression ?: AstConst.Void)
}

class AstExpressionStatement(expression: AstExpr?) : AstStatement() {
    var expression by child(expression ?: AstConst.Undefined)
}

sealed class AstExpr(type: ZcType = ZcType.Unknown) : Ast(type)

sealed class AstBinary(left: AstExpr, right: AstExpr) : AstExpr() {
    var left by child(left)
    var right by child(right)
}

class AstIdentifier(val name: String) : AstExpr()
class AstAssignment(assigned: AstExpr, assignation: AstExpr) : AstExpr() {
    val assignable by child(assigned)
    var assignation by child(assignation)
}

class AstValInitialization(valToInit: AstVal, initializer: AstExpr) : AstStatement() {
    var valToInit by child(valToInit)
    var initializer by child((initializer))
}

sealed class AstLocal(type: ZcType) : AstExpr(type) {
    abstract val name: String
    abstract val offset: Int
}

class AstVar(override val name: String, override val offset: Int, type: ZcType) : AstLocal(type)
class AstVal(override val name: String, override val offset: Int, type: ZcType) : AstLocal(type)

class AstArrayIndexing(array: AstExpr, index: AstExpr) : AstExpr() {
    var array by child(array)
    var index by child(index)
}

class AstStructFieldDereference(structInstance: AstExpr, val name: String) : AstExpr() {
    var structInstance by child(structInstance)

    val structType: ZcType.Struct
        get() = structInstance.type as ZcType.Struct
}

class AstFunctionCall(function: AstExpr, params: List<AstExpr>) : AstExpr() {
    var function by child(function)
    val params: MutableList<AstExpr> by childList(params.toMutableList())
}

class AstFunctionArgument(val name: String, val index: Int, type: ZcType) : AstExpr(type = type)

sealed class AstConst(type: ZcType) : AstExpr(type) {
    data class Integer(val value: Int) : AstConst(ZcType.Integer)
    data class Byte(val value: kotlin.Byte) : AstConst(ZcType.Byte)
    data class Boolean(val value: kotlin.Boolean) : AstConst(ZcType.Boolean)
    class StringLiteral(val value: String): AstConst(ZcType.Array(ZcType.Byte))
    class DefaultValue(type: ZcType) : AstConst(type)
    object Undefined : AstConst(ZcType.Unknown)
    object Void : AstConst(ZcType.Void)
}

sealed class AstLogicalBinary(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstDisjunction(left: AstExpr, right: AstExpr) : AstLogicalBinary(left, right)
class AstConjunction(left: AstExpr, right: AstExpr) : AstLogicalBinary(left, right)

sealed class AstBitBinary(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstBitAnd(left: AstExpr, right: AstExpr) : AstBitBinary(left, right)
class AstBitOr(left: AstExpr, right: AstExpr) : AstBitBinary(left, right)
class AstBitXor(left: AstExpr, right: AstExpr) : AstBitBinary(left, right)
sealed class AstBitShift(left: AstExpr, right: AstExpr) : AstBitBinary(left, right) {
    class Left(left: AstExpr, right: AstExpr) : AstBitShift(left, right)
    class Right(left: AstExpr, right: AstExpr) : AstBitShift(left, right)
}

sealed class AstComparison(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstEquals(left: AstExpr, right: AstExpr) : AstComparison(left, right)
class AstNotEquals(left: AstExpr, right: AstExpr) : AstComparison(left, right)
class AstLess(left: AstExpr, right: AstExpr) : AstComparison(left, right)
class AstLessEq(left: AstExpr, right: AstExpr) : AstComparison(left, right)
class AstGreat(left: AstExpr, right: AstExpr) : AstComparison(left, right)
class AstGreatEq(left: AstExpr, right: AstExpr) : AstComparison(left, right)

sealed class AstArithmeticBinary(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstSum(left: AstExpr, right: AstExpr) : AstArithmeticBinary(left, right)
class AstDifference(left: AstExpr, right: AstExpr) : AstArithmeticBinary(left, right)
class AstMul(left: AstExpr, right: AstExpr) : AstArithmeticBinary(left, right)
class AstDiv(left: AstExpr, right: AstExpr) : AstArithmeticBinary(left, right)
class AstMod(left: AstExpr, right: AstExpr) : AstArithmeticBinary(left, right)

class AstLogicalNot(expression: AstExpr) : AstExpr() {
    var expression by child(expression)
}

class AstBitNot(expression: AstExpr) : AstExpr() {
    var expression by child(expression)
}

class AstHardCastExpr(expression: AstExpr, val unresolvedCastType: UnresolvedType) : AstExpr() {
    val expression by child(expression)
}

class AstCastExpr(expression: AstExpr, castType: ZcType) : AstExpr(type = castType) {
    val expression by child(expression)
}

class AstSizeOf(val unresolvedType: UnresolvedType) : AstExpr(ZcType.Integer)

private val emptyVisitor: (Ast) -> Ast = { it }

fun Ast.walk(topDownVisitor: (Ast) -> Ast = emptyVisitor, bottomUpVisitor: (Ast) -> Ast = emptyVisitor): Ast {
    var modified = topDownVisitor(this)

    val iterator = modified.iterator()
    while (iterator.hasNext()) {
        val child = iterator.next()
        val modifiedChild = child.walk(topDownVisitor, bottomUpVisitor)
        iterator.set(modifiedChild)
    }

    modified = bottomUpVisitor(modified)

    return modified
}


fun main(args: Array<String>) {
}
