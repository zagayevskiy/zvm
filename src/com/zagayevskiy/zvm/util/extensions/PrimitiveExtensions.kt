package com.zagayevskiy.zvm.util.extensions


fun Int.bitCount() = java.lang.Integer.bitCount(this)

fun Int.copyToByteArray(array: ByteArray, startIndex: Int = 0) {
    require(startIndex + 3 < array.size)
    array[startIndex + 0] = ((this shr 24) and 0xff).toByte()
    array[startIndex + 1] = ((this shr 16) and 0xff).toByte()
    array[startIndex + 2] = ((this shr 8) and 0xff).toByte()
    array[startIndex + 3] = (this and 0xff).toByte()
}

fun String.copyToByteArray(array: ByteArray, startIndex: Int = 0) {
    val bytes = toByteArray()
    require(startIndex + bytes.size < array.size)
    bytes.copyTo(array, startIndex)
}

fun ByteArray.copyToInt(startIndex: Int = 0): Int {
    require(startIndex + 3 < size)

    return ((this[startIndex + 0].toInt() shl 24) and -0x1000000) or
            ((this[startIndex + 1].toInt() shl 16) and 0x00ff0000) or
            ((this[startIndex + 2].toInt() shl 8) and 0x0000ff00) or
            (this[startIndex + 3].toInt() and 0x000000ff)
}

fun ByteArray.copyTo(other: ByteArray, startIndex: Int = 0) = System.arraycopy(this, 0, other, startIndex, size)