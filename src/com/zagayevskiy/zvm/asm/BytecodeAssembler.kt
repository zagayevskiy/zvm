package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.asm.Command.*
import com.zagayevskiy.zvm.asm.FunctionDefinition.Type.DefByte
import com.zagayevskiy.zvm.asm.FunctionDefinition.Type.DefInt
import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.util.extensions.copyTo
import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import kotlin.math.max

private data class LabelDefinition(val name: String, val defined: Boolean = false, val address: Int = 0, val deferredUsages: MutableList<Address> = mutableListOf())

data class FunctionDefinition(val name: String, val index: Int, val defined: Boolean = false, val address: Int = 0, val args: List<Arg> = emptyList()) {
    data class Arg(val name: String, val type: Type, val offset: Int)
    enum class Type(val size: Int) {
        DefByte(1), DefInt(4)
    }
}

class GenerationInfo(val globalsCount: Int, val functions: List<FunctionDefinition>, val bytecode: ByteArray)

class BytecodeAssembler(private val commands: List<Command>, private val opcodesMapping: Map<Opcode, Byte>) {

    private var bytecode = ByteArray(1024)

    private val labelDefinitions = mutableMapOf<String, LabelDefinition>()

    private val functionDefinitionsIndices = mutableMapOf<String, Int>()
    private val functions = mutableListOf<FunctionDefinition>()
    private var lastDefinedFunction: FunctionDefinition? = null

    private var globalsCount: Int? = null

    private var ip = 0

    fun generate(): GenerationInfo {
        commands.forEach { command ->
            when (command) {
                is Func -> defineFunction(command)
                is Label -> defineLabel(command)
                is Instruction -> addInstruction(command)
                is GlobalsDefinition -> defineGlobals(command.count)
            }
        }
        checkThatAllLabelsDefined()
        checkThatAllFunctionsDefined()

        val resultBytecode = ByteArray(ip)
        bytecode.copyTo(destination = resultBytecode, count = ip)
        return GenerationInfo(globalsCount ?: 0, functions, resultBytecode)
    }

    private fun addInstruction(command: Instruction) = command.run {
        val byte = opcodesMapping[opcode] ?: error("Unknown opcode $opcode at $command")

        write(byte)
        operands.forEach { operand ->
            when (operand) {
                is Instruction.Operand.Integer -> when (opcode) {
                    ByteConst -> writeByteConst(operand.value)
                    else -> write(operand.value)
                }
                is Instruction.Operand.Id -> when (opcode) {
                    Call -> write(functionIndex(operand.name))
                    Jmp, JumpZero, JumpNotZero -> write(obtainLabel(operand.name))
                    IntConst -> write(obtainFuncArgumentOffset(operand.name) ?: obtainFunctionIndex(operand.name) ?: TODO("May be global?"))
                    LocalLoadInt, LocalStoreInt -> write(obtainFuncArgumentOffset(operand.name, DefInt) ?: error("Arg ${operand.name} not defined."))
                    LocalLoadByte, LocalStoreByte -> write(obtainFuncArgumentOffset(operand.name, DefByte) ?: error("Arg ${operand.name} not defined."))
                    else -> error("opcode ${opcode.name} can't operate with $operand")
                }
            }
        }
    }

    private fun writeByteConst(operand: Int) {
        if (operand in (Byte.MIN_VALUE..Byte.MAX_VALUE)) {
            write(operand.toByte())
        } else {
            error("${ByteConst.name} operand must be in ${Byte.MIN_VALUE}..${Byte.MAX_VALUE}. Has $operand.")
        }
    }

    private fun obtainLabel(name: String): Address {
        val label = labelDefinitions[name] ?: (LabelDefinition(name).also { labelDefinitions[name] = it })

        if (label.defined) return label.address

        label.deferredUsages.add(ip)

        return 0
    }

    private fun obtainFuncArgumentOffset(argName: String, checkType: FunctionDefinition.Type? = null): Int? {
        val currentFunc = lastDefinedFunction ?: throw IllegalStateException("No function defined but offset for $argName arg wanted")
        return currentFunc.args.firstOrNull { it.name == argName }?.apply {
            if(checkType != null && checkType != type) error("Want $checkType for arg $argName but $type found")
        }?.offset
    }

    private fun obtainFunctionIndex(name: String) = functionDefinitionsIndices[name]

    private fun defineLabel(label: Label) {
        val labelAddress = ip
        labelDefinitions[label.label]?.let { oldDefinition ->
            if (oldDefinition.defined) error("Label ${label.label} already defined")
            oldDefinition.deferredUsages.forEach { address ->
                labelAddress.copyToByteArray(bytecode, address)
            }
            oldDefinition.deferredUsages.clear()
        }

        labelDefinitions[label.label] = LabelDefinition(label.label, true, labelAddress)
    }


    private fun defineFunction(func: Func) {
        val index = functionIndex(func.name)
        val existed = functions[index]
        if (existed.defined) error("Function ${func.name} already defined!")
        checkThatAllLabelsDefined()
        labelDefinitions.clear()
        val defined = existed.copy(defined = true, address = ip, args =  func.args.toDefinitionArgs())
        functions[index] = defined
        lastDefinedFunction = defined
    }

    private fun functionIndex(name: String): Int {
        val existedIndex = functionDefinitionsIndices[name]
        if (existedIndex == null) {
            val index = functions.size
            functions.add(FunctionDefinition(name = name, index = index))
            functionDefinitionsIndices[name] = index
            return index
        }

        return existedIndex
    }

    private fun defineGlobals(count: Int) {
        if (globalsCount != null) error("Globals count must not be assigned twice")
        globalsCount = count
    }

    private fun write(value: Byte) {
        bytecode[ip] = value
        incIp(1)
    }

    private fun write(value: Int) {
        ensureBytecodeCapacity(ip + 4)
        value.copyToByteArray(bytecode, ip)
        incIp(4)
    }

    private fun incIp(inc: Int) {
        ip += inc
        ensureBytecodeCapacity(ip)
    }

    private fun ensureBytecodeCapacity(capacity: Int) {
        if (bytecode.size > capacity) return
        val old = bytecode
        bytecode = ByteArray(max(old.size * 2, capacity + 1))
        old.copyTo(destination = bytecode)
    }

    private fun checkThatAllLabelsDefined() {
        labelDefinitions.values.firstOrNull { !it.defined }?.let { error("Label \"${it.name}\" used but not defined.") }
    }

    private fun checkThatAllFunctionsDefined() {
        functions.firstOrNull { !it.defined }?.let { error("Function \"${it.name}\" called but not defined.") }
    }

    private fun error(message: String): Nothing = throw GenerationException(message)
}

class GenerationException(override val message: String) : Exception()

private fun List<Func.Arg>.toDefinitionArgs(): List<FunctionDefinition.Arg> {
    var offset = 0
    return asReversed().map { f ->
        val type = f.type.toDefinitionType()
        offset -= type.size
        FunctionDefinition.Arg(f.name, type, offset)
    }.asReversed()
}

private fun String.toDefinitionType() = when (this) {
    "int" -> DefInt
    "byte" -> DefByte
    else -> throw IllegalArgumentException("Unknown type name $this")
}