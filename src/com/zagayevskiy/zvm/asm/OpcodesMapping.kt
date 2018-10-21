package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.Opcodes

abstract class OpcodeImpl(override val name: String, override val operandCount: Int = 0) : Opcode

object OpcodesMapping {

    val mapping = mapOf<Opcode, Byte>(
            Jmp to Opcodes.Jmp,

            Call to Opcodes.Call,
            Ret to Opcodes.Ret,

            Push to Opcodes.Push,
            Pop to Opcodes.Pop
    )

    val opcodes = mapping.keys
}


object Jmp : OpcodeImpl(name = "jmp", operandCount = 1)

object Ret : OpcodeImpl(name = "ret")
object Call : OpcodeImpl(name = "call", operandCount = 1)

object Push : OpcodeImpl(name = "push", operandCount = 1)
object Pop : OpcodeImpl(name = "pop")
