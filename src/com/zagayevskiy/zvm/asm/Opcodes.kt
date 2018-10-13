package com.zagayevskiy.zvm.asm

sealed class ByteOpcode(override val name: String, override val operandCount: Int = 0, val bytecode: Byte) : Opcode

object Ret : ByteOpcode(name = "ret", bytecode = 0x0)
object Push : ByteOpcode(name = "push", operandCount = 1, bytecode = 0x1)
object Pop : ByteOpcode(name = "pop", bytecode = 0x2)
object ISum : ByteOpcode(name = "isum", bytecode = 0x3)
object Call : ByteOpcode(name = "call", operandCount = 1, bytecode = 0x4)
object Jmp : ByteOpcode(name = "jmp", operandCount = 1, bytecode = 0x5)

