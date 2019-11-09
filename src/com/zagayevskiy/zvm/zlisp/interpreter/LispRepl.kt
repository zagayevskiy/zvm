package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.*

class LispRepl {

    fun loop() {
        do {
            val line = readLine() ?: return

            val lexer = ZLispLexer(line.asSequence())
            val parser = ZLispParser(lexer)
            val parsed = parser.parse()
            when (parsed) {
                is LispParseResult.Success -> parsed.program.forEach { sexpr ->
                    if (sexpr is Sexpr.Atom && sexpr.name == "exit") return
                    print(eval(sexpr))
                }
                is LispParseResult.Failure -> println(parsed.exception)
            }

        } while (true)

    }

    private fun eval(sexpr: Sexpr): Sexpr {
        return sexpr
    }
    private fun print(sexpr: Sexpr) {
        println(sexpr.toString())
    }

}

fun main(args: Array<String>) {
    val repl = LispRepl()

    repl.loop()
}