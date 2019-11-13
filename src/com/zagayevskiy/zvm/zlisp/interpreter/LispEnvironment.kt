package com.zagayevskiy.zvm.zlisp.interpreter

import com.zagayevskiy.zvm.zlisp.Sexpr



class LispEnvironment(private val map: MutableMap<Sexpr, (LispEnvironment, Sexpr) -> Sexpr> = mutableMapOf()): MutableMap<Sexpr, (LispEnvironment, Sexpr) -> Sexpr> by map {

}