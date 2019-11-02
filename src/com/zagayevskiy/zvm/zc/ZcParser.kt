package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.common.Token.Eof
import com.zagayevskiy.zvm.common.Token.Identifier
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType

sealed class ParseResult {
    data class Success(val program: AstProgram) : ParseResult()
    object Failure : ParseResult()
}

class ZcParser(private val lexer: Lexer) {

    companion object {
        private val NotMatched = null
    }

    private lateinit var token: Token
    fun program(): ParseResult {
        try {
            val topLevelDeclarations = mutableListOf<TopLevelDeclaration>().apply {
                nextToken()
                add(topLevelDeclaration())
                while (token != Eof) {
                    add(topLevelDeclaration())
                }
            }

            return ParseResult.Success(AstProgram(topLevelDeclarations))
        } catch (e: ParseException) {
            throw e
//            return ParseResult.Failure()
        }
    }

    private fun topLevelDeclaration(): TopLevelDeclaration {
        return struct() ?: function() ?: constDefinition() ?: error("Top-level declaration expected.")
    }

    private fun constDefinition(): AstConstDeclaration? {
        maybe<ZcToken.Const>() ?: return NotMatched
        val name = expect<Identifier>().name

        val type = maybe<ZcToken.Colon>()?.andThan { type()?:error("Type expected after ':'") }

        val initializer = expect<ZcToken.Assign>().andThan { constExpr() ?: error("Const initializer expected.") }

        expect<ZcToken.Semicolon>()

        return AstConstDeclaration(name, type, initializer)
    }

    private fun struct(): AstStructDeclaration? {
        maybe<ZcToken.Struct>() ?: return NotMatched
        val name = expect<Identifier>().name

        expect<ZcToken.CurlyBracketOpen>()

        val fieldsDeclarations = structDeclarationList().takeIf { it.isNotEmpty() } ?: error("At least one field declaration expected for struct $name.")

        expect<ZcToken.CurlyBracketClose>()

        return AstStructDeclaration(name, fieldsDeclarations)
    }

    private fun structDeclarationList(): List<AstVarDecl> = mutableListOf<AstVarDecl>().apply {
        while (true) {
            add(structFieldDeclaration() ?: break)
            expect<ZcToken.Semicolon>()
        }
    }

    private fun structFieldDeclaration() = varDecl()

    //"fn" identifier "(" function_args_list ")" [":" return_type] function_body
    private fun function(): AstFunctionDeclaration? {
        maybe<ZcToken.Fun>() ?: return NotMatched
        val name = expect<Identifier>().name
        expect<ZcToken.ParenthesisOpen>()
        val args = functionArgsList()
        expect<ZcToken.ParenthesisClose>()
        val returnType = maybe<ZcToken.Colon>()?.andThan { functionReturnType() ?: error("Return type expected.") }
        val body = functionBody() ?: error("Function body expected.")

        return AstFunctionDeclaration(name, args, returnType, body)
    }

    //function_args_list ::= [ function_arg_definition {"," function_arg_definition} ]
    private fun functionArgsList(): List<FunctionArgumentDeclaration> {
        val first = functionArgDefinition() ?: return emptyList()
        val list = mutableListOf(first)

        while (maybe<ZcToken.Comma>() != null) {
            list.add(functionArgDefinition() ?: error("Argument declaration expected."))
        }

        return list
    }

    //function_arg_definition ::= identifier ":" identifier
    private fun functionArgDefinition(): FunctionArgumentDeclaration? {
        val name = maybe<Identifier>()?.name ?: return NotMatched
        expect<ZcToken.Colon>()
        val type = type() ?: error("Type of function argument expected.")
        return FunctionArgumentDeclaration(name, type)
    }

    private fun functionReturnType() = type()

    private fun functionBody() = functionBlockBody() ?: functionExpressionBody()

    private fun functionBlockBody() = block()

    private fun functionExpressionBody(): AstExpr? {
        maybe<ZcToken.Assign>() ?: return NotMatched
        return expression() ?: error("Expression expected in expression body.")
    }

    private fun type(): UnresolvedType? = simpleType() ?: arrayType() ?: functionalType()

    private fun simpleType(): UnresolvedType? = maybe<Identifier>()?.andThan { id -> UnresolvedType.Simple(id.name) }

    private fun arrayType(): UnresolvedType? {
        maybe<ZcToken.SquareBracketOpen>() ?: return null

        val elementType = type() ?: error("Type of array elements expected.")

        expect<ZcToken.SquareBracketClose>()

        return UnresolvedType.Array(elementType)
    }

    private fun functionalType(): UnresolvedType? {
        maybe<ZcToken.ParenthesisOpen>() ?: return null

        val argTypes = matchList<ZcToken.Comma, UnresolvedType>(::type) ?: emptyList()

        expect<ZcToken.ParenthesisClose>()
        expect<ZcToken.Arrow>()

        val returnType = type() ?: error("Return type expected.")

        return UnresolvedType.Function(argTypes, returnType)
    }


    private fun variableDeclStatement() = variableDecl()?.also { expect<ZcToken.Semicolon>() }

    private fun variableDecl(): AstStatement? = varDecl() ?: valDecl()

    // var_declaration ::= "var" identifier ((":" identifier) ["=" expression] | ("=" expression))
    private fun varDecl(): AstVarDecl? {
        maybe<ZcToken.Var>() ?: return NotMatched

        val name = expect<Identifier>().name

        val type = maybe<ZcToken.Colon>()?.andThan { type() ?: error("Type expected after ':'.") }

        val initializer = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Expression expected.") }

        if (type == null && initializer == null) error("Type or assignment expected.")

        return AstVarDecl(varName = name, unresolvedType = type, initializer = initializer)
    }


    private fun valDecl(): AstValDecl? {
        maybe<ZcToken.Val>() ?: return NotMatched

        val name = expect<Identifier>().name

        val type = maybe<ZcToken.Colon>()?.andThan { type() ?: error("Type expected after ':'.") }

        expect<ZcToken.Assign>()
        val initializer = expression() ?: error("Valid expression expected after assignment")

        return AstValDecl(valName = name, unresolvedType = type, initializer = initializer)
    }

    private fun block(): AstBlock? {
        maybe<ZcToken.CurlyBracketOpen>() ?: return NotMatched

        val statements = mutableListOf<AstStatement>().apply {
            while (true) {
                add(statement() ?: break)
            }
        }

        expect<ZcToken.CurlyBracketClose>()

        return AstBlock(statements)
    }

    // statement ::= variable_declaration | loop | function_return_statement | expression
    private fun statement(): AstStatement? = block()
            ?: variableDeclStatement()
            ?: loopStatement()
            ?: ifElseStatement()
            ?: functionReturnStatement()
            ?: expressionStatement()
            ?: asmStatement()
            ?: whenStatement()

    private fun whenStatement(): AstWhen? {
        maybe<ZcToken.When>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val checkValue = expression() ?: error("Check value expected.")
        expect<ZcToken.ParenthesisClose>()
        expect<ZcToken.CurlyBracketOpen>()
        val branches = mutableListOf<AstWhenBranch>()
        do {
            val branch = whenBranch()?.also { branches.add(it) }
        } while (branch != null)
        if (branches.isEmpty()) error("At least one branch expected")

        val elseStatement = whenElse()

        expect<ZcToken.CurlyBracketClose>()

        return AstWhen(checkValue, branches, elseStatement)
    }

    private fun whenBranch(): AstWhenBranch? {
        val case = expression() ?: return NotMatched
        val branch = expect<ZcToken.Arrow>().andThan { statement() } ?: error("statement expected after -> ")
        return AstWhenBranch(case, branch)
    }

    private fun whenElse(): AstStatement? {
        maybe<ZcToken.Else>() ?: return NotMatched
        expect<ZcToken.Arrow>()
        return statement() ?: error("statement expected after else ->")
    }

    private fun asmStatement(): AstAsmBlock? {
        maybe<ZcToken.Asm>() ?: return NotMatched
        expect<ZcToken.CurlyBracketOpen>()
        val asmBody = expect<Token.StringConst>()
        expect<ZcToken.CurlyBracketClose>()
        return AstAsmBlock(asmBody.value)
    }

    private fun loopStatement() = forLoop() ?: whileLoop()

    // for_loop ::= "for" "(" [for_loop_initializer] ";" [for_loop_condition] ";" [for_loop_step] ")" block
    private fun forLoop(): AstStatement? {
        maybe<ZcToken.For>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val initializer = forLoopInitializer() ?: AstStatementList.Empty
        expect<ZcToken.Semicolon>()
        val condition = forLoopCondition() ?: AstConst.Undefined
        expect<ZcToken.Semicolon>()
        val step = forLoopStep() ?: AstStatementList.Empty
        expect<ZcToken.ParenthesisClose>()
        val body = statement() ?: error("For-loop body expected.")
        //Block needed to create enclosing scope
        return AstBlock(listOf(AstForLoop(initializer, condition, step, body)))
    }

    // for_loop_initializer ::= variable_declaration {"," variable_declaration}
    private fun forLoopInitializer(): AstStatementList? {
        val first = variableDecl() ?: return NotMatched
        val list = mutableListOf(first)
        while (maybe<ZcToken.Comma>() != null) {
            list.add(variableDecl() ?: error("Initializer expected."))
        }

        return AstStatementList(list)
    }

    private fun forLoopCondition(): AstExpr? = expression()

    // for_loop_step ::= expression {"," expression}
    private fun forLoopStep(): AstStatementList? {
        val first = expression() ?: return NotMatched
        val list = mutableListOf(first)
        while (maybe<ZcToken.Comma>() != null) {
            list.add(expression() ?: error("Expression expected."))
        }

        return AstStatementList(list.map { AstExpressionStatement(it) })
    }

    // while_loop ::= "while" "(" expression ")" block
    private fun whileLoop(): AstWhileLoop? {
        maybe<ZcToken.While>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val condition = expression() ?: error("Expression expected.")
        expect<ZcToken.ParenthesisClose>()
        val body = statement() ?: error("While-loop body expected.")

        return AstWhileLoop(condition = condition, body = body)
    }

    // "if" "(" expression ")" (block | expression) [ "else" (block | expression) ]
    private fun ifElseStatement(): AstIfElse? {
        maybe<ZcToken.If>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val condition = expression() ?: error("Expression expected as if-condition.")
        expect<ZcToken.ParenthesisClose>()
        val ifBody = statement() ?: error("if-body expected.")
        val elseBody = maybe<ZcToken.Else>()?.andThan { statement() ?: error("else-body expected") }

        return AstIfElse(condition = condition, ifBody = ifBody, elseBody = elseBody)
    }

    private fun functionReturnStatement(): AstFunctionReturn? {
        maybe<ZcToken.Return>() ?: return NotMatched
        val expression = expression() ?: AstConst.Void
        expect<ZcToken.Semicolon>()
        return AstFunctionReturn(expression)
    }

    private fun expressionStatement(): AstExpressionStatement? {
        val expression = expression()?.also { expect<ZcToken.Semicolon>() } ?: return NotMatched
        return AstExpressionStatement(expression)
    }

    // expression ::= if_else_expr | disjunction_expr
    private fun expression() = disjunctionExpr()

    // disjunction_expr ::= conjunction_expr [ "||" disjunction_expr ]
    private fun disjunctionExpr(): AstExpr? {
        val left = conjunctionExpr() ?: return NotMatched
        maybe<ZcToken.Disjunction>() ?: return left

        val right = disjunctionExpr() ?: error("Right side of disjunction expected.")

        return AstDisjunction(left, right)
    }

    // conjunction_expr ::= bit_or_expr [ "&&" conjunction_expr ]
    private fun conjunctionExpr(): AstExpr? {
        val left = bitOrExpr() ?: return NotMatched
        maybe<ZcToken.Conjunction>() ?: return left
        val right = conjunctionExpr() ?: error("Right side of conjunction expected.")
        return AstConjunction(left, right)
    }

    // bit_or_expr ::= bit_xor_expr [ "|" bit_or_expr ]
    private fun bitOrExpr(): AstExpr? {
        val left = bitXorExpr() ?: return NotMatched
        maybe<ZcToken.BitOr>() ?: return left
        val right = bitOrExpr() ?: error("Right side of bit-or expected.")

        return AstBitOr(left, right)
    }

    // bit_xor_expr ::= bit_and_expr [ "^" bit_xor_expr ]
    private fun bitXorExpr(): AstExpr? {
        val left = bitAndExpr() ?: return NotMatched
        maybe<ZcToken.BitXor>() ?: return left
        val right = bitXorExpr() ?: error("Right side of bit-xor expected.")
        return AstBitXor(left, right)
    }

    // bit_and_expr ::= equality_expr [ "&" bit_and_expr ]
    private fun bitAndExpr(): AstExpr? {
        val left = eqExpr() ?: return NotMatched
        maybe<ZcToken.BitAnd>() ?: return left
        val right = bitAndExpr() ?: error("Right side of bit-and expected.")
        return AstBitAnd(left, right)
    }

    // equality_expr ::= comparison_expr [ ("==" | "!=") equality_expr ]
    private fun eqExpr(): AstExpr? {
        val left = comparisonExpr() ?: return NotMatched

        return when (token) {
            ZcToken.Equals -> {
                nextToken()
                AstEquals(left = left, right = eqExpr() ?: error("Right side of equals expected."))
            }
            ZcToken.NotEquals -> {
                nextToken()
                AstNotEquals(left = left, right = eqExpr() ?: error("Right size of not-equals expected."))
            }
            else -> left
        }
    }

    // comparison_expr ::= bit_shift_expr [ (">" | "<" | ">="| "<=") comparison_expr ]
    private fun comparisonExpr(): AstExpr? {
        val left = bitShiftExpr() ?: return NotMatched

        return when (token) {
            ZcToken.Great -> {
                nextToken()
                AstGreat(left = left, right = comparisonExpr() ?: error("Right side of > expected."))
            }
            ZcToken.GreatEq -> {
                nextToken()
                AstGreatEq(left = left, right = comparisonExpr() ?: error("Right side of >= expected."))
            }
            ZcToken.Less -> {
                nextToken()
                AstLess(left = left, right = comparisonExpr() ?: error("Right side of < expected."))
            }
            ZcToken.LessEq -> {
                nextToken()
                AstLessEq(left = left, right = comparisonExpr() ?: error("Right side of <= expected."))
            }
            else -> left
        }

    }

    // bit_shift_expr ::= addition_expr [ (">>" | "<<") addition_expr ]
    private fun bitShiftExpr(): AstExpr? {
        val left = additionExpr() ?: return NotMatched

        return when (token) {
            ZcToken.BitShiftLeft -> {
                nextToken()
                AstBitShift.Left(left = left, right = bitShiftExpr() ?: error("Right side of bit-shift-left expected."))
            }
            ZcToken.BitShiftRight -> {
                nextToken()
                AstBitShift.Right(left = left, right = bitShiftExpr() ?: error("Right side of bit-shift-right expected."))
            }
            else -> left
        }
    }

    // addition_expr ::= multiplication_expr { ("+" | "-") multiplication_expr }
    private fun additionExpr(): AstExpr? {
        val left = multiplicationExpr() ?: return NotMatched

        return when (token) {
            ZcToken.Plus -> {
                nextToken()
                AstSum(left = left, right = additionExpr() ?: error("Right side of sum expected."))
            }
            ZcToken.Minus -> {
                nextToken()
                AstDifference(left = left, right = additionExpr() ?: error("Right side of difference expected."))
            }
            else -> left
        }
    }

    // multiplication_expr ::= unary_expr [ "*" | "/" | "%" multiplication_expr ]
    private fun multiplicationExpr(): AstExpr? {
        val left = unaryExpr() ?: return NotMatched

        return when (token) {
            ZcToken.Asterisk -> {
                nextToken()
                AstMul(left = left, right = multiplicationExpr() ?: error("Right side of mul expected."))
            }
            ZcToken.Slash -> {
                nextToken()
                AstDiv(left = left, right = multiplicationExpr() ?: error("Right size of div expected."))
            }
            ZcToken.Percent -> {
                nextToken()
                AstMod(left = left, right = multiplicationExpr() ?: error("Right side of mod expected."))
            }
            else -> left
        }
    }

    // unary_expr ::= [( "~" | "!" | "@")] value_expr
    private fun unaryExpr(): AstExpr? = unaryBitNot() ?: unaryLogicalNot() ?: unaryDereferencing() ?: valueExpr()

    private fun unaryBitNot(): AstBitNot? {
        maybe<ZcToken.BitNot>() ?: return NotMatched
        val expression = expression() ?: error("Bit-not argument expected.")
        return AstBitNot(expression)
    }

    private fun unaryLogicalNot(): AstLogicalNot? {
        maybe<ZcToken.LogicalNot>() ?: return NotMatched
        val expression = expression() ?: error("Logical-not argument expected.")
        return AstLogicalNot(expression)
    }

    private fun unaryDereferencing() = maybe<ZcToken.Asterisk>()?.andThan { expression() ?: error("Dereferencing argument expected.") }

    private fun valueExpr() = constExpr() ?: parenthesisExpr() ?: assignmentExpr() ?: sizeOfOperator() ?: castOperator() ?: functionReference()

    private fun constExpr(): AstConst? {
        val int = maybe<Token.Integer>()?.value
                ?: maybe<ZcToken.Minus>()?.andThan { expect<Token.Integer>().value * -1 }

        if (int != null) return AstConst.Integer(int)

        val stringLiteral = maybe<Token.StringConst>() ?: return NotMatched


        return AstConst.StringLiteral(stringLiteral.value)
    }

    // parenthesis_expr ::= "(" expression ")" [chain]
    private fun parenthesisExpr(): AstExpr? {
        maybe<ZcToken.ParenthesisOpen>() ?: return NotMatched

        val expression = expression() ?: error("Expression expected.")

        expect<ZcToken.ParenthesisClose>()

        return chain(expression)
    }

    // identifier ("=" expression | [chain])
    private fun assignmentExpr(): AstExpr? {
        val varName = maybe<Identifier>()?.name ?: return NotMatched
        val variable = AstIdentifier(varName)

        val expression = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Right side of assignment expected.") }

        if (expression != null) {
            return AstAssignment(assigned = variable, assignation = expression)
        }

        return chain(variable)
    }

    private fun sizeOfOperator(): AstSizeOf? {
        maybe<ZcToken.SizeOf>() ?: return NotMatched
        expect<ZcToken.Less>()

        val type = type() ?: error("Type expected for sizeof.")

        expect<ZcToken.Great>()

        return AstSizeOf(type)
    }

    private fun castOperator(): AstExpr? {
        maybe<ZcToken.Cast>() ?: return NotMatched
        expect<ZcToken.Less>()

        val type = type() ?: error("Type expected for cast.")

        expect<ZcToken.Great>()
        expect<ZcToken.ParenthesisOpen>()
        val expression = expression() ?: error("Expression expected for cast")
        expect<ZcToken.ParenthesisClose>()

        return chain(AstHardCastExpr(expression, type))
    }

    private fun functionReference(): AstUnknownFunctionReference? {
        maybe<ZcToken.QuadDot>() ?: return NotMatched
        val name = expect<Token.Identifier>().name

        return AstUnknownFunctionReference(name)
    }

    // chain ::= function_call | array_indexing | struct_field_dereference
    private fun chain(leftSide: AstExpr): AstExpr = functionCall(leftSide) ?: arrayIndexing(leftSide) ?: structFieldDereference(leftSide) ?: leftSide

    // function_call ::= "(" expressions_list ")" [chain]
    private fun functionCall(function: AstExpr): AstExpr? {
        maybe<ZcToken.ParenthesisOpen>() ?: return NotMatched
        val expressions = matchList<ZcToken.Comma, AstExpr>(::expression) ?: emptyList()
        expect<ZcToken.ParenthesisClose>()

        val functionCall = AstFunctionCall(function = function, params = expressions)

        return chain(functionCall)
    }

    // array_indexing ::= "[" expression "]" ("=" expression | [chain])
    private fun arrayIndexing(array: AstExpr): AstExpr? {
        maybe<ZcToken.SquareBracketOpen>() ?: return NotMatched
        val expression = expression() ?: error("Expression expected as index.")
        expect<ZcToken.SquareBracketClose>()

        val indexedArray = AstArrayIndexing(array = array, index = expression)

        val assignation = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Right side of assignment expected.") }
        if (assignation != NotMatched) {
            return AstAssignment(assigned = indexedArray, assignation = assignation)
        }

        return chain(indexedArray)
    }

    // struct_field_dereference ::= "." identifier ("=" expression | [chain])
    private fun structFieldDereference(leftSide: AstExpr): AstExpr? {
        maybe<ZcToken.Dot>() ?: return NotMatched
        val fieldName = expect<Identifier>().name

        val dereference = AstStructFieldDereference(leftSide, fieldName)

        val assignation = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Right side of assignment expected.") }
        if (assignation != NotMatched) {
            return AstAssignment(assigned = dereference, assignation = assignation)
        }

        return chain(dereference)
    }

    private fun nextToken() {
        token = lexer.nextToken().also { currentToken ->
            (currentToken as? Token.Error)?.let { error("Lexical error at sequence ${it.sequence}") }
        }
    }

    private fun error(message: String = "Syntax error at token $token"): Nothing = throw ParseException("Line [${lexer.currentLineNumber}](${lexer.currentLine}): $message")

    private inline fun <reified T : Token> expect() = maybe<T>()
            ?: error("Unexpected token $token. Token of type ${T::class.simpleName} expected.")

    private inline fun <reified T : Token> maybe() = (token as? T)?.also { nextToken() }

    private inline fun <T : Token, R> T.andThan(block: (T) -> R): R = block(this)

    private inline fun <reified T : Token, R : Any> matchList(element: () -> R?): List<R>? {
        val first = element() ?: return NotMatched
        return mutableListOf(first).apply {
            while (maybe<T>() != null) add(element() ?: error())
        }
    }
}

class ParseException(override val message: String) : Exception()