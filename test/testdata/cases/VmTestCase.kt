package testdata.cases

import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.vm.LoadedInfo
import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.vm.VirtualMachine

interface VmTestCase {
    val bytecode: ByteArray

    fun createVm(info: LoadedInfo): VirtualMachine
    val runArgs: List<StackEntry>
    val expectedResult: StackEntry
}

internal class SimpleVmTestCase(private val name: String,
                                override val bytecode: ByteArray,
                                override val runArgs: List<StackEntry>, override val expectedResult: StackEntry,
                                private val stackSize: Int = 1024,
                                private val heapSize: Int = 2048) : VmTestCase {

    override fun createVm(info: LoadedInfo) = VirtualMachine(info, stackSize, BitTableMemory(heapSize))

    override fun toString() = "$name : ${javaClass.simpleName}"
}

data class TestSource(val name: String, val text: String)