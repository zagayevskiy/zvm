package com.zagayevskiy.zvm.zc.types

sealed class UnresolvedType {
    data class Simple(val name: String): UnresolvedType() {
        override fun toString() = name
    }
    data class Array(val elementType: UnresolvedType): UnresolvedType() {
        override fun toString() = "[$elementType]"
    }
    data class Function(val argTypes: List<UnresolvedType>, val retType: UnresolvedType): UnresolvedType() {
        override fun toString() = "(${argTypes.joinToString(separator = ", ")}) -> $retType"
    }
}