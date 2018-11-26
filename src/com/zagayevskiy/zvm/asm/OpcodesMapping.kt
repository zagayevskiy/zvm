package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.Opcodes

abstract class OpcodeImpl(override val name: String, override val operandCount: Int = 0) : Opcode

object OpcodesMapping {

    val mapping = mapOf<Opcode, Byte>(
            Jmp to Opcodes.JMP,
            JumpZero to Opcodes.JZ,
            JumpNotZero to Opcodes.JNZ,
            JumpPositive to Opcodes.JPOS,
            JumpNegative to Opcodes.JNEG,

            Call to Opcodes.CALL,
            Ret to Opcodes.RET,

            Push to Opcodes.PUSH,
            Pop to Opcodes.POP,
            Dup to Opcodes.DUP,


            ArgLoadInt to Opcodes.ALOADI,
            LocalStoreInt to Opcodes.LSTORI,
            LocalLoadInt to Opcodes.LLOADI,
            MemoryStoreInt to Opcodes.MSTORI,
            MemoryLoadInt to Opcodes.MLOADI,

            IntConst to Opcodes.CONSTI,
            IntAdd to Opcodes.ADDI,
            IntSub to Opcodes.SUBI,
            IntInc to Opcodes.INCI,
            IntDec to Opcodes.DECI,
            IntMul to Opcodes.MULI,
            IntDiv to Opcodes.DIVI,
            IntMod to Opcodes.MODI,
            IntXor to Opcodes.XORI,
            IntAnd to Opcodes.ANDI,
            IntOr to Opcodes.ORI,
            IntNot to Opcodes.NOTI,
            IntShl to Opcodes.SHLI,
            IntShr to Opcodes.SHRI,
            IntCmp to Opcodes.CMPI,
            IntConstCmp to Opcodes.CMPIC,
            IntLess to Opcodes.LESSI,
            IntLessEq to Opcodes.LEQI,
            IntGreater to Opcodes.GREATI,
            IntGreaterEq to Opcodes.GREQI,
            IntEq to Opcodes.EQI,
            IntNotEq to Opcodes.NEQI,
            RandomInt to Opcodes.RNDI,

            ArgLoadByte to Opcodes.ALOADB,
            LocalStoreByte to Opcodes.LSTORB,
            LocalLoadByte to Opcodes.LLOADB,
            MemoryStoreByte to Opcodes.MSTORB,
            MemoryLoadByte to Opcodes.MLOADB,

            ByteConst to Opcodes.CONSTB,
            ByteAdd to Opcodes.ADDB,
            ByteSub to Opcodes.SUBB,
            ByteMul to Opcodes.MULB,
            ByteDiv to Opcodes.DIVB,
            ByteMod to Opcodes.MODB,
            ByteXor to Opcodes.XORB,
            ByteAnd to Opcodes.ANDB,
            ByteOr to Opcodes.ORB,
            ByteNot to Opcodes.NOTB,
            ByteCmp to Opcodes.CMPB,
            ByteLess to Opcodes.LESSB,
            ByteLessEq to Opcodes.LEQB,
            ByteGreater to Opcodes.GREATB,
            ByteGreaterEq to Opcodes.GREQB,
            ByteConstCmp to Opcodes.CMPBC,
            ByteEq to Opcodes.EQB,
            ByteNotEq to Opcodes.NEQB,
            ByteLogicalNot to Opcodes.LNOTB,

            Out to Opcodes.OUT,

            Alloc to Opcodes.ALLOC,
            Free to Opcodes.FREE,

            ByteToInt to Opcodes.BTOI,
            IntToByte to Opcodes.ITOB
    )

    val opcodes = mapping.keys
}


object Jmp : OpcodeImpl(name = "jmp", operandCount = 1)
object JumpZero : OpcodeImpl(name = "jz", operandCount = 1)
object JumpNotZero : OpcodeImpl(name = "jnz", operandCount = 1)
object JumpPositive : OpcodeImpl(name = "jpos", operandCount = 1)
object JumpNegative : OpcodeImpl(name = "jneg", operandCount = 1)

object Ret : OpcodeImpl(name = "ret")
object Call : OpcodeImpl(name = "call", operandCount = 1)

object Push : OpcodeImpl(name = "push", operandCount = 1)
object Pop : OpcodeImpl(name = "pop")
object Dup : OpcodeImpl(name = "dup")

object ArgLoadInt : OpcodeImpl(name = "aloadi", operandCount = 1)
object LocalStoreInt : OpcodeImpl(name = "lstori", operandCount = 1)
object LocalLoadInt : OpcodeImpl(name = "lloadi", operandCount = 1)
object MemoryStoreInt : OpcodeImpl(name = "mstori")
object MemoryLoadInt : OpcodeImpl(name = "mloadi")

object IntConst : OpcodeImpl(name = "consti", operandCount = 1)
object IntAdd : OpcodeImpl(name = "addi")
object IntSub : OpcodeImpl(name = "subi")
object IntInc : OpcodeImpl(name = "inci")
object IntDec : OpcodeImpl(name = "deci")
object IntMul : OpcodeImpl(name = "muli")
object IntDiv : OpcodeImpl(name = "divi")
object IntMod : OpcodeImpl(name = "modi")
object IntXor : OpcodeImpl(name = "xori")
object IntAnd : OpcodeImpl(name = "andi")
object IntOr : OpcodeImpl(name = "ori")
object IntNot : OpcodeImpl(name = "noti")
object IntShl : OpcodeImpl(name = "shli")
object IntShr : OpcodeImpl(name = "shri")
object IntCmp : OpcodeImpl(name = "cmpi")
object IntConstCmp : OpcodeImpl(name = "cmpic", operandCount = 1)
object IntLess : OpcodeImpl(name = "lessi")
object IntLessEq : OpcodeImpl(name = "leqi")
object IntGreater : OpcodeImpl(name = "greati")
object IntGreaterEq : OpcodeImpl(name = "greqi")
object IntEq : OpcodeImpl(name = "eqi")
object IntNotEq : OpcodeImpl(name = "neqi")
object RandomInt : OpcodeImpl(name = "rndi")

object ArgLoadByte : OpcodeImpl(name = "aloadb", operandCount = 1)
object LocalStoreByte : OpcodeImpl(name = "lstorb", operandCount = 1)
object LocalLoadByte : OpcodeImpl(name = "lloadb", operandCount = 1)
object MemoryStoreByte : OpcodeImpl(name = "mstorb")
object MemoryLoadByte : OpcodeImpl(name = "mloadb")
object ByteConst : OpcodeImpl(name = "constb", operandCount = 1)
object ByteAdd : OpcodeImpl(name = "addb")
object ByteSub : OpcodeImpl(name = "subb")
object ByteMul : OpcodeImpl(name = "mulb")
object ByteDiv : OpcodeImpl(name = "divb")
object ByteMod : OpcodeImpl(name = "modb")
object ByteXor : OpcodeImpl(name = "xorb")
object ByteAnd : OpcodeImpl(name = "andb")
object ByteOr : OpcodeImpl(name = "orb")
object ByteNot : OpcodeImpl(name = "notb")
object ByteCmp : OpcodeImpl(name = "cmpb")
object ByteConstCmp : OpcodeImpl(name = "cmpib", operandCount = 1)
object ByteLess : OpcodeImpl(name = "lessb")
object ByteLessEq : OpcodeImpl(name = "leqb")
object ByteGreater : OpcodeImpl(name = "greatb")
object ByteGreaterEq : OpcodeImpl(name = "greqb")
object ByteEq : OpcodeImpl(name = "eqb")
object ByteNotEq : OpcodeImpl(name = "neqb")
object ByteLogicalNot: OpcodeImpl(name = "lnotb")

object Out : OpcodeImpl(name = "out")

object Alloc : OpcodeImpl(name = "alloc")
object Free : OpcodeImpl(name = "free")

object ByteToInt : OpcodeImpl(name = "btoi")
object IntToByte : OpcodeImpl(name = "itob")