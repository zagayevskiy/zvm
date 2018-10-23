package com.zagayevskiy.zvm.common

object Opcodes {
    val JMP = 0x0.byte

    val CALL = 0x10.byte
    val RET = 0x11.byte

    val PUSH = 0x20.byte
    val POP = 0x21.byte
    val ALOAD = 0x22.byte
    val LSTORE = 0x23.byte
    val LLOAD = 0x24.byte
    val MSTORE = 0x25.byte
    val MLOAD = 0x26.byte

    val ICONST = 0x30.byte

    val IADD = 0x40.byte

    val OUT = 0x70.byte

    val ALLOC = 0x80.byte
    val FREE = 0x81.byte
}

private val Int.byte
    get() = toByte()