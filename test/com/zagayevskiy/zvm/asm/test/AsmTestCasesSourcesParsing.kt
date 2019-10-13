package com.zagayevskiy.zvm.asm.test

import com.zagayevskiy.zvm.asm.AsmParser
import com.zagayevskiy.zvm.asm.AsmSequenceLexer
import com.zagayevskiy.zvm.asm.OpcodesMapping
import com.zagayevskiy.zvm.asm.ParseResult
import com.zagayevskiy.zvm.vm.Source
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import testsrc.AsmTestCases

@RunWith(Parameterized::class)
class AsmTestCasesSourcesParsing(private val source: Source) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = AsmTestCases.Sources
    }

    @Test
    fun test() {
        val parser = AsmParser(AsmSequenceLexer(source.text.asSequence()), OpcodesMapping.opcodes)
        val parsed = parser.program()
        parsed as? ParseResult.Success ?: throw IllegalArgumentException("Failed to parse $parsed")
    }
}