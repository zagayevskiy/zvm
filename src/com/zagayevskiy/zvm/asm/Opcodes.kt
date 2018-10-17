package com.zagayevskiy.zvm.asm



sealed class ByteOpcode123(override val name: String, override val operandCount: Int = 0, val bytecode: Byte) : Opcode

object MyJmp : ByteOpcode123(name = "jmp", operandCount = 1, bytecode = 0x5)
object MyRet : ByteOpcode123(name = "ret", bytecode = 0x0)
object Push : ByteOpcode123(name = "push", operandCount = 1, bytecode = 0x1)
object Pop : ByteOpcode123(name = "pop", bytecode = 0x2)
object ISum : ByteOpcode123(name = "isum", bytecode = 0x3)
object MyCall : ByteOpcode123(name = "call", operandCount = 1, bytecode = 0x4)


//FIXME TMP HACK
val byteOpcodes = listOf<Opcode>(
        MyRet,
        Push,
        Pop,
        ISum,
        MyCall,
        MyJmp
)