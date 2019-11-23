package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.common.preprocessing.CompositeIncludesResolver
import com.zagayevskiy.zvm.common.preprocessing.JavaAssetsIncludesResolver
import com.zagayevskiy.zvm.zc.ZcParser
import com.zagayevskiy.zvm.zlisp.LispParseResult
import com.zagayevskiy.zvm.zlisp.Sexpr
import com.zagayevskiy.zvm.zlisp.ZLispLexer
import com.zagayevskiy.zvm.zlisp.ZLispParser
import kotlin.math.exp

class LispInterpreter(private val text: String) {
    private val program: List<Sexpr>

    init {
        val lexer = ZLispLexer(text.asSequence())
        val parser = ZLispParser(lexer)
        val parsed = parser.parse()
        when (parsed) {
            is LispParseResult.Success -> program = parsed.program
            is LispParseResult.Failure -> throw RuntimeException(parsed.exception)
        }
    }


    fun interpret(args: String): Sexpr {
        val evaluator = LispEvaluator(CompositeIncludesResolver(listOf(JavaAssetsIncludesResolver("/includes/lisp"))))
        return program.map(evaluator::eval).last()
    }
}

fun main(args: Array<String>) {
    val interpreter = LispInterpreter("""
        (include! "decimal.lisp")

        (list-reverse (fib-decimal 1000))

    """.trimIndent())

    println(interpreter.interpret("123"))
}