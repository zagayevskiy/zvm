package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.Opcodes

abstract class OpcodeImpl(override val name: String, override val operandCount: Int = 0) : Opcode

object OpcodesMapping {

    val mapping = mapOf<Opcode, Byte>(
            Jmp to Opcodes.JMP,

            Call to Opcodes.CALL,
            Ret to Opcodes.RET,

            Push to Opcodes.PUSH,
            Pop to Opcodes.POP,
            ArgLoadInt to Opcodes.ALOADI,
            LocalStoreInt to Opcodes.LSTORI,
            LocalLoadInt to Opcodes.LLOADI,

            IntConst to Opcodes.CONSTI,

            IntAdd to Opcodes.ADDI,

            Out to Opcodes.OUT,

            Alloc to Opcodes.ALLOC,
            Free to Opcodes.FREE
    )

    val opcodes = mapping.keys
}


object Jmp : OpcodeImpl(name = "jmp", operandCount = 1)

object Ret : OpcodeImpl(name = "ret")
object Call : OpcodeImpl(name = "call", operandCount = 1)

object Push : OpcodeImpl(name = "push", operandCount = 1)
object Pop : OpcodeImpl(name = "pop")

object ArgLoadInt : OpcodeImpl(name = "aloadi", operandCount = 1)
object LocalStoreInt : OpcodeImpl(name = "lstori", operandCount = 1)
object LocalLoadInt : OpcodeImpl(name = "lloadi", operandCount = 1)
object MemoryStoreInt : OpcodeImpl(name = "mstori")
object MemeryLoadInt : OpcodeImpl(name = "mloadi")

object IntConst : OpcodeImpl(name = "consti", operandCount = 1)
object IntAdd : OpcodeImpl(name = "addi")
object IntMul : OpcodeImpl(name = "muli")
object IntDiv : OpcodeImpl(name = "divi")
object IntMod : OpcodeImpl(name = "modi")
object IntXor : OpcodeImpl(name = "xori")
object IntAnd : OpcodeImpl(name = "andi")
object IntOr : OpcodeImpl(name = "ori")
object IntNot : OpcodeImpl(name = "noti")

object ArgLoadByte : OpcodeImpl(name = "aloadb", operandCount = 1)
object LocalStoreByte : OpcodeImpl(name = "lstorb", operandCount = 1)
object LocalLoadByte : OpcodeImpl(name = "lloadb", operandCount = 1)
object MemoryStoreByte : OpcodeImpl(name = "mstorb")
object MemeryLoadByte : OpcodeImpl(name = "mloadb")
object ByteConst : OpcodeImpl(name = "constb", operandCount = 1)
object ByteAdd : OpcodeImpl(name = "addb")
object ByteMul : OpcodeImpl(name = "mulb")
object ByteDiv : OpcodeImpl(name = "divb")
object ByteMod : OpcodeImpl(name = "modb")
object ByteXor : OpcodeImpl(name = "xorb")
object ByteAnd : OpcodeImpl(name = "andb")
object ByteOr : OpcodeImpl(name = "orb")
object ByteNot : OpcodeImpl(name = "notb")

object Out : OpcodeImpl(name = "out")

object Alloc: OpcodeImpl(name = "alloc")
object Free: OpcodeImpl(name = "free")