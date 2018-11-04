package com.zagayevskiy.zvm.zc

import kotlin.reflect.KProperty

interface Type : Comparable<Type> {
    val name: String
    val index: Int

    override fun compareTo(other: Type) = compareValues(index, other.index)
}

sealed class BuiltInType(override val name: String, override val index: Int) : Type {
    override fun toString() = name
}

object BuiltInByte : BuiltInType("byte", 0)
object BuiltInInt : BuiltInType("int", 1)
object BuiltInString : BuiltInType("string", 2)
object BuiltInError : BuiltInType("error", Int.MAX_VALUE)

private fun arithmeticResultType(left: Type, right: Type) = if (left is BuiltInType && right is BuiltInType) {
    arithmeticResultType(left, right)
} else {
    BuiltInError
}

private fun arithmeticResultType(left: BuiltInType, right: BuiltInType): BuiltInType = when (left) {
    BuiltInByte -> when (right) {
        BuiltInByte -> BuiltInByte
        BuiltInInt -> BuiltInInt
        BuiltInString, BuiltInError -> BuiltInError
    }

    BuiltInInt -> when (right) {
        BuiltInByte, BuiltInInt -> BuiltInInt
        BuiltInString, BuiltInError -> BuiltInError
    }

    BuiltInString -> when (right) {
        BuiltInString -> BuiltInString
        else -> BuiltInError
    }

    BuiltInError -> BuiltInError
}

abstract class Ast {
    var evalType: Type? = null

    private val children = mutableListOf<Ast>()

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

    fun iterator(): MutableListIterator<Ast> = children.listIterator()
}

abstract class AstExpr : Ast()

abstract class AstBinary(left: AstExpr, right: AstExpr) : AstExpr() {
    var left by child(left)
    var right by child(right)
}

abstract class AstLeaf : AstExpr()

class AstIntExpr(val value: Int) : AstLeaf()
class AstByteExpr(val value: Byte) : AstLeaf()
class AstStringExpr(val value: String) : AstLeaf()

class AstCast(typeTo: Type, casted: AstExpr) : AstExpr() {
    var casted by child(casted)

    init {
        evalType = typeTo
    }
}

class AstBinaryPlus(left: AstExpr, right: AstExpr) : AstBinary(left, right)

class AstBinaryMul(left: AstExpr, right: AstExpr) : AstBinary(left, right)

fun topDown(ast: Ast, visitor: (Ast) -> Ast) = walk(ast, visitor, { it })

fun bottomUp(ast: Ast, visitor: (Ast) -> Ast) = walk(ast, { it }, visitor)

private fun walk(ast: Ast, topDownVisitor: (Ast) -> Ast, bottomUpVisitor: (Ast) -> Ast): Ast {
    var modified = topDownVisitor(ast)

    val iter = modified.iterator()
    while (iter.hasNext()) {
        val child = iter.next()
        val modifiedChild = walk(child, topDownVisitor, bottomUpVisitor)
        iter.set(modifiedChild)
    }

    modified = bottomUpVisitor(ast)

    return modified
}

fun main(args: Array<String>) {
    var ast: Ast = AstBinaryMul(AstBinaryPlus(AstBinaryPlus(AstIntExpr(1), AstByteExpr(2)), AstBinaryMul(AstByteExpr(3), AstByteExpr(4))), AstByteExpr(0))
    ast = bottomUp(ast) { it.also { println("${text(it)} ") } }

    ast = bottomUp(ast) { computeTypeAndPromote(it) }
    println("with types: ")
    bottomUp(ast) { it.also { println("${text(it)} ") } }
}

private fun text(ast: Ast): String = when (ast) {
    is AstIntExpr -> "${ast.value}: ${ast.evalType}"
    is AstByteExpr -> "${ast.value}: ${ast.evalType}"
    is AstStringExpr -> "${ast.value}: ${ast.evalType}"
    is AstBinaryPlus -> "+: ${ast.evalType}"
    is AstBinaryMul -> "*: ${ast.evalType}"
    is AstCast -> "cast (${ast.casted.evalType} -> ${ast.evalType})"
    else -> throw IllegalArgumentException(ast.toString())
}

private fun computeTypeAndPromote(ast: Ast): Ast {
    when (ast) {
        is AstIntExpr -> ast.evalType = BuiltInInt
        is AstByteExpr -> ast.evalType = BuiltInByte
        is AstStringExpr -> ast.evalType = BuiltInString
        is AstBinary -> {
            ast.evalType = arithmeticResultType(ast.left.evalType!!, ast.right.evalType!!)

            if (ast.evalType != ast.left.evalType) {
                ast.left = AstCast(ast.evalType!!, ast.left)
            }
            if (ast.evalType != ast.right.evalType) {
                ast.right = AstCast(ast.evalType!!, ast.right)
            }

        }
    }
    return ast
}