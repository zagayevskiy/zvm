package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.common.Address
import com.zagayevskiy.zvm.common.Opcodes.ADDB
import com.zagayevskiy.zvm.common.Opcodes.ADDI
import com.zagayevskiy.zvm.common.Opcodes.ADDSP
import com.zagayevskiy.zvm.common.Opcodes.ALLOC
import com.zagayevskiy.zvm.common.Opcodes.ANDB
import com.zagayevskiy.zvm.common.Opcodes.ANDI
import com.zagayevskiy.zvm.common.Opcodes.BTOI
import com.zagayevskiy.zvm.common.Opcodes.BTOJ
import com.zagayevskiy.zvm.common.Opcodes.CALL
import com.zagayevskiy.zvm.common.Opcodes.CMPB
import com.zagayevskiy.zvm.common.Opcodes.CMPBC
import com.zagayevskiy.zvm.common.Opcodes.CMPI
import com.zagayevskiy.zvm.common.Opcodes.CMPIC
import com.zagayevskiy.zvm.common.Opcodes.CONSTB
import com.zagayevskiy.zvm.common.Opcodes.CONSTI
import com.zagayevskiy.zvm.common.Opcodes.CRASH
import com.zagayevskiy.zvm.common.Opcodes.DECI
import com.zagayevskiy.zvm.common.Opcodes.DECSPB
import com.zagayevskiy.zvm.common.Opcodes.DECSPI
import com.zagayevskiy.zvm.common.Opcodes.DIVB
import com.zagayevskiy.zvm.common.Opcodes.DIVI
import com.zagayevskiy.zvm.common.Opcodes.DUP
import com.zagayevskiy.zvm.common.Opcodes.EQB
import com.zagayevskiy.zvm.common.Opcodes.EQI
import com.zagayevskiy.zvm.common.Opcodes.FREE
import com.zagayevskiy.zvm.common.Opcodes.GLOADB
import com.zagayevskiy.zvm.common.Opcodes.GLOADI
import com.zagayevskiy.zvm.common.Opcodes.GREATB
import com.zagayevskiy.zvm.common.Opcodes.GREATI
import com.zagayevskiy.zvm.common.Opcodes.GREQB
import com.zagayevskiy.zvm.common.Opcodes.GREQI
import com.zagayevskiy.zvm.common.Opcodes.GSTORB
import com.zagayevskiy.zvm.common.Opcodes.GSTORI
import com.zagayevskiy.zvm.common.Opcodes.INCI
import com.zagayevskiy.zvm.common.Opcodes.INCSPB
import com.zagayevskiy.zvm.common.Opcodes.INCSPI
import com.zagayevskiy.zvm.common.Opcodes.ITOB
import com.zagayevskiy.zvm.common.Opcodes.ITOJ
import com.zagayevskiy.zvm.common.Opcodes.JCALL
import com.zagayevskiy.zvm.common.Opcodes.JDEL
import com.zagayevskiy.zvm.common.Opcodes.JMP
import com.zagayevskiy.zvm.common.Opcodes.JNEW
import com.zagayevskiy.zvm.common.Opcodes.JNZ
import com.zagayevskiy.zvm.common.Opcodes.JZ
import com.zagayevskiy.zvm.common.Opcodes.LEQB
import com.zagayevskiy.zvm.common.Opcodes.LEQI
import com.zagayevskiy.zvm.common.Opcodes.LESSB
import com.zagayevskiy.zvm.common.Opcodes.LESSI
import com.zagayevskiy.zvm.common.Opcodes.LLOADB
import com.zagayevskiy.zvm.common.Opcodes.LLOADI
import com.zagayevskiy.zvm.common.Opcodes.LNOTB
import com.zagayevskiy.zvm.common.Opcodes.LSTORB
import com.zagayevskiy.zvm.common.Opcodes.LSTORI
import com.zagayevskiy.zvm.common.Opcodes.MLOADB
import com.zagayevskiy.zvm.common.Opcodes.MLOADI
import com.zagayevskiy.zvm.common.Opcodes.MODB
import com.zagayevskiy.zvm.common.Opcodes.MODI
import com.zagayevskiy.zvm.common.Opcodes.MSTORB
import com.zagayevskiy.zvm.common.Opcodes.MSTORI
import com.zagayevskiy.zvm.common.Opcodes.MULB
import com.zagayevskiy.zvm.common.Opcodes.MULI
import com.zagayevskiy.zvm.common.Opcodes.NEQB
import com.zagayevskiy.zvm.common.Opcodes.NEQI
import com.zagayevskiy.zvm.common.Opcodes.NOTB
import com.zagayevskiy.zvm.common.Opcodes.NOTI
import com.zagayevskiy.zvm.common.Opcodes.ORB
import com.zagayevskiy.zvm.common.Opcodes.ORI
import com.zagayevskiy.zvm.common.Opcodes.OUT
import com.zagayevskiy.zvm.common.Opcodes.POP
import com.zagayevskiy.zvm.common.Opcodes.PUSHFP
import com.zagayevskiy.zvm.common.Opcodes.RET
import com.zagayevskiy.zvm.common.Opcodes.RNDI
import com.zagayevskiy.zvm.common.Opcodes.SHLI
import com.zagayevskiy.zvm.common.Opcodes.SHRI
import com.zagayevskiy.zvm.common.Opcodes.STOJ
import com.zagayevskiy.zvm.common.Opcodes.SUBB
import com.zagayevskiy.zvm.common.Opcodes.SUBI
import com.zagayevskiy.zvm.common.Opcodes.XORB
import com.zagayevskiy.zvm.common.Opcodes.XORI
import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.memory.Memory
import com.zagayevskiy.zvm.memory.VMOutOfMemory
import com.zagayevskiy.zvm.util.extensions.*
import com.zagayevskiy.zvm.vm.RuntimeType.RuntimeByte
import com.zagayevskiy.zvm.vm.RuntimeType.RuntimeInt
import com.zagayevskiy.zvm.vm.StackEntry.*
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*

data class RuntimeFunction(val address: Address, val argTypesReversed: List<RuntimeType>) {
    val argsMemorySize = argTypesReversed.fold(0) { totalSize, arg -> totalSize + arg.size }
}

enum class RuntimeType(val size: Int) {
    RuntimeInt(4), RuntimeByte(1)
}

private data class StackFrame(val framePointer: Address, val previousStackPointer: Address, val returnAddress: Address)

sealed class StackEntry {

    data class VMInteger(val intValue: Int) : StackEntry() {
        override fun toString() = "i$intValue"
    }
    data class VMByte(val byteValue: Byte) : StackEntry() {
        override fun toString() = "b$byteValue"
    }

    object VMNull : StackEntry()
}

fun Int.toStackEntry() = VMInteger(this)
fun Byte.toStackEntry() = VMByte(this)

interface JavaInterop {
    fun put(obj: Any?): Int
    fun get(index: Int): Any?
    fun remove(index: Int)
}

class SimpleJavaInterop : JavaInterop {

    private val javaObjects = mutableMapOf<Int, Any?>()
    private var javaObjectIndex = 1

    override fun put(obj: Any?): Int {
        val index = javaObjectIndex++
        javaObjects[index] = obj
        return index
    }

    override fun get(index: Int): Any? = javaObjects[index]

    override fun remove(index: Int) {
        javaObjects[index] = null
    }
}

private object DisabledJavaInterop : JavaInterop {
    override fun put(obj: Any?): Int = 0
    override fun get(index: Int): Any? = null
    override fun remove(index: Int) = Unit
}

class VMStackOverflow(message: String) : Exception(message)
class VMStackUnderflow(message: String) : Exception(message)
class VMStackCorrupted(message: String) : Exception(message)
class VMStackProtected(message: String) : Exception(message)

class VirtualMachine(info: LoadedInfo, private val localsStackSize: Int = 1024, private val heap: Memory = BitTableMemory(localsStackSize), private val javaInterop: JavaInterop = DisabledJavaInterop) {

    private val functions = info.functions
    private val mainIndex = info.mainIndex
    private val bytecode: ByteArray = info.bytecode

    private val globals = (0 until info.globalsCount).map { VMNull as StackEntry }.toMutableList()

    private val localsStackBaseAddress = try {
        heap.allocate(localsStackSize)
    } catch (oom: VMOutOfMemory) {
        throw VMOutOfMemory("No enough memory to create locals stack. Wanted stack size: $localsStackSize", cause = oom)
    }

    private var stackPointer: Int = localsStackBaseAddress
        set(value) {
            when {
                value > localsStackBaseAddress + localsStackSize -> throw VMStackOverflow("Trying to increase stack size to ${value - localsStackBaseAddress} while max stack size is $localsStackSize")
                value < localsStackBaseAddress -> throw VMStackUnderflow("Trying to set sp under the base address")
                callStack.peekOrNull()?.previousStackPointer?.let { savedSp -> savedSp > value } ?: false -> throw VMStackCorrupted("Trying to set sp under current saved sp. value=$value, frame=${callStack.peek()}")
            }
            field = value
        }

    private var ip: Int = functions[mainIndex].address

    private val callStack = stack<StackFrame>()
    private val operandsStack = stack<StackEntry>()

    private val random = Random()


    fun run(args: List<StackEntry>): StackEntry {
        args.forEach { push(it) }
        call(mainIndex)
        loop()
        return pop().also { if(operandsStack.isNotEmpty()) error("Operands stack is not empty: $operandsStack") }
    }

    private fun loop() {
        while (ip < bytecode.size) {
            val code = nextByte()
            when (code) {
                CALL -> call(functionIndex = decodeNextInt())
                RET -> {
                    if (callStack.size == 1) return
                    ret()
                }
                CRASH -> {
                    throw RuntimeException("crash ${pop()}")
                }
                JCALL -> jcall(decodeNextInt())
                JNEW -> jnew(decodeNextInt())
                JDEL -> jdel()

                JMP -> jump(decodeNextInt())
                JZ -> jz()
                JNZ -> jnz()

                CONSTI -> push(decodeNextInt())
                CONSTB -> push(nextByte())

                GLOADI -> globalLoad<VMInteger>()
                GSTORI -> globalStore<VMInteger>()
                GLOADB -> globalLoad<VMByte>()
                GSTORB -> globalStore<VMByte>()


                LLOADI -> localLoadInt()
                LSTORI -> localStoreInt()
                LLOADB -> localLoadByte()
                LSTORB -> localStoreByte()

                MSTORI -> memoryStoreInt()
                MLOADI -> memoryLoadInt()
                MLOADB -> memoryLoadByte()
                MSTORB -> memoryStoreByte()

                POP -> pop()
                DUP -> push(peek())
                PUSHFP -> push(callStack.peek().framePointer)
                ADDSP -> addStackPointer(decodeNextInt())
                INCSPI -> addStackPointer(4)
                DECSPI -> addStackPointer(-4)
                INCSPB -> addStackPointer(1)
                DECSPB -> addStackPointer(-1)

                ADDI -> addi()
                SUBI -> subi()
                INCI -> inci()
                DECI -> deci()
                MULI -> muli()
                DIVI -> divi()
                MODI -> modi()
                XORI -> xori()
                ANDI -> andi()
                ORI -> ori()
                NOTI -> noti()
                SHLI -> shli()
                SHRI -> shri()
                CMPI -> cmpi()
                CMPIC -> cmpic()
                LESSI -> lessi()
                LEQI -> leqi()
                GREATI -> greati()
                GREQI -> greqi()
                EQI -> eqi()
                NEQI -> neqi()
                RNDI -> rndi()

                ADDB -> addb()
                SUBB -> subb()
                MULB -> mulb()
                DIVB -> divb()
                MODB -> modb()
                XORB -> xorb()
                ANDB -> andb()
                ORB -> orb()
                NOTB -> notb()
                CMPB -> cmpb()
                CMPBC -> cmpbc()
                LESSB -> lessb()
                LEQB -> leqb()
                GREATB -> greatb()
                GREQB -> greqb()
                EQB -> eqb()
                NEQB -> neqb()
                LNOTB -> lnotb()

                OUT -> out()

                ALLOC -> alloc()
                FREE -> free()

                BTOI -> btoi()
                ITOB -> itob()

                BTOJ -> btoj()
                ITOJ -> itoj()
                STOJ -> stoj()


                else -> error("Unknown bytecode 0x${code.toString(16)}")
            }
        }
    }

    private fun addStackPointer(value: Int) {
        stackPointer += value
    }

    private fun localLoadInt() {
        val offset = decodeNextInt()
        val frame = callStack.peek()
        val address = frame.framePointer + offset
        if (address >= stackPointer) throw VMStackProtected("Trying to load int from over sp. Address: $address, sp: $stackPointer, decoded offset: $offset, frame: $frame")
        push(heap.readInt(address))
    }

    private fun localStoreInt() {
        val offset = decodeNextInt()
        val frame = callStack.peek()
        val address = frame.framePointer + offset
        if (address + 4 > stackPointer) throw VMStackCorrupted("Trying to store int over sp. Address: $address, sp: $stackPointer, decoded offset: $offset, frame: $frame")

        val int = pop<VMInteger>().intValue
        heap.writeInt(address = address, value = int)
    }

    private fun localLoadByte() {
        val offset = decodeNextInt()
        val frame = callStack.peek()
        val address = frame.framePointer + offset
        if (address >= stackPointer) throw VMStackProtected("Trying to load byte from over sp. Address: $address, sp: $stackPointer, decoded offset: $offset, frame: $frame")
        push(heap[callStack.peek().framePointer + offset])
    }

    private fun localStoreByte() {
        val offset = decodeNextInt()
        val frame = callStack.peek()
        val address = frame.framePointer + offset
        if (address >= stackPointer) throw VMStackCorrupted("Trying to store byte over sp. Address: $address, sp: $stackPointer, decoded offset: $offset, frame: $frame")

        val byte = pop<VMByte>().byteValue
        heap[address] = byte
    }

    private fun memoryStoreInt() {
        val argument = pop<VMInteger>().intValue
        val offset = pop<VMInteger>(PopOffsetMsg).intValue
        val address = pop<VMInteger>(PopAddrMsg).intValue

        heap.writeInt(address + offset, argument)
    }

    private fun memoryLoadInt() {
        val offset = pop<VMInteger>(PopOffsetMsg).intValue
        val address = pop<VMInteger>(PopAddrMsg).intValue
        push(heap.readInt(address + offset))
    }

    private fun memoryStoreByte() {
        val argument = pop<VMByte>().byteValue
        val offset = pop<VMInteger>(PopOffsetMsg).intValue
        val address = pop<VMInteger>(PopAddrMsg).intValue

        heap[address + offset] = argument
    }

    private fun memoryLoadByte() {
        val offset = pop<VMInteger>(PopOffsetMsg).intValue
        val address = pop<VMInteger>(PopAddrMsg).intValue
        push(heap[address + offset])
    }


    private fun alloc() {
        val size = pop<VMInteger> { "alloc argument must be int, $it found" }.intValue
        if (size <= 0) error("alloc argument must be positive int, $size found")

        push(heap.allocate(size).toStackEntry())
    }

    private fun free() {
        val address = pop<VMInteger> { "free argument must be Address(int), $it found" }.intValue
        heap.free(address)
    }

    private inline fun <reified T : StackEntry> globalLoad() {
        val index = decodeNextInt()
        checkGlobalIndex(index)
        val global = globals[index]
        if (global !is T) error("Invalid global type. Has $global but ${T::class.java.simpleName} wanted.")
        push(globals[index])
    }

    private inline fun <reified T : StackEntry> globalStore() {
        val index = decodeNextInt()
        checkGlobalIndex(index)
        val value = pop<T> { "Want to store global ${T::class.java.simpleName} but has $it." }
        globals[index] = value
    }

    private fun decodeNextInt(): Int = bytecode.copyToInt(startIndex = ip).also { ip += 4 }

    private fun nextByte(): Byte = bytecode[ip++]

    //region control flow
    private fun call(functionIndex: Int) {
        val function = functions[functionIndex]
        val sp = stackPointer
        stackPointer += function.argsMemorySize
        var offset = stackPointer

        function.argTypesReversed.forEachIndexed { index, type ->
            offset -= type.size
            when (type) {
                RuntimeInt -> heap.writeInt(offset, pop<VMInteger> { "int expected as #$index arg of $function but $it found. " }.intValue)
                RuntimeByte -> heap[offset] = pop<VMByte> { "byte expected as #$index arg of $function but $it found." }.byteValue
            }

        }
        callStack.push(StackFrame(framePointer = stackPointer, previousStackPointer = sp, returnAddress = ip))
        ip = function.address
    }

    private fun ret() {
        val frame = callStack.pop()
        stackPointer = frame.previousStackPointer
        ip = frame.returnAddress
    }

    private fun jcall(operandsCount: Int) {
        val objectIndex = pop<VMInteger> { "obj name must be int" }.intValue
        val methodName = popStringUtf8()

        val operands = (1..operandsCount)
                .map { pop<VMInteger> { "arguments must be ints" }.intValue }
                .asReversed()
                .map { javaInterop.get(it) }
                .toTypedArray()
        val operandsClasses = operands.map { it?.javaClass as? Class<*> }.toTypedArray()


        val obj = javaInterop.get(objectIndex)!!
        val clazz = obj.javaClass
        val method = clazz.findMethod(methodName, operandsClasses)

        val result = method(obj, *operands)

        push(javaInterop.put(result).toStackEntry())
    }

    private fun jnew(operandsCount: Int) {
        val className = popStringUtf8()

        val operands = (1..operandsCount)
                .map { pop<VMInteger> { "arguments must be ints" }.intValue }
                .asReversed()
                .map { javaInterop.get(it) }
                .toTypedArray()

        val clazz = Class.forName(className)
        val newInstance = if (operands.isEmpty()) {
            clazz.newInstance()
        } else {
            val operandsClasses = operands.map { it?.javaClass as? Class<*> }.toTypedArray()
            val constructor = clazz.findConstructor(operandsClasses)
            constructor.newInstance(*operands)
        }

        push(javaInterop.put(newInstance).toStackEntry())
    }

    private fun Class<*>.findMethod(name: String, types: Array<Class<*>?>): Method {
        return declaredMethods.first {
            it.name == name &&
                    it.parameterCount == types.size && (0 until types.size).all { index -> matchTypes(it.parameterTypes[index], types[index]) }
        }
    }

    private fun Class<*>.findConstructor(types: Array<Class<*>?>): Constructor<*> {
        return declaredConstructors.first {
            val paramsTypes = it.parameterTypes
            it.parameterCount == types.size && (0 until types.size).all { index -> matchTypes(paramsTypes[index], types[index]) }
        }
    }

    private fun matchTypes(need: Class<*>, got: Class<*>?): Boolean {
        return when {
            got == null -> !need.isPrimitive
            need.isPrimitive -> matchPrimitiveTypes(need, got)
            else -> need.isAssignableFrom(got)
        }
    }

    private fun matchPrimitiveTypes(need: Class<*>, got: Class<*>): Boolean {
        return when (need) {
            Int::class.javaPrimitiveType -> java.lang.Integer::class.java == got
            Byte::class.javaPrimitiveType -> java.lang.Byte::class.java == got
            Boolean::class.javaPrimitiveType -> java.lang.Boolean::class.java == got
            Long::class.javaPrimitiveType -> java.lang.Long::class.java == got
            Float::class.javaPrimitiveType -> java.lang.Float::class.java == got
            Double::class.javaPrimitiveType -> java.lang.Double::class.java == got
            Char::class.javaPrimitiveType -> java.lang.Character::class.java == got
            Short::class.javaPrimitiveType -> java.lang.Short::class.java == got
            else -> false
        }
    }

    private fun jdel() {
        val deleteIndex = pop<VMInteger> { "del index must be int" }.intValue
        javaInterop.remove(deleteIndex)
    }

    private fun popStringUtf8(): String {
        val address = pop<VMInteger> { "string must be int" }.intValue
        val length = pop<VMInteger> { "string length must be int" }.intValue
        val buffer = ByteArray(length)
        heap.copyOut(source = address, count = length, destination = buffer)
        return String(buffer, Charsets.UTF_8)
    }

    private fun jz() = conditionalJump { value ->
        when (value) {
            is VMByte -> value.byteValue == 0.toByte()
            is VMInteger -> value.intValue == 0
            is StackEntry.VMNull -> true
        }
    }

    private fun jnz() = conditionalJump { value ->
        when (value) {
            is VMByte -> value.byteValue.toInt() != 0
            is VMInteger -> value.intValue != 0
            is StackEntry.VMNull -> false
        }
    }

    private inline fun conditionalJump(condition: (StackEntry) -> Boolean) {
        val address = decodeNextInt()
        val conditionArgument = pop()
        if (condition(conditionArgument)) {
            jump(address)
        }
    }

    private fun jump(address: Address) {
        ip = address
    }

    //endregion

    //region stack
    private fun pop() = operandsStack.pop()

    private inline fun <reified T : StackEntry> pop(lazyErrorMessage: (wrong: StackEntry) -> String = { "${T::class.simpleName} expected at top of the stack, but $it found" }): T {
        val value = pop()
        if (value !is T) error(lazyErrorMessage(value))
        return value
    }

    private fun push(entry: StackEntry) = operandsStack.push(entry)
    private fun push(value: Int) = push(value.toStackEntry())
    private fun push(value: Byte) = push(value.toStackEntry())

    private fun peek() = operandsStack.peek()
    //endregion

    //region int expressions
    private fun addi() = binaryIntExpr { left, right -> left + right }

    private fun subi() = binaryIntExpr { left, right -> left - right }

    private fun inci() = unaryIntExpr { it + 1 }

    private fun deci() = unaryIntExpr { it - 1 }

    private fun muli() = binaryIntExpr { left, right -> left * right }

    private fun divi() = binaryIntExpr { left, right -> left / right }

    private fun modi() = binaryIntExpr { left, right -> left % right }

    private fun xori() = binaryIntExpr { left, right -> left xor right }

    private fun andi() = binaryIntExpr { left, right -> left and right }

    private fun ori() = binaryIntExpr { left, right -> left or right }

    private fun noti() = unaryIntExpr { it.inv() }

    private fun shli() = binaryIntExpr { left, right -> left shl right }

    private fun shri() = binaryIntExpr { left, right -> left ushr right }

    private fun cmpi() {
        val right = pop<VMInteger>(PopIntExprOperandMsg).intValue
        val left = pop<VMInteger>(PopIntExprOperandMsg).intValue
        push(compareValues(left, right).toByte().toStackEntry())
    }

    private fun lessi() = compareIntExpr { left, right -> left < right }
    private fun leqi() = compareIntExpr { left, right -> left <= right }
    private fun greati() = compareIntExpr { left, right -> left > right }
    private fun greqi() = compareIntExpr { left, right -> left >= right }
    private fun eqi() = compareIntExpr { left, right -> left == right }
    private fun neqi() = compareIntExpr { left, right -> left != right }

    private fun cmpic() {
        val left = pop<VMInteger>(PopIntExprOperandMsg).intValue
        val right = decodeNextInt()
        push(compareValues(left, right).toByte().toStackEntry())
    }

    private fun rndi() = push(random.nextInt().toStackEntry())

    private inline fun unaryIntExpr(body: (Int) -> Int) {
        val value = pop<VMInteger>(PopIntExprOperandMsg).intValue
        push(body(value).toStackEntry())
    }

    private inline fun binaryIntExpr(body: (left: Int, right: Int) -> Int) {
        val right = pop<VMInteger>(PopIntExprOperandMsg).intValue
        val left = pop<VMInteger>(PopIntExprOperandMsg).intValue
        push(body(left, right).toStackEntry())
    }

    private inline fun compareIntExpr(body: (left: Int, right: Int) -> Boolean) {
        val right = pop<VMInteger>(PopIntExprOperandMsg).intValue
        val left = pop<VMInteger>(PopIntExprOperandMsg).intValue
        val result: Byte = if (body(left, right)) 1 else 0
        push(result.toStackEntry())
    }
    //endregion

    //region byte expressions
    private fun addb() = binaryByteExpr { left, right -> left + right }

    private fun subb() = binaryByteExpr { left, right -> left - right }

    private fun mulb() = binaryByteExpr { left, right -> left * right }

    private fun divb() = binaryByteExpr { left, right -> left / right }

    private fun modb() = binaryByteExpr { left, right -> left % right }

    private fun xorb() = binaryByteExpr { left, right -> left xor right }

    private fun andb() = binaryByteExpr { left, right -> left and right }

    private fun orb() = binaryByteExpr { left, right -> left or right }

    private fun notb() = unaryByteExpr { it.toInt().inv().toByte() }

    private fun cmpb() = binaryByteExpr { left, right -> compareValues(left, right) }

    private fun lessb() = compareByteExpr { left, right -> left < right }
    private fun leqb() = compareByteExpr { left, right -> left <= right }
    private fun greatb() = compareByteExpr { left, right -> left > right }
    private fun greqb() = compareByteExpr { left, right -> left >= right }
    private fun eqb() = compareByteExpr { left, right -> left == right }
    private fun neqb() = compareByteExpr { left, right -> left != right }

    private fun lnotb() = unaryByteExpr { if (it == 0.toByte()) 1 else 0 }

    private fun cmpbc() {
        TODO("not implemented")
    }

    private inline fun unaryByteExpr(body: (Byte) -> Byte) {
        val value = pop<VMByte>(PopByteExprOperandMsg).byteValue
        push(body(value).toStackEntry())
    }

    private inline fun binaryByteExpr(body: (left: Byte, right: Byte) -> Int) {
        val right = pop<VMByte>(PopByteExprOperandMsg).byteValue
        val left = pop<VMByte>(PopByteExprOperandMsg).byteValue
        push(body(left, right).toByte())
    }

    private inline fun compareByteExpr(body: (left: Byte, right: Byte) -> Boolean) {
        val right = pop<VMByte>(PopByteExprOperandMsg).byteValue
        val left = pop<VMByte>(PopByteExprOperandMsg).byteValue
        val result: Byte = if (body(left, right)) 1 else 0
        push(result.toStackEntry())
    }

    //endregion

    //region casts

    private fun btoi() = cast<VMByte, VMInteger> { it.byteValue.toInt().toStackEntry() }
    private fun itob() = cast<VMInteger, VMByte> { it.intValue.toByte().toStackEntry() }

    private inline fun <reified F : StackEntry, T : StackEntry> cast(convert: (F) -> T) {
        val value = pop<F> { "Cast error: $it is not ${F::class.simpleName}" }
        push(convert(value))
    }

    private fun btoj() = castToJava<VMByte> { it.byteValue }
    private fun itoj() = castToJava<VMInteger> { it.intValue }
    private fun stoj() {
        push(javaInterop.put(popStringUtf8()).toStackEntry())
    }

    private inline fun <reified F : StackEntry> castToJava(convert: (F) -> Any) {
        val value = pop<F> { "Java cast error: $it is not ${F::class.simpleName}" }
        push(javaInterop.put(convert(value)).toStackEntry())
    }

    //endregion

    private fun out() = pop().let { entry ->
        return@let when (entry) {
            is VMInteger -> println(entry.intValue)
            is VMByte -> println(entry.byteValue)
            is StackEntry.VMNull -> println("null")
        }
    }

    private fun checkGlobalIndex(index: Int) {
        if (index < 0 || index >= globals.size) error("Invalid global index: $index. Has ${globals.size} globals.")
    }

    private fun log(message: String) = println(message)

}

private val PopAddrMsg = { wrong: StackEntry -> "Address(int) expected at top of the stack, $wrong found." }
private val PopOffsetMsg = { wrong: StackEntry -> "Offset(int) expected at top of the stack, $wrong found." }
private val PopIntExprOperandMsg = { wrong: StackEntry -> "int expected as operand of int expression, $wrong found." }
private val PopByteExprOperandMsg = { wrong: StackEntry -> "byte expected as operand of byte expression, $wrong found." }