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
            ALoad to Opcodes.ALOAD,
            LStore to Opcodes.LSTORE,
            LLoad to Opcodes.LLOAD,

            IntConst to Opcodes.ICONST,

            IntAdd to Opcodes.IADD,

            Out to Opcodes.OUT
    )

    val opcodes = mapping.keys
}


object Jmp : OpcodeImpl(name = "jmp", operandCount = 1)

object Ret : OpcodeImpl(name = "ret")
object Call : OpcodeImpl(name = "call", operandCount = 1)

object Push : OpcodeImpl(name = "push", operandCount = 1)
object Pop : OpcodeImpl(name = "pop")
object ALoad : OpcodeImpl(name = "aload", operandCount = 1)
object LStore : OpcodeImpl(name = "lstore", operandCount = 1)
object LLoad : OpcodeImpl(name = "lload", operandCount = 1)

object IntConst : OpcodeImpl(name = "iconst", operandCount = 1)
object IntAdd : OpcodeImpl(name = "iadd")

object Out : OpcodeImpl(name = "out")