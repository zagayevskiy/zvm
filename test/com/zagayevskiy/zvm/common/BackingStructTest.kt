package com.zagayevskiy.zvm.common

import org.junit.Test
import kotlin.test.assertEquals

private class TestPlainStruct(array: ByteArray, offset: Int): BackingStruct(array, offset) {
    var x by int
    var y by byte
    var z by int
}

private class ParentStruct(array: ByteArray, offset: Int): BackingStruct(array, offset) {
    var x by byte
    val child by struct(::TestPlainStruct)
    var y by int
}

private class OtherParentStruct(array: ByteArray, offset: Int): BackingStruct(array, offset) {
    var otherX by byte
    var childX by int
    var childY by byte
    var childZAndY by long
}
class BackingStructTest {

    @Test
    fun sizeOf() {
        assertEquals(9, sizeOf(::TestPlainStruct))
        assertEquals(14, sizeOf(::ParentStruct))
        assertEquals(14, sizeOf(::OtherParentStruct))
    }

    @Test
    fun storing() {
        val backingArray = ByteArray(9)
        val struct = TestPlainStruct(backingArray, 0).apply {
            x = 1234567890
            y = 127
            z = 1987654321
        }

        assertEquals(1234567890, struct.x)
        assertEquals(127.toByte(), struct.y)
        assertEquals(1987654321, struct.z)

        val mirror = TestPlainStruct(backingArray, 0)

        assertEquals(1234567890, mirror.x)
        assertEquals(127.toByte(), mirror.y)
        assertEquals(1987654321, mirror.z)
    }

    @Test
    fun nestedStruct() {
        val backingArray = ByteArray(14)
        val parentStruct = ParentStruct(backingArray, 0).apply {
            x = 17
            y = 878787878
            child.apply {
                x = 1234567890
                y = 127
                z = 1987654321
            }
        }

        val mirror = ParentStruct(backingArray, 0)
        assertEquals(17, mirror.x)
        assertEquals(878787878, mirror.y)
        assertEquals(1234567890, mirror.child.x)
        assertEquals(127.toByte(), mirror.child.y)
        assertEquals(1987654321, mirror.child.z)
    }

    @Test
    fun hashAndEquals() {
        val backingArray = ByteArray(100)
        val parentStruct1 = ParentStruct(backingArray, 0).apply {
            x = -123
            child.apply {
                x = 905010703
                y = 127
                z = 0xabcdef9
            }
            y = 0x01234567
        }

        val otherParentStruct1 = OtherParentStruct(backingArray, 0)
        assertEquals<BackingStruct>(parentStruct1, otherParentStruct1)
        assertEquals(parentStruct1.hashCode(), otherParentStruct1.hashCode())

        val otherParentStruct2 = OtherParentStruct(backingArray, 50).apply {
            otherX = -123
            childX = 905010703
            childY = 127
            childZAndY = 0xabcdef901234567L
        }

        assertEquals(otherParentStruct1, otherParentStruct2)
        assertEquals(otherParentStruct1.hashCode(), otherParentStruct2.hashCode())
    }
}