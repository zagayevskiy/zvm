package com.zagayevskiy.zvm.zc.ast

import com.sun.xml.internal.ws.wsdl.writer.document.soap.Body
import com.zagayevskiy.zvm.zc.ZcType
import kotlin.reflect.KProperty


sealed class Ast(
        var type: ZcType = ZcType.Unknown,
        private val children: MutableList<Ast> = mutableListOf<Ast>()) : MutableIterable<Ast> {
    protected fun <T : Ast> child(defaultValue: T) = ChildDelegate(defaultValue, children)

    protected class ChildDelegate<T : Ast>(defaultValue: T, private val list: MutableList<Ast>) {
        private val index: Int = list.size

        init {
            list.add(defaultValue)
        }

        operator fun getValue(thisRef: Ast, property: KProperty<*>): T = list[index] as T
        operator fun setValue(thisRef: Ast, property: KProperty<*>, value: T) {
            list[index] = value
        }
    }

    override fun iterator(): MutableListIterator<Ast> = children.listIterator()
}

object StubAst : Ast()

class AstProgram(declarations: MutableList<Ast>) : Ast(children = declarations)

sealed class TopLevelDeclaration(type: ZcType = ZcType.Unknown) : Ast()

class FunctionArgumentDeclaration(val name: String, val typeName: String)

class AstFunctionDeclaration(val name: String, val args: List<FunctionArgumentDeclaration>, val returnTypeName: String?, body: Ast) : TopLevelDeclaration() {
    val body by child(body)
}

class AstStructDeclaration(val name: String) : TopLevelDeclaration()

class AstDefinedFunction(val name: String, body: Ast) : TopLevelDeclaration() {
    val body by child(body)
}

class AstBlock(statements: List<AstStatement>) : Ast(children = statements.toMutableList())

sealed class AstStatement : Ast()
class AstVarDecl(val varName: String, val typeName: String?, initializer: Ast?) : AstStatement() {
    var initializer by child(initializer ?: AstConst.Undefined)
}

class AstValDecl(val valName: String, val typeName: String?, initializer: Ast) : AstStatement() {
    var initializer by child(initializer)
}

class AstLoop(initializer: Ast?, condition: AstExpr?, step: Ast?, body: Ast) : AstStatement() {
    var initializer by child(initializer ?: AstConst.Undefined)
    var condition by child(condition ?: AstConst.Undefined)
    var step by child(step ?: AstConst.Undefined)
    var body by child(body)
}

class AstWhile(condition: AstExpr, body: Ast) : AstStatement() {
    var condition by child(condition)
    var body by child(body)
}

class AstIfElse(condition: AstExpr, ifBody: Ast, elseBody: Ast?) : AstStatement() {
    var ifBody by child(ifBody)
    var elseBody by child(elseBody ?: AstConst.Undefined)
}

class AstFunctionReturn(expression: AstExpr?) : AstStatement() {
    var expression by child(expression ?: AstConst.Void)
}

class AstExpressionStatement(expression: AstExpr?) : AstStatement() {
    var expression by child(expression ?: AstConst.Undefined)
}

abstract class AstExpr(type: ZcType = ZcType.Unknown) : Ast(type)

sealed class AstBinary(left: AstExpr, right: AstExpr) : AstExpr() {
    var left by child(left)
    var right by child(right)
}

sealed class AstConst(type: ZcType) : AstExpr(type) {
    class Integer(val value: Int) : AstConst(ZcType.Integer)
    class Byte(val value: Byte) : AstConst(ZcType.Byte)
    class Boolean(val value: kotlin.Boolean) : AstConst(ZcType.Boolean)
    object Undefined : AstConst(ZcType.Unknown)
    object Void : AstConst(ZcType.Void)
}

class AstSum(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstDifference(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstMul(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstDiv(left: AstExpr, right: AstExpr) : AstBinary(left, right)
class AstMod(left: AstExpr, right: AstExpr) : AstBinary(left, right)


private val emptyVisitor: (Ast) -> Ast = { it }

fun Ast.walk(topDownVisitor: (Ast) -> Ast = emptyVisitor, bottomUpVisitor: (Ast) -> Ast = emptyVisitor, condition: (Ast) -> Boolean = { true }): Ast {
    var modified = if (condition(this)) topDownVisitor(this) else this

    val iterator = modified.iterator()
    while (iterator.hasNext()) {
        val child = iterator.next()
        val modifiedChild = child.walk(topDownVisitor, bottomUpVisitor, condition)
        iterator.set(modifiedChild)
    }

    modified = if (condition(modified)) bottomUpVisitor(modified) else modified

    return modified
}


fun main(args: Array<String>) {
}
