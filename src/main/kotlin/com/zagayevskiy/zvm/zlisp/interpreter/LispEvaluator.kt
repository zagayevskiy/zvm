package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.common.preprocessing.IncludesResolver
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

class LispEvaluator(private val includesResolver: IncludesResolver) {

    private var nextId: Int = 0
        get() = field++

    private val atomT = "T".atom

    private val globalEnv = LispEnv().apply {
        set(atomT, atomT)
        putArithmeticFunction("+", Int::plus)
        putArithmeticFunction("-", Int::minus)
        putArithmeticFunction("*", Int::times)
        putArithmeticFunction("/", Int::div)
        putArithmeticFunction("%", Int::rem)
        putFunction("<", true) { _, args ->
            val (left, right) = args.requireList(2)
            if ((left as Sexpr.Number).value < (right as Sexpr.Number).value) atomT else Sexpr.Nil
        }
        putFunction("=", true) { _, args ->
            val (left, right) = args.requireList(2)
            if(left == right) atomT else Sexpr.Nil
        }
        putFunction("&&", true) { _, args ->
            if (args.asSequence().any { it == Sexpr.Nil }) Sexpr.Nil else atomT
        }
        putFunction("||", true) { _, args ->
            if (args.asSequence().any { it != Sexpr.Nil }) atomT else Sexpr.Nil
        }

        putFunction("def!", false, ::evalDef)

        putFunction("include!", true) { _, args ->
            args.asSequence().map { path ->
                val pathString = path as Sexpr.Str
                val lexer = ZLispLexer(includesResolver.resolve(pathString.value)?.asSequence() ?: throw IllegalStateException("Can'r resolve include ${pathString.value}"))
                val parser = ZLispParser(lexer)
                val parsed = parser.parse()
                when (parsed) {
                    is LispParseResult.Success -> parsed.program
                    is LispParseResult.Failure -> throw RuntimeException(parsed.exception)
                }
                parsed.program.map { expr -> eval(expr) }.lastOrNull() ?: Sexpr.Nil
            }.lastOrNull() ?: Sexpr.Nil

        }

        putTcoFunction("let*", false) { env, args ->
            val (bindings, function) = args.requireList(2)

            val letEnv = LispEnv(outerEnv = env)
            bindings.asSequence().windowed(size = 2, step = 2).forEach { (name, value) ->
                letEnv[name] = eval(letEnv, value)
            }

            letEnv to function
        }

        putFunction("fn*", false, ::evalLambdaDefinition)

        putFunction("defun", false) { env, args ->
            val (name, binds, body) = args.requireList(3)

            evalDef(env, name dot (evalLambdaDefinition(env, binds dot (body dot Sexpr.Nil)) dot Sexpr.Nil))
        }

        putFunction("list", true) {_, args -> args }
        putFunction("car", true) {_, args ->
            (args.requireList(1).first() as Sexpr.DotPair).head
        }
        putFunction("cdr", true) {_, args ->
            (args.requireList(1).first() as Sexpr.DotPair).tail
        }
        putFunction("cons", true) {_, args ->
            val (head, tail) = args.requireList(2)
            (head dot tail)
        }

        putFunction("nil?", true) {_, args -> if (args.requireList(1).first() == Sexpr.Nil) atomT else Sexpr.Nil }
        putFunction("number?", true) {_, args -> if (args.requireList(1).first() is Sexpr.Number) atomT else Sexpr.Nil }

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

    fun eval(expr: Sexpr): Sexpr {
        return eval(globalEnv, expr)
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
        return asSequence().toList().let { list -> list.takeIf { it.size == size } ?: throw IllegalStateException("$this required to be a list of size $size but has ${list.size} ") }
    }

    private fun LispEnv.lookup(sexpr: Sexpr) = find(sexpr) ?: throw IllegalArgumentException("Unknown symbol $sexpr")

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

    private fun evalDef(env: LispEnv, definition: Sexpr): Sexpr {
        val (key, value) = definition.requireList(2)
        return eval(env, value).also { evaluatedValue -> env[key] = evaluatedValue }
    }

    private fun evalLambdaDefinition(outerEnv: LispEnv, definition: Sexpr): Sexpr {
        val (binds, body) = definition.requireList(2)
        if (binds.asSequence().any { it !is Sexpr.Atom }) throw IllegalStateException("Only atoms required in binds list")
        return LispTailCallFunction("lambda#$nextId", true) { _, args ->
            val lambdaEnv = binds.asSequence()
                    .zip(args.asSequence() + infiniteNilSequence()) { bind, arg -> bind to arg }
                    .fold(LispEnv(outerEnv)) { lambdaEnv, (bind, arg) ->
                        lambdaEnv[bind] = arg
                        lambdaEnv
                    }
            lambdaEnv to body
        }
    }
}
