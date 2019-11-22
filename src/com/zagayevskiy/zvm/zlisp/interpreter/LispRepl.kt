package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.LispParseResult
import com.zagayevskiy.zvm.zlisp.Sexpr
import com.zagayevskiy.zvm.zlisp.ZLispLexer
import com.zagayevskiy.zvm.zlisp.ZLispParser


class LispRepl {
    fun loop() {
        val evaluator = LispEvaluator()

        do {
            val line = readLine() ?: return

            val lexer = ZLispLexer(line.asSequence())
            val parser = ZLispParser(lexer)
            val parsed = parser.parse()
            when (parsed) {
                is LispParseResult.Success -> parsed.program.forEach { sexpr ->
                    if (sexpr is Sexpr.Atom && sexpr.name == "exit") return
                    print(evaluator.eval(sexpr))
                }
                is LispParseResult.Failure -> println(parsed.exception)
            }

        } while (true)
    }

    private fun print(sexpr: Sexpr) {
        println(sexpr.toString())
    }
}

fun main(args: Array<String>) {
    val repl = LispRepl()

    repl.loop()
}