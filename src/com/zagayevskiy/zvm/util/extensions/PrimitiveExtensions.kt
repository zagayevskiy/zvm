package com.zagayevskiy.zvm.util.extensions


fun Int.bitCount() = java.lang.Integer.bitCount(this)

fun Int.copyToByteArray(destination: ByteArray, startIndex: Int = 0) {
    require(startIndex + 3 < destination.size)
    destination[startIndex + 0] = ((this shr 24) and 0xff).toByte()
    destination[startIndex + 1] = ((this shr 16) and 0xff).toByte()
    destination[startIndex + 2] = ((this shr 8) and 0xff).toByte()
    destination[startIndex + 3] = (this and 0xff).toByte()
}

fun String.copyToByteArray(destination: ByteArray, destIndex: Int = 0) {
    val bytes = toByteArray()
    require(destIndex + bytes.size < destination.size)
    bytes.copyTo(destination = destination, destIndex = destIndex)
}

fun ByteArray.copyToInt(startIndex: Int = 0): Int {
    require(startIndex + 3 < size) { "startIndex=$startIndex, size=$size" }

    return ((this[startIndex + 0].toInt() shl 24) and -0x1000000) or
            ((this[startIndex + 1].toInt() shl 16) and 0x00ff0000) or
            ((this[startIndex + 2].toInt() shl 8) and 0x0000ff00) or
            (this[startIndex + 3].toInt() and 0x000000ff)
}

fun ByteArray.copyTo(destination: ByteArray, destIndex: Int = 0, sourceIndex: Int = 0, count: Int = size) = System.arraycopy(this, sourceIndex, destination, destIndex, count)