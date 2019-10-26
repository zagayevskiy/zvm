package com.zagayevskiy.zvm.util.extensions


fun Int.bitCount() = java.lang.Integer.bitCount(this)

fun Int.copyToByteArray(destination: ByteArray, startIndex: Int = 0) {
    require(startIndex + 3 < destination.size)
    destination[startIndex + 0] = ((this shr 24) and 0xff).toByte()
    destination[startIndex + 1] = ((this shr 16) and 0xff).toByte()
    destination[startIndex + 2] = ((this shr 8) and 0xff).toByte()
    destination[startIndex + 3] = (this and 0xff).toByte()
}

fun Long.copyToByteArray(destination: ByteArray, startIndex: Int = 0) {
    require(startIndex + 7 < destination.size)
    val i1 = ((this shr 32) and 0xffffffffL).toInt()
    val i2 = (this and 0xffffffffL).toInt()
    i1.copyToByteArray(destination, startIndex)
    i2.copyToByteArray(destination, startIndex + 4)

}

fun String.copyToByteArray(destination: ByteArray, destIndex: Int = 0) {
    val bytes = toByteArray()
    require(destIndex + bytes.size < destination.size)
    bytes.copyTo(destination = destination, destIndex = destIndex)
}

fun String.toSizePrefixedByteArray(): ByteArray {
    val bytes = toByteArray()
    val result = ByteArray(bytes.size + 4)
    bytes.size.copyToByteArray(result)
    bytes.copyTo(result, destIndex = 4)
    return result
}

fun ByteArray.copyToInt(startIndex: Int = 0): Int {
    require(startIndex + 3 < size) { "startIndex=$startIndex, size=$size" }

    return ((this[startIndex + 0].toInt() shl 24) and -0x1000000) or
            ((this[startIndex + 1].toInt() shl 16) and 0x00ff0000) or
            ((this[startIndex + 2].toInt() shl 8) and 0x0000ff00) or
            (this[startIndex + 3].toInt() and 0x000000ff)
}

fun ByteArray.copyToLong(startIndex: Int = 0): Long {
    require(startIndex + 7 < size) { "startIndex=$startIndex, size=$size" }

    val i1 = (copyToInt(startIndex).toLong() and 0xffffffffL) shl 32
    val i2 = copyToInt(startIndex + 4).toLong() and 0xffffffffL
    return i1 or i2
}

fun ByteArray.copyTo(destination: ByteArray, destIndex: Int = 0, sourceIndex: Int = 0, count: Int = size) = System.arraycopy(this, sourceIndex, destination, destIndex, count)

infix fun Byte.xor(right: Byte): Int = toInt() xor right.toInt()

infix fun Byte.and(right: Byte): Int = toInt() and right.toInt()

infix fun Byte.or(right: Byte): Int = toInt() or right.toInt()