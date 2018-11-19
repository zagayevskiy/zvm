package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

fun ZcType.canBeAutoPromotedTo(other: ZcType) = when (this) {
    ZcType.Integer -> when (other) {
        ZcType.Integer -> true
        ZcType.Void,
        ZcType.Byte,
        ZcType.Boolean,
        ZcType.Unknown -> false
    }
    ZcType.Byte -> when (other) {
        ZcType.Integer, ZcType.Byte -> true
        ZcType.Void,
        ZcType.Boolean,
        ZcType.Unknown -> false
    }
    ZcType.Boolean, ZcType.Void, ZcType.Unknown -> false
}