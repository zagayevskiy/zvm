package testdata.cases

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.entries
import com.zagayevskiy.zvm.util.extensions.and
import com.zagayevskiy.zvm.util.extensions.or
import com.zagayevskiy.zvm.util.extensions.xor
import com.zagayevskiy.zvm.vm.*
import testdata.sources.asm.AsmFactorial
import testdata.sources.asm.AsmFibonacci
import testdata.sources.asm.AsmReverse
import testdata.sources.asm.AsmSimple.JustRet0
import testdata.sources.asm.AsmSimple.JustRetArg


object AsmTestCases : MutableList<VmTestCase> by mutableListOf() {
    val Sources = mutableListOf<TestSource>()

    init {
        binarySources {
            IntAdd ireturns { left, right -> left + right }
            IntMul ireturns { left, right -> left * right }
            IntXor ireturns { left, right -> left xor right }
            IntOr ireturns { left, right -> left or right }
            IntAnd ireturns { left, right -> left and right }
            IntMul ireturns { left, right -> left * right }
            IntMul ireturns { left, right -> left * right }

            ByteAdd breturns { left, right -> left + right }
            ByteMul breturns { left, right -> left * right }
            ByteXor breturns { left, right -> left xor right }
            ByteOr breturns { left, right -> left or right }
            ByteAnd breturns { left, right -> left and right }
            ByteMul breturns { left, right -> left * right }
            ByteMul breturns { left, right -> left * right }
        }

        source(JustRet0) {
            run(args = No, ret = 0)
        }

        source(JustRetArg) {
            run(arg = 100500, ret = 100500)
        }

        source(AsmFactorial.Recursive) {
            run(arg = 1, ret = 1)
            run(arg = 2, ret = 2)
            run(arg = 5, ret = 120)
            run(arg = 12, ret = 479001600)
        }

        source(AsmFactorial.Iterative) {
            run(arg = 1, ret = 1)
            run(arg = 2, ret = 2)
            run(arg = 5, ret = 120)
            run(arg = 12, ret = 479001600)
        }

        source(AsmFibonacci.Recursive) {
            run(arg = 1, ret = 1)
            run(arg = 5, ret = 5)
            run(arg = 9, ret = 34)
            run(arg = 12, ret = 144)
            run(arg = 33, ret = 3524578)
            run(arg = 41, ret = 165580141)
        }

        source(AsmFibonacci.Iterative) {
            run(arg = -100, ret = 1)
            run(arg = 1, ret = 1)
            run(arg = 5, ret = 5)
            run(arg = 9, ret = 34)
            run(arg = 12, ret = 144)
            run(arg = 33, ret = 3524578)
            run(arg = 41, ret = 165580141)
        }

        source(AsmReverse.IntBits) {
            run(arg = 0b01010101_11110000_11100110_11001100, ret = 0b00110011_01100111_00001111_10101010)
            run(arg = 0b00110011_01100111_00001111_10101010, ret = 0b01010101_11110000_11100110_11001100)
        }

        source(AsmReverse.IntBytes.ViaBitOps) {
            run(arg = 0x0a1b2c3d, ret = 0x3d2c1b0a)
            run(arg = 0x3d2c1b0a, ret = 0x0a1b2c3d)
            run(arg = 0b01010101_11110000_00001111_00110011, ret = 0b00110011_00001111_11110000_01010101)
            run(arg = 0b00110011_00001111_11110000_01010101, ret = 0b01010101_11110000_00001111_00110011)
        }

        source(AsmReverse.IntBytes.ViaMemory) {
            run(arg = 0x0a1b2c3d, ret = 0x3d2c1b0a)
            run(arg = 0x3d2c1b0a, ret = 0x0a1b2c3d)
            run(arg = 0b01010101_11110000_00001111_00110011, ret = 0b00110011_00001111_11110000_01010101)
            run(arg = 0b00110011_00001111_11110000_01010101, ret = 0b01010101_11110000_00001111_00110011)
        }
    }
}

private object No : List<StackEntry> by emptyList()

private class AsmRunBuilder(val source: TestSource) {
    private val precompiledProgram: LoadedInfo

    init {
        val parser = AsmParser(AsmSequenceLexer(source.text.asSequence()), OpcodesMapping.opcodes)
        val parsed = parser.program()
        val result = parsed as? ParseResult.Success ?: throw IllegalArgumentException("Failed to parse $parsed")
        val assembler = BytecodeAssembler(result.commands, OpcodesMapping.mapping)
        val generator = BytecodeGenerator()
        val bytecode = generator.generate(assembler.generate())
        val loader = BytecodeLoader(bytecode)
        precompiledProgram = (loader.load() as LoadingResult.Success).info
    }

    fun run(arg: Int, ret: Int) = run(args = entries(arg), ret = ret.toStackEntry())

    fun run(args: List<StackEntry>, ret: Int) = run(args, ret.toStackEntry())

    fun run(args: List<StackEntry>, ret: StackEntry) {
        AsmTestCases.add(SimpleVmTestCase(
                """Asm ${source.name} ${(args.map { it.toString() }.takeIf { it.isNotEmpty() } ?: "") } -> $ret"""",
                precompiledProgram,
                runArgs = args,
                expectedResult = ret
        ))
    }
}

private fun source(source: TestSource, block: AsmRunBuilder.() -> Unit) {
    AsmTestCases.Sources.add(source)
    AsmRunBuilder(source).block()
}

private object BinarySourcesBuilder {
    private val binaryTestsIntArguments = listOf(
            entries(0, 0),
            entries(0, 1),
            entries(1, 0),
            entries(Int.MIN_VALUE, Int.MAX_VALUE),
            entries(10000, -10000)
    )

    private val binaryTestsByteArguments = listOf(
            entries(0.toByte(), 0.toByte()),
            entries(0.toByte(), 1.toByte()),
            entries(1.toByte(), 0.toByte()),
            entries(Byte.MIN_VALUE, Byte.MAX_VALUE),
            entries((-10).toByte(), 10.toByte())
    )


    infix fun Opcode.ireturns(ret: (left: Int, right: Int) -> Int) {
        source(binaryIntSource()) {
            binaryTestsIntArguments.forEach { args ->
                val (left, right) = args
                run(args = args, ret = ret((left as StackEntry.VMInteger).intValue, (right as StackEntry.VMInteger).intValue))
            }
        }
    }

    infix fun Opcode.breturns(ret: (left: Byte, right: Byte) -> Int) {
        source(binaryByteSource()) {
            binaryTestsByteArguments.forEach { args ->
                val (left, right) = args
                run(args = args, ret = ret((left as StackEntry.VMByte).byteValue, (right as StackEntry.VMByte).byteValue).toByte().toInt())
            }
        }
    }
}

private fun binarySources(block: BinarySourcesBuilder.() -> Unit) {
    BinarySourcesBuilder.block()
}

private fun Opcode.binaryIntSource() = TestSource("Binary $name", """
        .fun main: left: int, right: int;
        lloadi left
        lloadi right
        $name
        ret
    """.trimIndent())

private fun Opcode.binaryByteSource() = TestSource("Binary $name", """
        .fun main: left: byte, right: byte;
        lloadb left
        lloadb right
        $name
        btoi
        ret
    """.trimIndent())