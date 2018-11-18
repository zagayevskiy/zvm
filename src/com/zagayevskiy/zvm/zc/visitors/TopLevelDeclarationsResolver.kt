package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.zc.types.ZcType
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType

class TopLevelDeclarationsResolver(private val ast: Ast) {

    private val globalScope = GlobalScope()
    fun resolve(): Ast {
        return ast.walk(topDownVisitor = { ast ->
            when (ast) {
                is AstFunctionDeclaration -> declare(ast) ?: error("Function ${ast.name} with arguments ${ast.args.map { it.type }} already defined. ")

                else -> ast
            }
        })
    }

    private fun declare(function: AstFunctionDeclaration): AstDefinedFunction? {
        checkArgNames(function)
        return with(function) {
            val resolvedArgs = args.mapIndexed { index, argDecl ->
                //TODO other types
                AstFunctionArgument(argDecl.name, index, ZcType.byName((argDecl.type as? UnresolvedType.Simple)?.name) ?: error("Unknown type ${argDecl.type}"))
            }

            val resolvedRetType = resolveReturnType(returnTypeName, body)

            globalScope.declareFunction(name, resolvedArgs, resolvedRetType, body)
        }
    }

    private fun checkArgNames(function: AstFunctionDeclaration) {
        val existedNames = mutableSetOf<String>()
        function.args.forEach { arg ->
            if (existedNames.contains(arg.name)) error("Argument ${arg.name} already defined in function ${function.name}")
            existedNames += arg.name
        }
    }

    private fun resolveReturnType(returnTypeName: String?, functionBody: Ast): ZcType {
        return if (returnTypeName == null) {
            if (functionBody is AstExpr) error("Expression body not supported yet")
            ZcType.Void
        } else {
            ZcType.byName(returnTypeName) ?: error("Unknown type $returnTypeName")
        }
    }

    private fun resolveType(unresolved: UnresolvedType): ZcType {
        TODO()
    }

    private fun error(message: String): Nothing = throw RuntimeException(message)
}
