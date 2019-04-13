package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

fun ZcType.canBeAutoPromotedTo(other: ZcType) = when (this) {
    ZcType.Integer -> when (other) {
        ZcType.Integer -> true
        else -> false
    }
    ZcType.Byte -> when (other) {
        ZcType.Integer, ZcType.Byte -> true
        else -> false
    }
    ZcType.Boolean -> when (other) {
        ZcType.Boolean,
        ZcType.Byte,
        ZcType.Integer -> true
        else -> false
    }
    is ZcType.Array -> {
        when (other) {
            is ZcType.Array -> (other.itemType is ZcType.Void || itemType is ZcType.Void)
            is ZcType.Struct -> itemType is ZcType.Void
            else -> false
        }
    }
    is ZcType.Struct -> {
        (other is ZcType.Array) && other.itemType is ZcType.Void
    }
    is ZcType.Function,
    ZcType.Void,
    ZcType.Unknown -> false
}