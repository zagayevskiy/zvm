package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.LispParseResult
import com.zagayevskiy.zvm.zlisp.Sexpr
import com.zagayevskiy.zvm.zlisp.ZLispLexer
import com.zagayevskiy.zvm.zlisp.ZLispParser

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
        val evaluator = LispEvaluator()
        return program.map { evaluator.eval(it) }.last()
    }
}

fun main(args: Array<String>) {
    val interpreter = LispInterpreter("""
        (defun list-reverse-rec (source reversed)
            (cond
                ( (nil? source) reversed )
                ( T (list-reverse-rec (cdr source) (cons (car source) reversed)))
            )
        )

        (defun list-reverse (source) (list-reverse-rec source nil))

        (def! decimal-base 10000)
        (defun decimal (int) (cond
            ((number? int) (list-reverse (split-int int nil)))))

        (defun split-int (int acc)
            (cond
                ( (< int decimal-base) (cons int acc))
                ( T (split-int (/ int decimal-base) (cons (% int decimal-base) acc)))
            )
        )

        (defun decimal+finish-rec (x transfer-int acc)
            (cond
                ( (nil? x) (cond
                                ( (= transfer-int 0) acc)
                                ( T (cons transfer-int acc))
                            )
                )
                ( T (decimal+finish-rec
                        (cdr x)
                        (/ (+ transfer-int (car x)) decimal-base)
                        (cons (% (+ transfer-int (car x)) decimal-base) acc))
                )
            )
        )

        (defun decimal+rec (left right transfer-int acc)
            (cond
                ( (nil? left) (decimal+finish-rec right transfer-int acc))
                ( (nil? right) (decimal+finish-rec left transfer-int acc))
                ( T (decimal+rec
                        (cdr left)
                        (cdr right)
                        (/ (+ transfer-int (car left) (car right) ) decimal-base)
                        (cons (% (+ transfer-int (car left) (car right) ) decimal-base) acc) )
                )

            )
        )

        (defun decimal+ (left right) (list-reverse (decimal+rec left right 0 nil)))

        (defun fib-decimal-rec (prev cur n) (cond ((= n 0) prev) (T (fib-decimal-rec cur (decimal+ prev cur) (- n 1)))))
        (defun fib-decimal (n) (fib-decimal-rec (decimal 0) (decimal 1) n))

        (defun count-digits-rec (n acc)
            (cond
                ( (< n 10) acc)
                ( T (count-digits-rec (- n 1) (+ acc 1)))
            )
        )
        (defun count-digits (n) (count-digits-rec n 1))

        (defun decimal-digits-count-rec (d acc)
                (cond
                    ( (nil? (cdr d)) (+ acc (count-digits (car d))))
                    ( T (decimal-digits-count-rec (cdr d) (+ acc 4)))
                )
        )
        (defun decimal-digits-count (d) (decimal-digits-count-rec 0) )

        (list-reverse (fib-decimal 1000))

    """.trimIndent())

    println(interpreter.interpret("123"))
}