package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.Sexpr

typealias BuiltInFunctionSignature = (LispEnv, Sexpr) -> Sexpr

class LispEnv(private val outerEnv: LispEnv? = null) {

    sealed class Entity {
        class BuiltInFunc(private val name: String,val evalArgs: Boolean, private val impl: BuiltInFunctionSignature) : BuiltInFunctionSignature by impl, Entity() {
            override fun toString() = "function $name"
        }

        data class Expr(val sexpr: Sexpr) : Entity()
    }

    private val map: MutableMap<Sexpr, Entity> = mutableMapOf()

    fun find(key: Sexpr): Entity? = get(key) ?: outerEnv?.get(key)

    operator fun get(key: Sexpr): Entity? = map[key]

    operator fun set(key: Sexpr, value: Entity) {
        map[key] = value
    }

    operator fun set(key: Sexpr, value: Sexpr) {
        set(key, Entity.Expr(value))
    }
}