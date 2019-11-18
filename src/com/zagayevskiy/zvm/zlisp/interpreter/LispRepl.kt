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

    private var nextId: Int = 0
        get() = field++

    fun loop() {
        val atomT = "T".atom
        val replEnv = LispEnv().apply {
            set(atomT, atomT)
            putArithmeticFunction("+", Int::plus)
            putArithmeticFunction("-", Int::minus)
            putArithmeticFunction("*", Int::times)
            putArithmeticFunction("/", Int::div)
            putFunction("=", true) { _, args ->
                val (left, right) = args.requireList(2)
                if(left == right) atomT else Sexpr.Nil
            }

            putFunction("def!", false) { env, definition ->
                val (key, value) = definition.requireList(2)
                eval(env, value).also { evaluatedValue -> env[key] = evaluatedValue }

            }

            putTcoFunction("let*", false) { env, args ->
                val (bindings, function) = args.requireList(2)

                val letEnv = LispEnv(outerEnv = env)
                bindings.asSequence().windowed(size = 2, step = 2).forEach { (name, value) ->
                    letEnv[name] = eval(letEnv, value)
                }

                letEnv to function
            }

            putFunction("fn*", false) { outerEnv, definition ->
                val (binds, body) = definition.requireList(2)
                if (binds.asSequence().any { it !is Sexpr.Atom }) throw IllegalStateException("Only atoms required in binds list")
                LispTailCallFunction("lambda#$nextId", true) { _, args ->
                    val lambdaEnv = binds.asSequence()
                            .zip(args.asSequence() + infiniteNilSequence()) { bind, arg -> bind to arg }
                            .fold(LispEnv(outerEnv)) { lambdaEnv, (bind, arg) ->
                                lambdaEnv[bind] = arg
                                lambdaEnv
                            }
                    lambdaEnv to body
                }
            }

            putTcoFunction("cond", false) { env, args ->
                args.asSequence().mapNotNull { case ->
                    val (condition, body) = case.requireList(2)
                    grabIf(eval(env, condition) != Sexpr.Nil) { env to body }
                }.firstOrNull() ?: (env to Sexpr.Nil)
            }

            putFunction("quote", false) { env, args ->
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

    private fun eval(env: LispEnv, expr: Sexpr): Sexpr {
        var currentEnv = env
        var evaluatingSexpr = expr

        do {
            when (evaluatingSexpr) {
                is Sexpr.DotPair -> when (val f = eval(currentEnv, evaluatingSexpr.head)) {
                    is LispFunction -> {
                        val args = if (f.evalArgs) {
                            evaluatingSexpr.tail.asSequence().map { arg -> eval(currentEnv, arg) }.asSexpr()
                        } else {
                            evaluatingSexpr.tail
                        }
                        return f(currentEnv, args)
                    }
                    is LispTailCallFunction -> {
                        val args = if (f.evalArgs) {
                            evaluatingSexpr.tail.asSequence().map { arg -> eval(currentEnv, arg) }.asSexpr()
                        } else {
                            evaluatingSexpr.tail
                        }
                        val (tcoEnv, tcoExpr) = f(currentEnv, args)
                        currentEnv = tcoEnv
                        evaluatingSexpr = tcoExpr
                    }
                    else -> throw IllegalStateException("can't call $f")
                }
                is Sexpr.Atom -> return currentEnv.lookup(evaluatingSexpr)
                else -> return evaluatingSexpr
            }
        } while (true)
    }

    private fun infiniteNilSequence() = Sequence {
        object : Iterator<Sexpr.Nil> {
            override fun hasNext() = true
            override fun next() = Sexpr.Nil
        }
    }

    private fun Sexpr.requireList(size: Int): List<Sexpr> {
        return asSequence().toList().takeIf { it.size == size } ?: throw IllegalStateException("$this required to be a list")
    }

    private fun LispEnv.lookup(sexpr: Sexpr) = find(sexpr) ?: throw IllegalArgumentException("Unknown symbol $sexpr")

    private fun print(sexpr: Sexpr) {
        println(sexpr.toString())
    }

    private fun LispEnv.putTcoFunction(name: String, evalArgs: Boolean, function: LispTailCallLambdaSignature) {
        set(name.atom, LispTailCallFunction(name, evalArgs, function))
    }

    private fun LispEnv.putFunction(name: String, evalArgs: Boolean, function: LispLambdaSignature) {
        set(name.atom, LispFunction(name, evalArgs, function))
    }

    private fun LispEnv.putArithmeticFunction(name: String, operator: Int.(Int) -> Int) {
        putFunction(name, true) { _: LispEnv, args: Sexpr ->

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