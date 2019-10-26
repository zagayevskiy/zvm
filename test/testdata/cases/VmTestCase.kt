package testdata.cases

import com.zagayevskiy.zvm.vm.SimpleVmIo
import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.vm.VmIo
import kotlin.test.assertEquals

interface VmTestCase {
    val bytecode: ByteArray

    val stackSize: Int
        get() = 1024

    val heapSize: Int
        get() = 10000

    val io: VmIo
        get() = SimpleVmIo

    val runArgs: List<StackEntry>

    fun checkResult(actualResult: StackEntry)
}

internal abstract class AbsVmTestCase : VmTestCase {
    abstract val name: String

    override fun toString() = name
}

internal class SimpleVmTestCase(override val name: String,
                                override val bytecode: ByteArray,
                                override val runArgs: List<StackEntry>,
                                private val expectedResult: StackEntry) : AbsVmTestCase() {

    override fun checkResult(actualResult: StackEntry) = assertEquals(expectedResult, actualResult)
}

internal class PrintVmTestCase(override val name: String,
                                     override val bytecode: ByteArray,
                                     override val runArgs: List<StackEntry>,
                                     private val expectPrinted: List<String>) : AbsVmTestCase() {

    override val io = TestVmIo()

    override fun checkResult(actualResult: StackEntry) = assertEquals(expectPrinted, io.printed)
}

internal class TestVmIo : VmIo {
    private val printedField = mutableListOf<String>()
    val printed: List<String>
        get() = printedField

    override fun print(value: String) {
        printedField.add(value)
    }

}

data class TestSource(val name: String, val text: String)