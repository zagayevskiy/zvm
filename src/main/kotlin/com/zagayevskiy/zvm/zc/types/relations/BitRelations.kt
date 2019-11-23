package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

private val ErrorType = null

fun bitShiftOperandTypePromotion(operandType: ZcType) = when (operandType) {
    ZcType.Integer -> ZcType.Integer
    ZcType.Byte -> ZcType.Integer
    else -> ErrorType
}

fun bitBinaryTypesPromotion(left: ZcType, right: ZcType) = when (left) {
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

fun bitUnaryTypePromotion(operandType: ZcType) = when (operandType) {
    ZcType.Integer -> ZcType.Integer
    ZcType.Byte -> ZcType.Byte
    else -> ErrorType
}