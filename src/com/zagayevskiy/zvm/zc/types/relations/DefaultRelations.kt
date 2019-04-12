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
        (other is ZcType.Array) && (other.itemType is ZcType.Void)
    }
    is ZcType.Struct,
    ZcType.Void,
    ZcType.Unknown -> false
}