package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

private val ErrorType = null

fun arithmeticTypesPromotion(left: ZcType, right: ZcType) = when(left) {
    ZcType.Void -> ErrorType
    ZcType.Integer -> when(right) {
        ZcType.Void -> ErrorType
        ZcType.Integer -> ZcType.Integer
        ZcType.Byte -> ZcType.Integer
        ZcType.Boolean -> ErrorType
        ZcType.Unknown -> ErrorType
    }
    ZcType.Byte -> when(right) {
        ZcType.Void -> ErrorType
        ZcType.Integer -> ZcType.Integer
        ZcType.Byte -> ZcType.Byte
        ZcType.Boolean -> ErrorType
        ZcType.Unknown -> ErrorType
    }
    ZcType.Boolean -> ErrorType
    ZcType.Unknown -> ErrorType
}