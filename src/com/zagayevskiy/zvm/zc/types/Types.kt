package com.zagayevskiy.zvm.zc.types

sealed class ZcType(val name: String, val sizeOf: Int) {

    object Void : ZcType("void", 0)
    object Integer : ZcType("int", 4)
    object Byte : ZcType("byte", 1)
    object Boolean : ZcType("bool", 1)
    object Unknown : ZcType("unknown", 0)
    class Struct(private val fields: List<Field>) : ZcType("struct", 4) {
        class Field(val name: String, val type: ZcType, val offset: Int)

        fun findField(name: String): Field? = fields.firstOrNull { it.name == name }

        override fun toString() = "struct $name"
    }

    class Array(val itemType: ZcType) : ZcType("array", 4) {
        override fun toString() = "[$itemType]"
    }

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