package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

private val ErrorType = null

fun bitShiftOperandTypePromotion(operandType: ZcType) = when (operandType) {
    ZcType.Void, ZcType.Boolean, ZcType.Unknown -> ErrorType
    ZcType.Integer -> ZcType.Integer
    ZcType.Byte -> ZcType.Integer
}

fun bitBinaryTypesPromotion(left: ZcType, right: ZcType) = when (left) {
    ZcType.Void, ZcType.Boolean, ZcType.Unknown -> ErrorType
    ZcType.Integer -> when (right) {
        ZcType.Void, ZcType.Boolean, ZcType.Unknown -> ErrorType
        ZcType.Integer -> ZcType.Integer
        ZcType.Byte -> ZcType.Integer
    }
    ZcType.Byte -> when (right) {
        ZcType.Void, ZcType.Boolean, ZcType.Unknown -> ErrorType
        ZcType.Integer -> ZcType.Integer
        ZcType.Byte -> ZcType.Byte
    }
}

fun bitUnaryTypePromotion(operandType: ZcType) = when (operandType) {
    ZcType.Void, ZcType.Boolean, ZcType.Unknown -> ErrorType
    ZcType.Integer -> ZcType.Integer
    ZcType.Byte -> ZcType.Byte
}