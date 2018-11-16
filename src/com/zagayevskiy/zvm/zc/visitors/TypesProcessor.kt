package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.util.extensions.*
import com.zagayevskiy.zvm.zc.ZcType
import com.zagayevskiy.zvm.zc.ast.*

class TypesProcessor(private val program: AstProgram) {

    private val scopes = stack<Scope>()
    private val currentScope: Scope
        get() = scopes.peek()

    fun processTypes(): Ast {
        return resolveSymbolsAndTypes(program)
    }

    private fun resolveSymbolsAndTypes(ast: Ast): Ast {
        return ast.walk(
                topDownVisitor = ::onTopDown,
                bottomUpVisitor = ::onBottomUp
        )
    }

    private fun onTopDown(ast: Ast): Ast = when (ast) {
        is Scope -> ast.also { scopes.push(ast) }
        is AstValDecl -> {
            val astVal = currentScope.declareVal(ast.valName, ZcType.byName(ast.typeName) ?: ZcType.Unknown) ?: error("Name ${ast.valName} already declared.")
            createAssignment(astVal, ast.initializer)

        }
        is AstVarDecl -> {
            val astVar = currentScope.declareVar(ast.varName, ZcType.byName(ast.typeName) ?: ZcType.Unknown) ?: error("Name ${ast.varName} already declared.")
            createAssignment(astVar, ast.initializer)
        }
        is AstFunctionCall -> resolveFunctionCall(ast)
        is AstIdentifier -> currentScope.lookup(ast.name) ?: error("Unknown identifier '${ast.name}'")
        else -> ast
    }

    private fun onBottomUp(ast: Ast): Ast = when (ast) {
        is Scope -> ast.also { scopes.pop() }

        else -> ast
    }

    private fun resolveFunctionCall(call: AstFunctionCall) = call.also {
        when (val function = call.function) {
            is AstIdentifier -> {
                val definedFunction = currentScope.lookupFunction(function.name) //TODO resolve with types
                        .firstOrNull() ?: error("Unknown function ${function.name}")
                call.function = AstFunctionReference(definedFunction)
            }
        }
    }

    private fun createAssignment(left: AstExpr, right: AstExpr): AstExpr {
        return if (right is AstConst.Undefined) {
            left
        } else {
            AstAssignment(left, right)
        }
    }
}