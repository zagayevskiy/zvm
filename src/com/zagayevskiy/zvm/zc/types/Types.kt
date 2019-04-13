package com.zagayevskiy.zvm.zc.types

sealed class ZcType(open val name: String, val sizeOf: Int) {

    object Void : ZcType("void", 0)
    object Integer : ZcType("int", 4)
    object Byte : ZcType("byte", 1)
    object Boolean : ZcType("bool", 1)
    object Unknown : ZcType("unknown", 0)
    data class Struct(val structName: String, private val fields: List<Field>) : ZcType("struct $structName", 4) {
        val allocSize by lazy { fields.last().offset + fields.last().type.sizeOf }

        data class Field(val name: String, val type: ZcType, val offset: Int)

        fun findField(name: String): Field? = fields.firstOrNull { it.name == name }
    }

    data class Array(val itemType: ZcType) : ZcType("[$itemType]", 4)

    companion object {
        fun byName(name: String?) = when (name) {
            Integer.name -> Integer
            Byte.name -> Byte
            Boolean.name -> Boolean
            else -> null
        }
    }

    override fun toString() = name
}