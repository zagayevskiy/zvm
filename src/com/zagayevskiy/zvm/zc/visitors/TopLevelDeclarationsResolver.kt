package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.zc.types.ZcType
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType
import com.zagayevskiy.zvm.zc.types.resolveType

class TopLevelDeclarationsResolver(private val ast: Ast) {

    private val globalScope = GlobalScope()
    fun resolve(): Ast {
        return ast.walk(topDownVisitor = { ast ->
            when (ast) {
                is AstFunctionDeclaration -> declare(ast) ?: error("Function ${ast.name} with arguments ${ast.args.map { it.type }} already defined.")
                is AstStructDeclaration -> declare(ast) ?: error("Struct ${ast.name} already defined.")
                else -> ast
            }
        })
    }

    private fun declare(function: AstFunctionDeclaration): AstDefinedFunction? {
        checkArgNames(function)
        return with(function) {
            val resolvedArgs = args.mapIndexed { index, argDecl ->
                //TODO other types
                AstFunctionArgument(argDecl.name, index, resolveType(argDecl.type))
            }

            val resolvedRetType = returnType?.let { resolveType(it) } ?: resolveReturnType(body)

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

    private fun resolveReturnType(functionBody: Ast): ZcType {
        return if (functionBody is AstExpr) {
            error("Expression body not supported yet")
        } else {
            ZcType.Void
        }
    }

    private fun declare(structDecl: AstStructDeclaration): AstDefinedStruct? {
        val fields = mutableListOf<ZcType.Struct.Field>()
        var currentOffset = 0

        structDecl.fieldsDeclarations.forEach { varDecl ->
            val fieldType = varDecl.unresolvedType?.let { resolveType(it) } ?: TODO("Fields initializer not supported yet.")
            val declaredInitializer = varDecl.initializer
            if (declaredInitializer != AstConst.Undefined) TODO("Fields initializer not supported yet.")

            fields += ZcType.Struct.Field(varDecl.varName, fieldType, currentOffset)
            currentOffset += fieldType.sizeOf
        }

        return globalScope.declareStruct(structDecl.name, ZcType.Struct(structDecl.name, fields))
    }

    private fun resolveType(unresolved: UnresolvedType): ZcType = globalScope.resolveType(unresolved)

    private fun error(message: String): Nothing = throw RuntimeException(message)
}
