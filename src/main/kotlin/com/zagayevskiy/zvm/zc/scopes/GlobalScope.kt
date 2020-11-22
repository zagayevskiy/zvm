package com.zagayevskiy.zvm.zc.scopes

import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.ZcType

class GlobalScope : BaseScope(null) {

    private val functions = mutableMapOf<String, MutableList<AstDefinedFunction>>()
    private val structs = mutableMapOf<String, AstDefinedStruct>()
    private val consts = mutableMapOf<String, AstConst>()

    init {
        declareConst("true", AstConst.Boolean.True)
        declareConst("false", AstConst.Boolean.False)
        declareConst("nil", AstConst.DefaultValue(forType = ZcType.Array(itemType = ZcType.Void)))
    }

    override fun declareVar(name: String, type: ZcType): AstVar? {
        TODO("Globals not supported yet")
    }

    override fun declareVal(name: String, type: ZcType): AstVal? {
        TODO("Globals not supported yet")
    }

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

    fun declareStruct(name: String, type: ZcType.Struct): AstDefinedStruct? {
        if (structs.containsKey(name)) return null

        return AstDefinedStruct(name, type).also { structs[name] = it }
    }

    fun declareConst(name: String, initializer: AstConst): Boolean {
        if (consts.containsKey(name)) return false
        consts[name] = initializer
        return true
    }

    override fun lookupFunction(name: String) = functions[name] ?: emptyList<AstDefinedFunction>()

    override fun lookupStruct(name: String) = structs[name]

    override fun lookup(name: String, deep: Boolean): AstExpr? {
        return consts[name] ?: super.lookup(name, deep)
    }

    private fun List<AstFunctionArgument>.typesEquals(types: List<ZcType>) = mapIndexed { index, arg -> arg.type == types[index] }.all { it }
}