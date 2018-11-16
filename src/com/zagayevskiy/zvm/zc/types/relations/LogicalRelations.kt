package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

private val ErrorType = null

fun logicalTypesPromotion(left: ZcType, right: ZcType) = when (left) {
    ZcType.Void, ZcType.Unknown -> ErrorType
    ZcType.Boolean -> when (right) {
        ZcType.Void, ZcType.Unknown -> ErrorType
        ZcType.Integer, ZcType.Byte, ZcType.Boolean -> ZcType.Boolean
    }
    ZcType.Integer, ZcType.Byte -> when (right) {
        ZcType.Boolean -> ZcType.Boolean
        else -> ErrorType
    }
}

fun logicalUnaryTypePromotion(operandType: ZcType) = ZcType.Boolean
        .takeIf { operandType != ZcType.Void && operandType != ZcType.Unknown }