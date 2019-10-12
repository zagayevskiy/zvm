package com.zagayevskiy.zvm.util.extensions

import org.junit.Assert.*
import org.junit.Test

class PrimitiveExtensionsTest {
    @Test
    fun long() {
        val expected = byteArrayOf(
                0b11111111.toByte(),
                0b00000000,
                0b10101010.toByte(),
                0b11001100.toByte(),
                0b00110011,
                0b11110000.toByte(),
                0b00001111,
                0b01010101
        )

        val long = (0b11111111000000001010101011001100L shl 32) or 0b00110011111100000000111101010101L

        val actual = ByteArray(8)
        long.copyToByteArray(actual)

        assertEquals(expected.toList(), actual.toList())
        assertEquals(long, actual.copyToLong())
    }
}