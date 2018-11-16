package com.zagayevskiy.zvm.zc.ast

import com.zagayevskiy.zvm.zc.types.ZcType

interface Scope {
    val enclosingScope: Scope?

    fun declareVar(name: String, type: ZcType): AstVar?
    fun declareVal(name: String, type: ZcType): AstVal?

    fun lookupFunction(name: String): List<AstDefinedFunction>
    fun lookup(name: String, deep: Boolean = true): AstExpr?
}

open class BaseScope(override val enclosingScope: Scope?) : Scope {

    private val variables = mutableMapOf<String, AstExpr>()


    override fun declareVar(name: String, type: ZcType): AstVar? {
        if (name.existsInThisScope()) return null
        return AstVar(name, type).also { variables[name] = it }
    }

    override fun declareVal(name: String, type: ZcType): AstVal? {
        if (name.existsInThisScope()) return null
        return AstVal(name, type).also { variables[name] = it }
    }

    override fun lookupFunction(name: String) = enclosingScope?.lookupFunction(name) ?: emptyList()

    override fun lookup(name: String, deep: Boolean): AstExpr? {
        return variables[name] ?: enclosingScope?.takeIf { deep }?.lookup(name, deep = true)
    }

    private fun String.existsInThisScope() = lookup(this, deep = false) != null
}

class GlobalScope : BaseScope(null) {

    private val functions = mutableMapOf<String, MutableList<AstDefinedFunction>>()

    fun declareFunction(name: String, args: List<AstFunctionArgument>, retType: ZcType, body: Ast): AstDefinedFunction? {
        val argTypes = args.map { it.type }

        val sameNamedFunctions = functions[name] ?: mutableListOf()
        val sameSignatureFunction = sameNamedFunctions.firstOrNull { it.args.typesEquals(argTypes) }
        if (sameSignatureFunction != null) return null // TODO: may be error?

        val function = AstDefinedFunction(
                name = name,
                args = args,
                retType = retType,
                body = body,
                enclosingScope = this
        )
        functions[name] = sameNamedFunctions.apply { add(function) }

        return function
    }

    override fun lookupFunction(name: String) = functions[name] ?: emptyList<AstDefinedFunction>()

    private fun List<AstFunctionArgument>.typesEquals(types: List<ZcType>) = mapIndexed { index, arg -> arg.type == types[index] }.all { it }
}

interface FunctionScope : Scope {
    fun lookupArgument(name: String): AstFunctionArgument?
}

class FunctionScopeDelegate(enclosingScope: Scope, args: List<AstFunctionArgument>) : BaseScope(enclosingScope), FunctionScope {
    private val arguments = args.associateBy { arg -> arg.name }

    init {
        if (arguments.size != args.size) throw IllegalArgumentException("Args names must be different. Has $args.")
    }

    override fun lookupArgument(name: String) = arguments[name]

    override fun lookup(name: String, deep: Boolean): AstExpr? {
        return arguments[name] ?: super.lookup(name, deep)
    }
}

