package com.zagayevskiy.zvm.common

import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import com.zagayevskiy.zvm.util.extensions.copyToInt
import kotlin.reflect.KProperty

interface Struct {
    var offset: Int
    val array: ByteArray

    val int
        get() = IntFieldDelegate(offset).also { offset += 4 }

    val byte
        get() = ByteFieldDelegate(offset++)

    fun <S : Struct> struct(factory: (parent: Struct) -> S) = StructFieldDelegate(this, factory)


    class IntFieldDelegate(private val offset: Int) {
        operator fun getValue(parent: Struct, property: KProperty<*>): Int = parent.array.copyToInt(offset)

        operator fun setValue(parent: Struct, property: KProperty<*>, value: Int) {
            value.copyToByteArray(parent.array, offset)
        }
    }

    class ByteFieldDelegate(private val offset: Int) {
        operator fun getValue(parent: Struct, property: KProperty<*>): Byte = parent.array[offset]

        operator fun setValue(parent: Struct, property: KProperty<*>, value: Byte) {
            parent.array[offset] = value
        }
    }

    class StructFieldDelegate<S : Struct>(private val parent: Struct, private val factory: (parent: Struct) -> S) {
        private var field: S? = null

        operator fun getValue(parent: Struct, property: KProperty<*>): S {
            return field ?: factory(parent).also { field = it }
        }
    }
}

abstract class TopLevelStruct(final override val array: ByteArray, final override var offset: Int = 0) : Struct

abstract class NestedStruct(parent: Struct) : Struct by parent

class SomeStruct(array: ByteArray) : TopLevelStruct(array) {
    var x by int
    var y by byte
    var z by int
    val inner by struct(::SomeNestedStruct)
    var k by byte
}

class SomeNestedStruct(parent: Struct) : NestedStruct(parent) {
    var x by byte
    var y by int
    val inner by struct(::SomeFlatStruct)
}

class SomeFlatStruct(parent: Struct) : NestedStruct(parent) {
    var a by byte
    var b by byte
    var c by int
}

fun main(args: Array<String>) {
    val memory = ByteArray(1024)
    val struct = SomeStruct(memory)
    val otherStruct = SomeStruct(memory)


    struct.apply {
        x = 123456
        y = 121
        k = 31
        z = 9999999
        inner.x = 1
        inner.y = 5
        inner.inner.a = 10
        inner.inner.b = 20
        inner.inner.c = 30
    }

    otherStruct.apply {
        println("x=$x, y=$y, z=$z, k=$k, inner.x=${inner.x}, inner.y=${inner.y}, i.i.a=${inner.inner.a}, i.i.b=${inner.inner.b}, i.i.c=${inner.inner.c}")
    }
}