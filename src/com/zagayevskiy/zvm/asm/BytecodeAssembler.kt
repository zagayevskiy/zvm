package com.zagayevskiy.zvm.asm

import com.zagayevskiy.zvm.asm.Command.*
import com.zagayevskiy.zvm.util.extensions.copyTo
import com.zagayevskiy.zvm.util.extensions.copyToByteArray
import kotlin.math.max

private data class LabelDefinition(val name: String, val defined: Boolean = false, val address: Int = 0)

typealias Address = Int

class BytecodeAssembler(private val commands: List<Command>, private val opcodeMapping: Map<String, Byte>) {

    private val constantPool = ConstantPoolGenerator()
    private var bytecode = ByteArray(1024)
    private val labelDefinitions = mutableMapOf<String, LabelDefinition>()
    private val deferredLabelUsages = mutableMapOf<LabelDefinition, List<Address>>()

    private var ip = 0

    fun generate(): ByteArray {
        commands.forEach { command ->
            when (command) {
                is Func -> defineFunction(command)
                is Label -> defineLabel(command)
            }
        }

        error("TODO")
    }

    private fun defineLabel(label: Label) {
        val labelAddress = ip
        labelDefinitions[label.label]?.let { oldDefinition ->
            if (oldDefinition.defined) error("Label ${label.label} already defined")
            deferredLabelUsages.remove(oldDefinition)?.forEach { address ->
                labelAddress.copyToByteArray(bytecode, address)
            }
        }

        labelDefinitions[label.label] = LabelDefinition(label.label, true, labelAddress)
    }


    private fun defineFunction(func: Func) {
        val defined = func.run { constantPool.defineFunction(name = name, address = ip, args = args, locals = locals) }
        if (!defined) error("Function ${func.name} already defined!")
    }

    private fun moveIp(bytes: Int) {
        ip += bytes
        ensureBytecodeCapacity(ip)
    }

    private fun ensureBytecodeCapacity(capacity: Int) {
        if (bytecode.size > capacity) return
        val old = bytecode
        bytecode = ByteArray(max(old.size * 2, capacity + 1))
        old.copyTo(bytecode)
    }

    private fun error(message: String): Nothing = throw GenerationException(message)
}

class GenerationException(override val message: String) : Exception()