package com.zagayevskiy.zvm.common

object Opcodes {
    //region control flow opcodes
    const val CALL: Byte = 1
    const val RET: Byte = 2
    const val JMP: Byte = 3
    const val JZ: Byte = 4
    const val JNZ: Byte = 5
    const val CRASH: Byte = 6
    //endregion

    //region java interop opcodes
    const val JCALL: Byte = -1
    const val JNEW: Byte = -2
    const val JDEL: Byte = -3
    //endregion

    const val POP: Byte = 12
    const val DUP: Byte = 13

    //region casts opcodes
    const val ITOB: Byte = 21
    const val BTOI: Byte = 22
    const val ITOJ: Byte = 23
    const val BTOJ: Byte = 24
    const val STOJ: Byte = 25
    //endregion

    //region int opcodes
    const val ALOADI: Byte = 40
    const val LSTORI: Byte = 41
    const val LLOADI: Byte = 42
    const val MSTORI: Byte = 43
    const val MLOADI: Byte = 44
    const val CONSTI: Byte = 45
    const val ADDI: Byte = 46
    const val SUBI: Byte = 47
    const val INCI: Byte = 48
    const val DECI: Byte = 49
    const val MULI: Byte = 50
    const val DIVI: Byte = 51
    const val MODI: Byte = 52
    const val XORI: Byte = 53
    const val ANDI: Byte = 54
    const val ORI: Byte = 55
    const val NOTI: Byte = 56
    const val SHLI: Byte = 57
    const val SHRI: Byte = 58
    const val CMPI: Byte = 59
    const val CMPIC: Byte = 60
    const val LESSI: Byte = 61
    const val LEQI: Byte = 62
    const val GREATI: Byte = 63
    const val GREQI: Byte = 64
    const val EQI: Byte = 65
    const val NEQI: Byte = 66
    const val RNDI: Byte = 67
    const val GLOADI: Byte = 68
    const val GSTORI: Byte = 69
    //endregion

    //region byte opcodes
    const val ALOADB: Byte = -40
    const val LSTORB: Byte = -41
    const val LLOADB: Byte = -42
    const val MSTORB: Byte = -43
    const val MLOADB: Byte = -44
    const val CONSTB: Byte = -45
    const val ADDB: Byte = -46
    const val SUBB: Byte = -47
    const val MULB: Byte = -48
    const val DIVB: Byte = -49
    const val MODB: Byte = -50
    const val XORB: Byte = -51
    const val ANDB: Byte = -52
    const val ORB: Byte = -53
    const val NOTB: Byte = -54
    const val CMPB: Byte = -55
    const val CMPBC: Byte = -56
    const val LESSB: Byte = -57
    const val LEQB: Byte = -58
    const val GREATB: Byte = -59
    const val GREQB: Byte = -60
    const val EQB: Byte = -61
    const val NEQB: Byte = -62
    const val LNOTB: Byte = -63
    const val GLOADB: Byte = -64
    const val GSTORB: Byte = -65
    //endregion

    const val OUT: Byte = -10

    //region memory opcodes
    const val ALLOC: Byte = -11
    const val FREE: Byte = -12
    //endregion

}