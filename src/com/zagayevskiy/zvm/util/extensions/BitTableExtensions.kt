package com.zagayevskiy.zvm.util.extensions

import com.zagayevskiy.zvm.util.BitTable

/**
 * Returns index of nearest to the end not set bit or null
 */
fun BitTable.checkInterval(begin: Int, end: Int): Int? {
    require(0 <= begin)
    require(begin <= end)
    require(end < size)
    return (end downTo begin).firstOrNull { index -> this[index] }
}

fun BitTable.fill(from: Int, to: Int, value: Boolean) {
    require(0 <= from)
    require(from < to)
    require(to <= size)

    (from until to).forEach { index -> this[index] = value }
}