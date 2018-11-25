package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

private val ErrorType = null

fun arithmeticTypesPromotion(left: ZcType, right: ZcType) = when (left) {
    ZcType.Integer -> when (right) {
        ZcType.Integer -> ZcType.Integer
        ZcType.Byte -> ZcType.Integer
        else -> ErrorType
    }
    ZcType.Byte -> when (right) {
        ZcType.Integer -> ZcType.Integer
        ZcType.Byte -> ZcType.Byte
        else -> ErrorType
    }
    else -> ErrorType
}