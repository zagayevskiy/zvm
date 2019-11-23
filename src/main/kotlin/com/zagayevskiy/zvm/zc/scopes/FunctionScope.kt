package com.zagayevskiy.zvm.zc.scopes

import com.zagayevskiy.zvm.zc.ast.AstExpr
import com.zagayevskiy.zvm.zc.ast.AstFunctionArgument

interface FunctionScope : LocalScope

class FunctionScopeDelegate(enclosingScope: Scope, args: List<AstFunctionArgument>) : BaseLocalScope(enclosingScope), FunctionScope {
    private val arguments = args.associateBy { arg -> arg.name }

    init {
        if (arguments.size != args.size) throw IllegalArgumentException("Args names must be different. Has $args.")
    }

    override fun lookup(name: String, deep: Boolean): AstExpr? {
        return arguments[name] ?: super.lookup(name, deep)
    }
}