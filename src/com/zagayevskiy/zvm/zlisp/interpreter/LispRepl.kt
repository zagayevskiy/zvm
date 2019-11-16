package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.util.grabIf
import com.zagayevskiy.zvm.zlisp.*

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
        val replEnv = LispEnv().apply {
            putArithmetic("+", Int::plus)
            putArithmetic("-", Int::minus)
            putArithmetic("*", Int::times)
            putArithmetic("/", Int::div)

            put("def!", false) { env, definition ->
                val (key, value) = definition.requireList(2)
                eval(env, value).also { evaluatedValue -> env[key] = evaluatedValue }

            }

            put("let*", false) { env, args ->
                val (bindings, function) = args.requireList(2)

                val letEnv = LispEnv(outerEnv = env)
                bindings.asSequence().windowed(size = 2, step = 2).forEach { (name, value) ->
                    letEnv[name] = eval(letEnv, value)
                }

                eval(letEnv, function)
            }

            put("cond", false) { env, args ->
                args.asSequence().mapNotNull { case ->
                    val (condition, body) = case.requireList(2)
                    grabIf(eval(env, condition) != Sexpr.Nil) { eval(env, body) }
                }.firstOrNull() ?: Sexpr.Nil
            }

//            put("fn*", false) { env, definition ->
//
//            }

            put("quote", false) { env, args ->
                args.requireList(1).first()
            }
        }

        do {
            val line = readLine() ?: return

            val lexer = ZLispLexer(line.asSequence())
            val parser = ZLispParser(lexer)
            val parsed = parser.parse()
            when (parsed) {
                is LispParseResult.Success -> parsed.program.forEach { sexpr ->
                    if (sexpr is Sexpr.Atom && sexpr.name == "exit") return
                    print(eval(replEnv, sexpr))
                }
                is LispParseResult.Failure -> println(parsed.exception)
            }

        } while (true)

    }

    private fun eval(env: LispEnv, sexpr: Sexpr): Sexpr {
        return when (sexpr) {
            is Sexpr.DotPair -> when (val f = env.lookup(sexpr.head)) {
                is LispEnv.Entity.BuiltInFunc -> {
                    val args = if (f.evalArgs) {
                        sexpr.tail.asSequence().map { arg -> eval(env, arg) }.asSexpr()
                    } else {
                        sexpr.tail
                    }
                    f(env, args)
                }
                else -> throw IllegalStateException("can't call $f")
            }
            is Sexpr.Atom -> when (val resolved = env.lookup(sexpr)) {
                is LispEnv.Entity.BuiltInFunc -> throw IllegalStateException("$resolved can not be evaluated directly")
                is LispEnv.Entity.Expr -> resolved.sexpr
            }

            else -> sexpr
        }
    }

    private fun Sexpr.requireList(size: Int): List<Sexpr> {
        return asSequence().toList().takeIf { it.size == size } ?: throw IllegalStateException("$this required to be a list")
    }

    private fun LispEnv.lookup(sexpr: Sexpr) = get(sexpr) ?: throw IllegalArgumentException("Unknown symbol $sexpr")

    private fun print(sexpr: Sexpr) {
        println(sexpr.toString())
    }

    private fun LispEnv.put(name: String, evalArgs: Boolean, function: BuiltInFunctionSignature) {
        set(name.atom, LispEnv.Entity.BuiltInFunc(name, evalArgs, function))
    }

    private fun LispEnv.putArithmetic(name: String, operator: Int.(Int) -> Int) {
        put(name, true) { _: LispEnv, args: Sexpr ->

            val ints = args.asSequence().map {
                when (it) {
                    is Sexpr.Number -> it.value
                    else -> throw IllegalArgumentException("$it can't be used in arithmetic function")
                }
            }

            Sexpr.Number(ints.reduce { acc, operand -> acc.operator(operand) })
        }
    }

}

fun main(args: Array<String>) {
    val repl = LispRepl()

    repl.loop()
}