package com.zagayevskiy.zvm.zlisp.compiler

import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType
import com.zagayevskiy.zvm.zlisp.Sexpr

class ZLispCompiler() {

    private val atomVals = mutableMapOf<String, AstValDecl>()
    private val numberVals = mutableMapOf<Int, AstValDecl>()
    private val strVals = mutableMapOf<String, AstValDecl>()
    private var nextId: Int = 0
        get() = field++

    fun compile() {
        val program = listOf<Sexpr>()

        val lispDecls = program.map { expr ->
            AstExpressionStatement(expr.toExpr())
        }

        AstFunctionDeclaration(
                name = "evalProgram",
                returnType = null,
                body = AstBlock(statements = atomVals.values + numberVals.values + strVals.values + lispDecls),
                args = listOf(FunctionArgumentDeclaration("context", UnresolvedType.Simple("LispRuntimeContext"))))
    }

    private val memory = AstIdentifier("memory")
    private val cons = AstIdentifier("cons")
    private val createAtom = AstIdentifier("createAtom")
    private val createNumber = AstIdentifier("createNumber")
    private val createString = AstIdentifier("createString")

    private fun Sexpr.toExpr(): AstExpr {
        //TODO includes
        return when (this) {
            is Sexpr.DotPair -> AstFunctionCall(cons, listOf(memory, head.toExpr(), tail.toExpr()))
            is Sexpr.Atom -> obtainAtom(name)
            is Sexpr.Number -> obtainNumber(value)
            is Sexpr.Str -> obtainString(value)
            Sexpr.Nil -> AstIdentifier("nil")
            is Sexpr.RuntimeOnly -> throw IllegalStateException("Must not be here $this")
        }
    }

    private fun obtainAtom(name: String): AstIdentifier {
        return obtain("atom", name, atomVals, createAtom, AstConst.StringLiteral(name))
    }

    private fun obtainNumber(value: Int): AstIdentifier {
        return obtain("number", value, numberVals, createNumber, AstConst.Integer(value))
    }

    private fun obtainString(value: String): AstIdentifier {
        return obtain("str", value, strVals, createString, AstConst.StringLiteral(value))
    }

    private fun <T> obtain(valNamePrefix: String, value: T, cache: MutableMap<T, AstValDecl>, function: AstIdentifier, valExpr: AstExpr): AstIdentifier {
        cache[value]?.let { valDecl ->
            return AstIdentifier(valDecl.valName)
        }

        val valName = "${valNamePrefix}_$nextId"
        val valDecl = AstValDecl(valName, null, AstFunctionCall(function, listOf(memory, valExpr)))
        cache[value] = valDecl
        return AstIdentifier(valName)
    }
}