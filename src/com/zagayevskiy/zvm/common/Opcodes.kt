package com.zagayevskiy.zvm.common

object Opcodes {
    val JMP = 0x0.byte

    val CALL = 0x10.byte
    val RET = 0x11.byte

    val PUSH = 0x20.byte
    val POP = 0x21.byte

    val ICONST = 0x30.byte

    val IADD = 0x40.byte

    val OUT = 0x70.byte
}

private val Int.byte
    get() = toByte()