package com.zagayevskiy.zvm.zc.types

sealed class ZcType(val name: String) {

    object Void : ZcType("void")
    object Integer : ZcType("int")
    object Byte : ZcType("byte")
    object Boolean : ZcType("bool")
    object Unknown : ZcType("unknown")
//    class Pointer(val to: ZcType): ZcType("pointer->${to.name}")

    companion object {
        fun byName(name: String?) = when (name) {
            Integer.name -> Integer
            Byte.name -> Byte
            Boolean.name -> Boolean
            else -> null
        }
    }

    override fun toString() = "t:$name"
}