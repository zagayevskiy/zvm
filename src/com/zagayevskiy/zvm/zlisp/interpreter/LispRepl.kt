package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.*
import java.lang.IllegalArgumentException

private val String.atom: Sexpr.Atom
    get() = Sexpr.Atom(this)

private operator fun Sexpr.iterator(): Iterator<Sexpr> = object : Iterator<Sexpr> {
    private var cursor: Sexpr = this@iterator
    override fun hasNext() = cursor is Sexpr.DotPair

    override fun next(): Sexpr {
        return when (val c = cursor) {
            is Sexpr.DotPair -> {
                cursor = c.tail
                c.head
            }
            else -> throw NoSuchElementException(cursor.toString())
        }
    }
}

private fun Sexpr.asSequence() = iterator().asSequence()
private fun Sequence<Sexpr>.asSexpr(): Sexpr {
    val iter = iterator()
    if (!iter.hasNext()) {
        return Sexpr.Nil
    }
    val head = iter.next()
    val tail: Sequence<Sexpr> = iter.asSequence()
    return head dot tail.asSexpr()
}

class LispRepl {

    fun loop() {
        val env = LispEnvironment()
        env["+".atom] = arithmetic(Int::plus)
        env["-".atom] = arithmetic(Int::minus)
        env["*".atom] = arithmetic(Int::times)
        env["/".atom] = arithmetic(Int::div)

        do {
            val line = readLine() ?: return

            val lexer = ZLispLexer(line.asSequence())
            val parser = ZLispParser(lexer)
            val parsed = parser.parse()
            when (parsed) {
                is LispParseResult.Success -> parsed.program.forEach { sexpr ->
                    if (sexpr is Sexpr.Atom && sexpr.name == "exit") return
                    print(eval(env, sexpr))
                }
                is LispParseResult.Failure -> println(parsed.exception)
            }

        } while (true)

    }

    private fun eval(env: LispEnvironment, sexpr: Sexpr): Sexpr {
        return when (sexpr) {
            is Sexpr.DotPair -> env[sexpr.head]?.let { f ->
                f(env, sexpr.tail.asSequence().map { arg -> eval(env, arg) }.asSexpr())
            } ?: throw IllegalArgumentException("Unknown symbol ${sexpr.head}")
            else -> sexpr
        }
    }

    private fun print(sexpr: Sexpr) {
        println(sexpr.toString())
    }

    private fun arithmetic(operator: Int.(Int) -> Int) = { _: LispEnvironment, args: Sexpr ->

        val ints = args.asSequence().map {
            when (it) {
                is Sexpr.Number -> it.value
                else -> throw IllegalArgumentException("$it can't be used in arithmetic function")
            }
        }

        Sexpr.Number(ints.reduce { acc, operand -> acc.operator(operand) })
    }

}

fun main(args: Array<String>) {
    val repl = LispRepl()

    repl.loop()
}