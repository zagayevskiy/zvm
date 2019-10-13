package com.zagayevskiy.zvm.zc.scopes

import com.zagayevskiy.zvm.zc.ast.AstExpr
import com.zagayevskiy.zvm.zc.ast.AstLocal
import com.zagayevskiy.zvm.zc.ast.AstVal
import com.zagayevskiy.zvm.zc.ast.AstVar
import com.zagayevskiy.zvm.zc.types.ZcType

interface LocalScope: Scope {
    val localsOffset: Int
}

open class BaseLocalScope(enclosingScope: Scope?): BaseScope(enclosingScope), LocalScope {
    private val locals = mutableMapOf<String, AstLocal>()
    private var localsOffsetField: Int = (enclosingScope as? LocalScope)?.localsOffset ?: 0

    override val localsOffset: Int
        get() = localsOffsetField

    override fun declareVar(name: String, type: ZcType): AstVar? {
        return declareLocal(AstVar(name, localsOffset, type))
    }

    override fun declareVal(name: String, type: ZcType): AstVal? {
        return declareLocal(AstVal(name, localsOffset, type))
    }

    override fun lookup(name: String, deep: Boolean): AstExpr? {
        return locals[name] ?: enclosingScope?.takeIf { deep }?.lookup(name, deep = true)
    }

    private fun <T: AstLocal> declareLocal(local: T): T? {
        val name = local.name
        if (name.existsInThisScope()) return null
        locals[name] = local
        localsOffsetField += local.type.sizeOf
        return local
    }

    private fun String.existsInThisScope() = lookup(this, deep = false) != null
}