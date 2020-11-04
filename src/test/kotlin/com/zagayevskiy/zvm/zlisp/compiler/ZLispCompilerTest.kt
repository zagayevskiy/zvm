package com.zagayevskiy.zvm.zlisp.compiler

import com.zagayevskiy.zvm.zlisp.compiler.ZLispCompiler
import org.junit.Test

class ZLispCompilerTest {

    @Test
    fun test() {
        val program = """
            (defun test (x) (+ x 1))
        """.trimIndent()

        ZLispCompiler().compile(program)
    }

}