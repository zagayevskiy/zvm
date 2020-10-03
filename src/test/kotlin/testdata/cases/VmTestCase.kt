package testdata.cases

import com.zagayevskiy.zvm.vm.*
import kotlin.test.assertEquals

interface VmTestCase {
    companion object {
        const val DefaultHeapSize = 100000
    }
    val bytecode: ByteArray

    val stackSize: Int
        get() = 2048

    val heapSize: Int
        get() = DefaultHeapSize

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
    abstract val bytecodeProvider: () -> ByteArray

    final override val bytecode: ByteArray
        get() = bytecodeProvider()

    override fun toString() = name
}

internal class CachingBytecodeProvider(private val impl: () -> ByteArray): () -> ByteArray {
    private var cached: ByteArray? = null
    private var cachedException: Exception? = null
    override operator fun invoke(): ByteArray {
        cached?.let { return it }
        cachedException?.let { throw it }
        try {
            return impl().also { cached = it }
        } catch (e: Exception) {
            cachedException = e
            throw e
        }
    }
}

internal class SimpleVmTestCase(override val name: String,
                                override val bytecodeProvider: () -> ByteArray,
                                override val runArgs: List<StackEntry>,
                                private val expectedResult: StackEntry,
                                override val heapSize: Int = VmTestCase.DefaultHeapSize) : AbsVmTestCase() {

    override fun checkResult(actualResult: StackEntry) = assertEquals(expectedResult, actualResult)
}

internal class PrintVmTestCase(override val name: String,
                               override val bytecodeProvider: () -> ByteArray,
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