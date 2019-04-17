package com.zagayevskiy.zvm.zc.types.relations

import com.zagayevskiy.zvm.zc.types.ZcType

private val ErrorType = null

fun arraySumTypesPromotion(left: ZcType, right: ZcType) = when (left) {
    ZcType.Integer, ZcType.Byte -> when (right) {
        is ZcType.Array -> right
        else -> ErrorType
    }
    is ZcType.Array -> when (right) {
        ZcType.Integer, ZcType.Byte -> left
        else -> ErrorType
    }
    else -> ErrorType
}