package com.zagayevskiy.zvm.zc.ast

import com.zagayevskiy.zvm.zc.ZcType

interface Scope {
    val enclosingScope: Scope?
}

class GlobalScope : Scope {

    private val functions = mutableMapOf<String, AstDefinedFunction>()

    override val enclosingScope: Scope? = null

    fun declareFunction(name: String, args: List<AstFunctionArgument>, retType: ZcType, body: Ast): AstDefinedFunction? {
        val argTypes = args.map { it.type }
        val mangledName = mangleFunctionName(name, argTypes)

        if (functions.containsKey(mangledName)) return null

        return AstDefinedFunction(
                name = mangledName,
                args = args,
                retType = retType,
                body = body,
                enclosingScope = this)
    }

    fun lookupFunction(name: String, argTypes: List<ZcType>): AstDefinedFunction? {
        val mangledName = mangleFunctionName(name, argTypes)

        return functions[mangledName]
    }

    private fun mangleFunctionName(name: String, argTypes: List<ZcType>): String {
        return if (name == "main")
            name
        else
            argTypes.fold(StringBuilder(name)) { builder, type ->
                builder.append("$").append(type.name)
            }.toString()
    }
}

interface FunctionScope : Scope {
    fun lookupArgument(name: String): AstFunctionArgument?
}

class FunctionScopeDelegate(override val enclosingScope: Scope, args: List<AstFunctionArgument>) : FunctionScope {
    private val argsMap = args.associateBy { arg -> arg.name }

    init {
        if (argsMap.size != args.size) throw IllegalArgumentException("Args names must be different. Has $args.")
    }

    override fun lookupArgument(name: String) = argsMap[name]
}

