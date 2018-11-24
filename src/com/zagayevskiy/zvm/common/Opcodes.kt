package com.zagayevskiy.zvm.common

object Opcodes {
    //region control flow opcodes
    const val CALL: Byte = 0x10
    const val RET: Byte = 0x11
    const val JMP: Byte = 0x12
    const val JZ: Byte = 0x13
    const val JNZ: Byte = 0x14
    const val JPOS: Byte = 0x15
    const val JNEG: Byte = 0x16
    //endregion

    const val PUSH: Byte = 0x20
    const val POP: Byte = 0x21

    //region casts opcodes
    const val ITOB: Byte = 0x30
    const val BTOI: Byte = 0x31
    //endregion

    //region int opcodes
    const val ALOADI: Byte = 0x40
    const val LSTORI: Byte = 0x41
    const val LLOADI: Byte = 0x42
    const val MSTORI: Byte = 0x43
    const val MLOADI: Byte = 0x44
    const val CONSTI: Byte = 0x45
    const val ADDI: Byte = 0x46
    const val SUBI: Byte = 0x47
    const val INCI: Byte = 0x48
    const val DECI: Byte = 0x49
    const val MULI: Byte = 0x4A
    const val DIVI: Byte = 0x4B
    const val MODI: Byte = 0x4C
    const val XORI: Byte = 0x4D
    const val ANDI: Byte = 0x4E
    const val ORI: Byte = 0x4F
    const val NOTI: Byte = 0x50
    const val SHLI: Byte = 0x51
    const val SHRI: Byte = 0x52
    const val CMPI: Byte = 0x53
    const val CMPIC: Byte = 0x54
    const val LESSI: Byte = 0x55
    const val LEQI: Byte = 0x56
    const val GREATI: Byte = 0x57
    const val GREQI: Byte = 0x58
    const val RNDI: Byte = 0x60
    //endregion

    //region byte opcodes
    const val ALOADB: Byte = 0x60
    const val LSTORB: Byte = 0x61
    const val LLOADB: Byte = 0x62
    const val MSTORB: Byte = 0x63
    const val MLOADB: Byte = 0x64
    const val CONSTB: Byte = 0x65
    const val ADDB: Byte = 0x66
    const val SUBB: Byte = 0x67
    const val MULB: Byte = 0x68
    const val DIVB: Byte = 0x69
    const val MODB: Byte = 0x6A
    const val XORB: Byte = 0x6B
    const val ANDB: Byte = 0x6C
    const val ORB: Byte = 0x6D
    const val NOTB: Byte = 0x6E
    const val CMPB: Byte = 0x6F
    const val CMPBC: Byte = 0x70
    const val LESSB: Byte = 0x71
    const val LEQB: Byte = 0x72
    const val GREATB: Byte = 0x73
    const val GREQB: Byte = 0x74
    const val LNOTB: Byte = 0x75
    //endregion

    const val OUT: Byte = -0x1

    //region memory opcodes
    const val ALLOC: Byte = -0x2
    const val FREE: Byte = -0x3
    //endregion

}