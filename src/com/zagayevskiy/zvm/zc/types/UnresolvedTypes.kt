package com.zagayevskiy.zvm.zc.types

import com.zagayevskiy.zvm.zc.ast.Scope

sealed class UnresolvedType {
    data class Simple(val name: String) : UnresolvedType() {
        override fun toString() = name
    }

    data class Array(val elementType: UnresolvedType) : UnresolvedType() {
        override fun toString() = "[$elementType]"
    }

    data class Function(val argTypes: List<UnresolvedType>, val retType: UnresolvedType) : UnresolvedType() {
        override fun toString() = "(${argTypes.joinToString(separator = ", ")}) -> $retType"
    }
}

fun Scope.resolveType(unresolved: UnresolvedType): ZcType = when (unresolved) {
    is UnresolvedType.Simple -> {
        val name = unresolved.name
        ZcType.byName(name) ?: lookupStruct(name)?.type ?: error("Unknown type $name.")
    }
    is UnresolvedType.Array -> {
        val elementsType = resolveType(unresolved.elementType)
        ZcType.Array(elementsType)
    }

    is UnresolvedType.Function -> ZcType.Function(argTypes = unresolved.argTypes.map(::resolveType), retType = resolveType(unresolved.retType))
}