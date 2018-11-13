package com.zagayevskiy.zvm.zc.ast

import com.sun.org.apache.xpath.internal.operations.Bool
import com.zagayevskiy.zvm.zc.ZcType

interface Scope {
}

class ScopeDelegate

class GlobalScope : Scope {

    private val functions = mutableMapOf<String, AstDefinedFunction>()

    fun declareFunction(name: String, args: List<AstFunctionArgument>, retType: ZcType, body: Ast): AstDefinedFunction? {
        val argTypes = args.map { it.type }
        val mangledName = mangleFunctionName(name, argTypes)

        if (functions.containsKey(mangledName)) return null

        return AstDefinedFunction(mangledName, args, retType, body)
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

class FunctionScopeDelegate(args: List<AstFunctionArgument>) : FunctionScope {
    private val argsMap = args.associateBy { arg -> arg.name }

    override fun lookupArgument(name: String) = argsMap[name]
}

