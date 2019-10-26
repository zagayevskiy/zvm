package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.common.Opcodes
import com.zagayevskiy.zvm.util.BitTable
import java.lang.IllegalStateException

abstract class OpcodeImpl(override val name: String, override val operandCount: Int = 0) : Opcode {
    override fun toString() = "${javaClass.simpleName} (operands: $operandCount)"
}

object OpcodesMapping {

    val mapping = createMapping(
            Jmp to Opcodes.JMP,
            JumpZero to Opcodes.JZ,
            JumpNotZero to Opcodes.JNZ,

            Call to Opcodes.CALL,
            Invoke to Opcodes.INVOKE,
            Ret to Opcodes.RET,
            Crash to Opcodes.CRASH,

            JavaCall to Opcodes.JCALL,
            JavaNew to Opcodes.JNEW,
            JavaDelete to Opcodes.JDEL,

            Pop to Opcodes.POP,
            Dup to Opcodes.DUP,
            PushFramePointer to Opcodes.PUSHFP,
            AddStackPointer to Opcodes.ADDSP,
            IncStackPointerInt to Opcodes.INCSPI,
            DecStackPointerInt to Opcodes.DECSPI,
            IncStackPointerByte to Opcodes.INCSPB,
            DecStackPointerByte to Opcodes.DECSPB,
            PushConstantPool to Opcodes.PUSHCP,

            GlobalStoreInt to Opcodes.GSTORI,
            GlobalLoadInt to Opcodes.GLOADI,
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

            GlobalLoadByte to Opcodes.GLOADB,
            GlobalStoreByte to Opcodes.GSTORB,
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
            IntToByte to Opcodes.ITOB,
            ByteToJava to Opcodes.BTOJ,
            IntToJava to Opcodes.ITOJ,
            StringToJava to Opcodes.STOJ
    )

    val opcodes = mapping.keys

    private fun createMapping(vararg implToOpcode: Pair<Opcode, Byte>) : Map<Opcode, Byte> {
        val mapping = implToOpcode.toMap()
        implToOpcode.fold( BitTable(256)) { acc, (impl, opcode)->
            val index = opcode.toInt() and 0xff
            if (acc[index]) throw IllegalArgumentException("Opcode $opcode used twice. Last use with $impl" )
            acc.apply { set(index, true) }
        }
        mapping.keys.zip(implToOpcode) { keyImpl, (impl, _) ->
            if (keyImpl != impl) throw IllegalArgumentException("$impl used twice")
        }

        return mapping
    }
}


object Jmp : OpcodeImpl(name = "jmp", operandCount = 1)
object JumpZero : OpcodeImpl(name = "jz", operandCount = 1)
object JumpNotZero : OpcodeImpl(name = "jnz", operandCount = 1)

object Ret : OpcodeImpl(name = "ret")
object Call : OpcodeImpl(name = "call", operandCount = 1)
object Invoke : OpcodeImpl(name = "invoke")
object Crash : OpcodeImpl(name = "crash")

object JavaCall : OpcodeImpl(name = "jcall", operandCount = 1)
object JavaNew : OpcodeImpl(name = "jnew", operandCount = 1)
object JavaDelete : OpcodeImpl(name = "jdel")

object Pop : OpcodeImpl(name = "pop") //TODO make it i/b
object Dup : OpcodeImpl(name = "dup") //TODO make it i/b
object PushFramePointer : OpcodeImpl(name = "pushfp")
object AddStackPointer : OpcodeImpl(name = "addsp", operandCount = 1)
object IncStackPointerInt : OpcodeImpl(name = "incspi")
object DecStackPointerInt : OpcodeImpl(name = "decspi")
object IncStackPointerByte : OpcodeImpl(name = "incspb")
object DecStackPointerByte : OpcodeImpl(name = "decspb")
object PushConstantPool : OpcodeImpl(name = "pushcp")

object GlobalLoadInt : OpcodeImpl(name = "gloadi", operandCount = 1)
object GlobalStoreInt : OpcodeImpl(name = "gstori", operandCount = 1)
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

object GlobalLoadByte : OpcodeImpl(name = "gloadb", operandCount = 1)
object GlobalStoreByte : OpcodeImpl(name = "gstorb", operandCount = 1)
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
object ByteLogicalNot : OpcodeImpl(name = "lnotb")

object Out : OpcodeImpl(name = "out")

object Alloc : OpcodeImpl(name = "alloc")
object Free : OpcodeImpl(name = "free")

object ByteToInt : OpcodeImpl(name = "btoi")
object IntToByte : OpcodeImpl(name = "itob")
object ByteToJava : OpcodeImpl(name = "btoj")
object IntToJava : OpcodeImpl(name = "itoj")
object StringToJava : OpcodeImpl(name = "stoj")