package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.memory.BitTableMemory

interface VmTestCase {
    val loadedProgram: LoadedInfo
    fun createVm(info: LoadedInfo): VirtualMachine
    val runArgs: List<StackEntry>
    val expectedResult: StackEntry
}

abstract class AbsVmTestCase(private val name: String, protected val stackSize: Int = 1024, protected val heapSize: Int = 2048): VmTestCase {

    override fun createVm(info: LoadedInfo) = VirtualMachine(info, stackSize, BitTableMemory(heapSize))

    override fun toString() = "$name : ${javaClass.simpleName}"
}

data class Source(val name: String, val text: String)