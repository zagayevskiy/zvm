package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.util.extensions.*
import com.zagayevskiy.zvm.zc.types.ZcType
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType
import com.zagayevskiy.zvm.zc.types.relations.*
import com.zagayevskiy.zvm.zc.types.resolveType

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
            var type = ast.unresolvedType?.let { resolveType(it) }
            val initializer = resolveSymbolsAndTypes(ast.initializer) as AstExpr
            if (type == null) {
                type = initializer.type
            }
            val astVal = currentScope.declareVal(ast.valName, type) ?: error("Name ${ast.valName} already declared.")
            AstValInitialization(astVal, initializer.tryAutoPromoteTo(type) ?: error("Can't auto promote $initializer to $type for val-initialization."))

        }
        is AstVarDecl -> resolveVarDecl(ast)
        is AstFunctionCall -> resolveFunctionCall(ast)
        is AstIdentifier -> currentScope.lookup(ast.name) ?: error("Unknown identifier '${ast.name}'")
        else -> ast
    }

    private fun onBottomUp(ast: Ast): Ast {
        // TODO may be do it typesafe?
        if (ast.isLeaf() && ast.type == ZcType.Unknown) throw IllegalStateException("$ast type must not be unknown at that point.")

        return when (ast) {
            is Scope -> ast.also { scopes.pop() }
            is AstArithmeticBinary -> ast.apply {
                val promotedType = arithmeticTypesPromotion(left.type, right.type) ?: error("${left.type} and ${right.type} can't be used in arithmetic expressions.")
                left = left.promoteTo(promotedType)
                right = right.promoteTo(promotedType)
                type = promotedType
            }

            is AstLogicalBinary -> ast.apply {
                val promotedType = logicalTypesPromotion(left.type, right.type) ?: error("${left.type} and ${right.type} can't be used in logical expressions.")
                left = left.promoteTo(promotedType)
                right = right.promoteTo(promotedType)
                type = promotedType

            }

            is AstLogicalNot -> ast.apply {
                val promotedType = logicalUnaryTypePromotion(expression.type) ?: error("${expression.type} can't be used in unary logical expression.")
                expression = expression.promoteTo(promotedType)
                type = promotedType
            }

            is AstBitBinary -> ast.apply {
                return@apply when (ast) {
                    is AstBitAnd, is AstBitOr, is AstBitXor -> {
                        val promotedType = bitBinaryTypesPromotion(left.type, right.type) ?: error("${left.type} and ${right.type} can't be used in bit expressions.")
                        left = left.promoteTo(promotedType)
                        right = right.promoteTo(promotedType)
                        type = promotedType
                    }
                    is AstBitShift.Left, is AstBitShift.Right -> {
                        val promotedLeftType = bitShiftOperandTypePromotion(left.type) ?: error("${left.type} can't be bit-shifted.")
                        val promotedRightType = bitShiftOperandTypePromotion(right.type) ?: error("${right.type} can't be used as bit-shift right size.")
                        left = left.promoteTo(promotedLeftType)
                        right = right.promoteTo(promotedRightType)
                        type = promotedLeftType
                    }
                }
            }
            is AstBitNot -> ast.apply {
                val promotedType = bitUnaryTypePromotion(expression.type) ?: error("${expression.type} can't be used in unary bit expression.")
                expression = expression.promoteTo(promotedType)
                type = promotedType
            }
            is AstComparison -> ast.apply {
                when {
                    left.type.canBeAutoPromotedTo(right.type) -> left = left.promoteTo(right.type)
                    right.type.canBeAutoPromotedTo(left.type) -> right = right.promoteTo(left.type)
                    else -> error("$left and $right can't be casted one to another for comparison.")
                }
                type = ZcType.Boolean
            }
            is AstArrayIndexing -> ast.apply {
                val arrayType = (ast.array.type as? ZcType.Array) ?: error("${ast.array} isn't array and can't be indexed.")
                val arrayItemType = arrayType.itemType
                if (arrayItemType is ZcType.Void) error("Array of ${ZcType.Void} can't be indexed.")
                type = arrayItemType
                index = index.tryAutoPromoteTo(ZcType.Integer) ?: error("$index used as array index can't be promoted to ${ZcType.Integer}.")
            }
            is AstStructFieldDereference -> ast.apply {
                val structType = (structInstance.type as? ZcType.Struct) ?: error("Not struct ${ast.structInstance} can't be dereferenced.")
                val resolvedField = structType.findField(name) ?: error("Struct $structType has no field named $name. Processed $ast.")
                type = resolvedField.type
            }

            is AstFunctionReturn -> ast.apply {
                val enclosingFunction = findEnclosingFunction() ?: error("Return-statement can be used only inside a function.")
                expression = expression.tryAutoPromoteTo(enclosingFunction.retType) ?: error("${expression.type} can't be auto promoted to function return type(${enclosingFunction.retType}).")
            }

            is AstIfElse -> ast.apply {
                condition = condition.tryAutoPromoteTo(ZcType.Boolean) ?: error("If-condition type (${condition.type}) can't be auto promoted to ${ZcType.Boolean}")
            }

            is AstWhileLoop -> ast.apply {
                this.condition = condition.tryAutoPromoteTo(ZcType.Boolean) ?: error("While-loop-condition type (${condition.type}) can't be auto promoted to ${ZcType.Boolean}")
            }

            is AstForLoop -> ast.apply {
                this.condition = condition.tryAutoPromoteTo(ZcType.Boolean) ?: error("For-loop-condition type (${condition.type}) can't be auto promoted to ${ZcType.Boolean}")
            }

            is AstAssignment -> ast.apply {
                when (assignable) {
                    is AstVar,
                    is AstArrayIndexing,
                    is AstStructFieldDereference -> assignation = assignation.tryAutoPromoteTo(assignable.type) ?: error("Can't auto promote $assignation to ${assignable.type} for assignment.")

                    else -> error("$assignable isn't assignable.")
                }
            }

            else -> ast
        }
    }

    private fun resolveVarDecl(varDecl: AstVarDecl): AstStatement {
        var type = varDecl.unresolvedType?.let { resolveType(it) }

        val declaredInitializer = varDecl.initializer

        val initializer: AstExpr = when {
            type == null && declaredInitializer is AstConst.Undefined -> error("At least type or initializer must be specified for var declaration.")
            type == null -> resolveSymbolsAndTypes(declaredInitializer) as AstExpr
            declaredInitializer is AstConst.Undefined -> AstConst.DefaultValue(type)
            else -> {
                val temp = resolveSymbolsAndTypes(declaredInitializer) as AstExpr
                temp.tryAutoPromoteTo(type) ?: error("Can't auto promote $temp to $type for var-initialization.")
            }
        }

        type = initializer.type

        val astVar = currentScope.declareVar(varDecl.varName, type) ?: error("Name ${varDecl.varName} already declared.")
        return AstExpressionStatement(AstAssignment(astVar, initializer))
    }

    private fun resolveFunctionCall(call: AstFunctionCall) = call.apply {
        when (val func = call.function) {
            is AstIdentifier -> {
                val definedFunction = currentScope.lookupFunction(func.name) //TODO resolve with types
                        .firstOrNull() ?: error("Unknown function ${func.name}")
                function = AstFunctionReference(definedFunction)
                type = definedFunction.retType
            }
        }
    }

    private fun findEnclosingFunction(scope: Scope? = currentScope): AstDefinedFunction? = when (scope) {
        null -> null
        is AstDefinedFunction -> scope
        else -> findEnclosingFunction(scope.enclosingScope)
    }

    private fun AstExpr.tryAutoPromoteTo(promotedType: ZcType): AstExpr? = when {
        type == promotedType -> this
        type.canBeAutoPromotedTo(promotedType) -> promoteTo(promotedType)
        else -> null
    }

    private fun AstExpr.promoteTo(promotedType: ZcType) = if (type == promotedType) {
        this
    } else {
        AstCastExpr(this, promotedType)
    }

    private fun resolveType(unresolved: UnresolvedType): ZcType = scopes.peek().resolveType(unresolved)
}