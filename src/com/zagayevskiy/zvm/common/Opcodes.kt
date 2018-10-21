package com.zagayevskiy.zvm.common

object Opcodes {
    val Jmp = 0x0.byte

    val Call = 0x10.byte
    val Ret = 0x11.byte

    val Push = 0x20.byte
    val Pop = 0x21.byte
}

private val Int.byte
    get() = toByte()