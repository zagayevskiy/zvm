package com.zagayevskiy.zvm.zc

sealed class ZcType(val name: String) {

    object Void : ZcType("void")
    object Integer : ZcType("int")
    object Byte : ZcType("byte")
    object Boolean : ZcType("bool")
    object Unknown : ZcType("unknown")
    object Error : ZcType("error")

    companion object {
        fun byName(name: String?) = when (name) {
            ZcType.Integer.name -> Integer
            ZcType.Byte.name -> Byte
            else -> null
        }
    }

    override fun toString() = "ZcType#$name"
}