package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.Sexpr

sealed class LispRuntimeType: Sexpr.RuntimeOnly()

typealias LispLambdaSignature = (LispEnv, Sexpr) -> Sexpr
typealias LispTailCallLambdaSignature = (LispEnv, Sexpr) -> Pair<LispEnv, Sexpr>

class LispFunction(private val name: String, val evalArgs: Boolean, private val lambda: LispLambdaSignature): LispLambdaSignature by lambda, LispRuntimeType() {
    override fun toString() = "function $name"
}

class LispTailCallFunction(private val name: String, val evalArgs: Boolean, private val lambda: LispTailCallLambdaSignature): LispTailCallLambdaSignature by lambda, LispRuntimeType() {
    override fun toString() = "tco function $name"
}