package testdata.cases

import com.zagayevskiy.zvm.vm.*
import kotlin.test.assertEquals

interface VmTestCase {
    val bytecode: ByteArray

    val stackSize: Int
        get() = 2048

    val heapSize: Int
        get() = 100000

    val io: VmIo
        get() = SimpleVmIo

    val crashHandler: CrashHandler
        get() = SimpleCrashHandler

    val runArgs: List<StackEntry>

    fun prepare() {}

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
                               private val expectPrinted: List<String>,
                               private val expectCrashCode: Int? = null) : AbsVmTestCase() {

    private lateinit var testIo: TestVmIo

    override fun prepare() {
        testIo = TestVmIo()
    }

    override val io: VmIo
        get() = testIo

    override val crashHandler: CrashHandler
        get() = expectCrashCode?.let { TestCrashHandler(it) } ?: SimpleCrashHandler

    override fun checkResult(actualResult: StackEntry) = assertEquals(expectPrinted, testIo.printed)
}

internal class TestCrashHandler(private val expectedCrashCode: Int) : CrashHandler {

    override fun handleCrash(code: Int) {
        if (expectedCrashCode != code) SimpleCrashHandler.handleCrash(code)
    }

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