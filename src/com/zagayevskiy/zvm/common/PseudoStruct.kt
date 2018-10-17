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

    fun <S : Struct> structArray(size: Int, initializer: (parent: Struct, index: Int) -> S) = StructArrayFieldDelegate<S>(size, initializer)

    fun <S : Struct> struct(factory: (parent: Struct) -> S) = StructFieldDelegate(factory)//.also { offset += sizeOf(factory) }


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

    class StructFieldDelegate<S : Struct>(private val factory: (parent: Struct) -> S) {
        private var field: S? = null

        operator fun getValue(parent: Struct, property: KProperty<*>): S {
            return field ?: factory(parent).also { field = it }
        }
    }

    class StructArrayFieldDelegate<S : Struct>(private val size: Int, private val initializer: (parent: Struct, index: Int) -> S) {
        private var field: StructArray<S>? = null

        operator fun getValue(parent: Struct, property: KProperty<*>): StructArray<S> {
            return field ?: StructArray(parent, size, initializer).also { field = it }
        }
    }
}

class StructArray<S : Struct>(parent: Struct, val size: Int, private val initializer: (parent: Struct, index: Int) -> S) : NestedStruct(parent), Iterable<S> {

    private val elements = mutableListOf<S?>().also { list -> (0..size).forEach { list.add(null) } }

    operator fun get(index: Int): S {
        if (0 > index || index >= size) throw NoSuchElementException("Want $index, has $size")

        return elements[index] ?: initializer(parent, index).also { elements[index] = it }
    }

    override fun iterator() = object : Iterator<S> {
        private var cursor = 0
        override fun hasNext() = cursor < size
        override fun next() = get(cursor++)
    }
}

abstract class TopLevelStruct(final override val array: ByteArray, final override var offset: Int = 0) : Struct

abstract class NestedStruct(protected val parent: Struct) : Struct by parent

fun <S : Struct> sizeOf(factory: (parent: Struct) -> S): Int = object : Struct {
    override var offset: Int = 0
    override val array = ByteArray(0)
}.let(factory).offset

class SomeStruct(array: ByteArray) : TopLevelStruct(array) {
    var x by int
    var y by byte
    var z by int
    val inner by struct(::SomeNestedStruct)
    val arr by structArray(10) { parent, index -> SomeNestedStruct(parent) }
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
        x = 111
        y = 22
        k = 3
        z = 99
        inner.x = 1
        inner.y = 5
        inner.inner.a = 10
        inner.inner.b = 20
        inner.inner.c = 30
        arr.forEachIndexed { index, it ->
            it.x = index.toByte()

        }
    }

    otherStruct.apply {
        println("x=$x, y=$y, z=$z, k=$k, inner.x=${inner.x}, inner.y=${inner.y}, i.i.a=${inner.inner.a}, i.i.b=${inner.inner.b}, i.i.c=${inner.inner.c}")
        arr.forEach { print("x=${it.x}, y=${it.y}; ") }
    }

    println()

    memory.forEach {
        print("$it ")
    }
    println()

    println("${sizeOf(::SomeNestedStruct)}")
}