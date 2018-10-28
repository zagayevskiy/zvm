package com.zagayevskiy.zvm.common

object Opcodes {
    val JMP = 0x0.byte

    val CALL = 0x10.byte
    val RET = 0x11.byte

    val PUSH = 0x20.byte
    val POP = 0x21.byte

    //region int opcodes
    val ALOADI = 0x40.byte
    val LSTORI = 0x41.byte
    val LLOADI = 0x42.byte
    val MSTORI = 0x43.byte
    val MLOADI = 0x44.byte
    val CONSTI = 0x45.byte
    val ADDI = 0x46.byte
    val MULI = 0x47.byte
    val DIVI = 0x48.byte
    val MODI = 0x49.byte
    val XORI = 0x4A.byte
    val ANDI = 0x4B.byte
    val ORI = 0x4C.byte
    val NOTI = 0x4D.byte
    //endregion

    //region byte opcodes
    val ALOADB = 0x60.byte
    val LSTORB = 0x61.byte
    val LLOADB = 0x62.byte
    val MSTORB = 0x63.byte
    val MLOADB = 0x64.byte
    val CONSTB = 0x65.byte
    val ADDB = 0x66.byte
    val MULB = 0x67.byte
    val DIVB = 0x68.byte
    val MODB = 0x69.byte
    val XORB = 0x6A.byte
    val ANDB = 0x6B.byte
    val ORB = 0x6C.byte
    val NOTB = 0x6D.byte
    //endregion

    val OUT = 0x70.byte

    val ALLOC = 0x80.byte
    val FREE = 0x81.byte
}

private val Int.byte
    get() = toByte()