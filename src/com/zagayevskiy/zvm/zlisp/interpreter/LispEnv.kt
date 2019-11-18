package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.Sexpr

typealias BuiltInFunctionSignature = (LispEnv, Sexpr) -> Sexpr

class LispEnv(private val outerEnv: LispEnv? = null) {

    private val map: MutableMap<Sexpr, Sexpr> = mutableMapOf()

    fun find(key: Sexpr): Sexpr? = get(key) ?: outerEnv?.get(key)

    operator fun get(key: Sexpr): Sexpr? = map[key]

    operator fun set(key: Sexpr, value: Sexpr) {
        map[key] = value
    }
}