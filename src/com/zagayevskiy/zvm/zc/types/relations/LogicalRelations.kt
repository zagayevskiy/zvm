package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

private val ErrorType = null

fun logicalTypesPromotion(left: ZcType, right: ZcType) = when (left) {
    ZcType.Boolean -> when (right) {
        ZcType.Boolean,
        ZcType.Byte,
        ZcType.Integer -> ZcType.Boolean
        else -> ErrorType
    }
    ZcType.Integer, ZcType.Byte -> when (right) {
        ZcType.Boolean -> ZcType.Boolean
        else -> ErrorType
    }
    else -> ErrorType
}

fun logicalUnaryTypePromotion(operandType: ZcType) = ZcType.Boolean
        .takeIf { operandType != ZcType.Void && operandType != ZcType.Unknown }