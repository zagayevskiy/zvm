package com.zagayevskiy.zvm.zc.scopes

import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.ZcType

interface Scope {
    val enclosingScope: Scope?

    fun declareVar(name: String, type: ZcType): AstVar?
    fun declareVal(name: String, type: ZcType): AstVal?

    fun lookupFunction(name: String): List<AstDefinedFunction>
    fun lookupStruct(name: String): AstDefinedStruct?
    fun lookup(name: String, deep: Boolean = true): AstExpr?
}



abstract class BaseScope(override val enclosingScope: Scope?) : Scope {

    override fun lookupFunction(name: String) = enclosingScope?.lookupFunction(name) ?: emptyList()

    override fun lookupStruct(name: String) = enclosingScope?.lookupStruct(name)

    override fun lookup(name: String, deep: Boolean): AstExpr? {
        return enclosingScope?.takeIf { deep }?.lookup(name, deep = true)
    }
}





