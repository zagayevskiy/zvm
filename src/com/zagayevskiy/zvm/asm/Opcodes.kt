package com.zagayevskiy.zvm.asm

abstract class OpcodeImpl(override val name: String, override val operandCount: Int = 0) : Opcode

object OpcodesMapping {

    val mapping = mapOf<Opcode, Byte>(
            Jmp to 0x0,

            Call to 0x10,
            Ret to 0x11,

            Push to 0x20,
            Pop to 0x21
    )

    val opcodes = mapping.keys
}

object Jmp : OpcodeImpl(name = "jmp", operandCount = 1)

object Ret : OpcodeImpl(name = "ret")
object Call : OpcodeImpl(name = "call", operandCount = 1)

object Push : OpcodeImpl(name = "push", operandCount = 1)
object Pop : OpcodeImpl(name = "pop")

