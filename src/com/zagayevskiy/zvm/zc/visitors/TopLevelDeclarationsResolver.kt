package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.zc.types.ZcType
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType

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
            resolveType(returnTypeName)
        }
    }

    private fun declare(struct: AstStructDeclaration): AstDefinedStruct? {
        val fields = struct.fieldsDeclarations.map {
            //TODO!!!
            when (it) {
                is AstVarDecl -> it.varName
                is AstValDecl -> it.valName
                else -> error("$it is not field declaration.")
            }
        }.mapIndexed { index, name -> ZcType.Struct.Field(name, ZcType.Integer, index * 4) }
        return globalScope.declareStruct(struct.name, ZcType.Struct(fields)) //TODO
    }

    private fun resolveType(unresolved: UnresolvedType): ZcType {
        val name = (unresolved as? UnresolvedType.Simple)?.name ?: error("Unknown type $unresolved")
        return resolveType(name)
    }

    private fun resolveType(name: String): ZcType {
        return ZcType.byName(name) ?: globalScope.lookupStruct(name)?.type ?: error("Unknown type $name")
    }

    private fun error(message: String): Nothing = throw RuntimeException(message)
}
