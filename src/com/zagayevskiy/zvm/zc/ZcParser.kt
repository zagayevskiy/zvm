package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.common.Token.Eof
import com.zagayevskiy.zvm.common.Token.Identifier
import com.zagayevskiy.zvm.zc.ast.*

sealed class ParseResult {
    class Success(val program: Ast) : ParseResult()
    class Failure : ParseResult()
}

class ZcParser(private val lexer: Lexer) {

    companion object {
        private val NotMatched = null
    }

    private lateinit var token: Token
    fun program(): ParseResult {
        try {
            val topLevelDeclarations = mutableListOf<Ast>().apply {
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
        return struct() ?: function() ?: error("Top-level declaration expected.")
    }

    private fun struct(): AstStructDeclaration? {
        maybe<ZcToken.Struct>() ?: return NotMatched
        val name = expect<Identifier>().name

        expect<ZcToken.CurlyBracketOpen>()

        structDeclarationList()

        expect<ZcToken.CurlyBracketClose>()

        return AstStructDeclaration(name)
    }

    private fun structDeclarationList(): List<Ast> = mutableListOf<Ast>().apply {
        while (true) {
            add(variableDecl() ?: break)
        }
        if (isEmpty()) error("At least one variable declaration expected.")
    }

    //"fn" identifier "(" function_args_list ")" [":" return_type] function_body
    private fun function(): AstFunctionDeclaration? {
        maybe<ZcToken.Fun>() ?: return NotMatched
        val name = expect<Identifier>().name
        expect<ZcToken.ParenthesisOpen>()
        val args = functionArgsList()
        expect<ZcToken.ParenthesisClose>()
        val returnType = maybe<ZcToken.Colon>()?.andThan { functionReturnType() ?: error("Return type expected.") }?.name
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
        val typeName = expect<Identifier>().name
        return FunctionArgumentDeclaration(name, typeName)
    }

    private fun functionReturnType() = maybe<Identifier>()

    private fun functionBody() = functionBlockBody() ?: functionExpressionBody()

    private fun functionBlockBody() = block()

    private fun functionExpressionBody(): Ast? {
        maybe<ZcToken.Assign>() ?: return NotMatched
        return expression() ?: error("Expression expected in expression body.")
    }

    private fun variableDecl(): AstStatement? = (varDecl() ?: valDecl())?.also { expect<ZcToken.Semicolon>() }

    // var_declaration ::= "var" identifier ((":" identifier) ["=" expression] | ("=" expression))
    private fun varDecl(): AstVarDecl? {
        maybe<ZcToken.Var>() ?: return NotMatched

        val name = expect<Identifier>().name

        val typeName = maybe<ZcToken.Colon>()?.andThan { expect<Identifier>() }?.name

        val initializer = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Expression expected.") }

        if (typeName == null && initializer == null) error("type or assignment expected")

        return AstVarDecl(varName = name, typeName = typeName, initializer = initializer)
    }


    private fun valDecl(): AstValDecl? {
        maybe<ZcToken.Val>() ?: return NotMatched

        val name = expect<Identifier>().name

        val typeName = maybe<ZcToken.Colon>()?.andThan { expect<Identifier>() }?.name

        expect<ZcToken.Assign>()
        val initializer = expression() ?: error("Valid expression expected after assignment")

        return AstValDecl(valName = name, typeName = typeName, initializer = initializer)
    }

    private fun block(): Ast? {
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
    private fun statement(): AstStatement? = variableDecl() ?: loopStatement() ?: ifElseStatement() ?: functionReturnStatement() ?: expressionStatement()

    private fun loopStatement() = forLoop() ?: whileLoop()

    // for_loop ::= "for" "(" [for_loop_initializer] ";" [for_loop_condition] ";" [for_loop_step] ")" block
    private fun forLoop(): AstLoop? {
        maybe<ZcToken.For>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val initializer = forLoopInitializer()
        expect<ZcToken.Semicolon>()
        val condition = forLoopCondition()
        expect<ZcToken.Semicolon>()
        val step = forLoopStep()
        expect<ZcToken.ParenthesisClose>()
        val body = block() ?: error("For-loop body expected.")
        return AstLoop(initializer, condition, step, body)
    }

    // for_loop_initializer ::= variable_declaration {"," variable_declaration}
    private fun forLoopInitializer(): Ast? {
        val first = variableDecl() ?: return NotMatched
        val list = mutableListOf(first)
        while (maybe<ZcToken.Colon>() != null) {
            list.add(variableDecl() ?: error("Initializer expected."))
        }

        return StubAst
    }

    private fun forLoopCondition(): AstExpr? = expression()

    // for_loop_step ::= expression {"," expression}
    private fun forLoopStep(): Ast? {
        val first = expression() ?: return NotMatched
        val list = mutableListOf(first)
        while (maybe<ZcToken.Colon>() != null) {
            list.add(expression() ?: error("Expression expected."))
        }

        return StubAst
    }

    // while_loop ::= "while" "(" expression ")" block
    private fun whileLoop(): AstWhile? {
        maybe<ZcToken.While>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val condition = expression() ?: error("Expression expected.")
        expect<ZcToken.ParenthesisClose>()
        val body = block() ?: error("While-loop body expected.")

        return AstWhile(condition = condition, body = body)
    }

    // "if" "(" expression ")" (block | expression) [ "else" (block | expression) ]
    private fun ifElseStatement(): AstIfElse? {
        maybe<ZcToken.If>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val condition = expression() ?: error("Expression expected as if-condition.")
        expect<ZcToken.ParenthesisClose>()
        val ifBody = block() ?: expression() ?: error("if-body expected.")
        val elseBody = maybe<ZcToken.Else>()?.andThan { block() ?: expression() ?: error("else-body expected") }

        return AstIfElse(condition = condition, ifBody = ifBody, elseBody = elseBody)
    }

    private fun functionReturnStatement(): AstFunctionReturn? {
        maybe<ZcToken.Return>() ?: return NotMatched
        val expression = expression() ?: error("Expression expected.")
        expect<ZcToken.Semicolon>()
        return AstFunctionReturn(expression)
    }

    private fun expressionStatement(): AstExpressionStatement? {
        val expression = expression()?.also { expect<ZcToken.Semicolon>() } ?: return NotMatched
        return AstExpressionStatement(expression)
    }

    // expression ::= if_else_expr | disjunction_expr
    private fun expression() = disjunctionExpr()

    // disjunction_expr ::= conjunction_expr { "||" conjunction_expr }
    private fun disjunctionExpr(): AstExpr? {
        var conjuctions = matchList<ZcToken.Disjunction>(::conjunctionExpr) ?: return NotMatched
        return AstConst.Undefined //TODO
    }

    // conjunction_expr ::= bit_or_expr { "&&" bit_or_expr }
    private fun conjunctionExpr(): Ast? {
        val ors = matchList<ZcToken.Conjunction>(::bitOrExpr) ?: return NotMatched
        return StubAst
    }

    // bit_or_expr ::= bit_xor_expr { "|" bit_xor_expr }
    private fun bitOrExpr(): Ast? {
        val xors = matchList<ZcToken.BitOr>(::bitXorExpr) ?: return NotMatched

        return StubAst
    }

    // bit_xor_expr ::= bit_and_expr { "^" bit_and_expr }
    private fun bitXorExpr(): Ast? {
        val ands = matchList<ZcToken.BitXor>(::bitAndExpr) ?: return NotMatched
        return StubAst
    }

    // bit_and_expr ::= equality_expr { "&" equality_expr }
    private fun bitAndExpr(): Ast? {
        val eqs = matchList<ZcToken.BitAnd>(::eqExpr) ?: return NotMatched
        return StubAst
    }

    // equality_expr ::= comparison_expr { ("==" | "!=") comparison_expr }
    private fun eqExpr(): Ast? {
        val left = comparisonExpr() ?: return NotMatched

        fun maybeEquality() = maybeOneOf(ZcToken.Equals, ZcToken.NotEquals)

        var token = maybeEquality()
        while (token != null) {
            comparisonExpr() ?: error("Right side of equality expected.")
            token = maybeEquality()
        }


        return StubAst
    }

    // comparison_expr ::= bit_shift_expr { (">" | "<" | ">="| "<=") bit_shift_expr }
    private fun comparisonExpr(): Ast? {
        val left = bitShiftExpr() ?: return NotMatched
        fun maybeComparison() = maybeOneOf(ZcToken.Great, ZcToken.GreatEq, ZcToken.Less, ZcToken.LessEq)

        var token = maybeComparison()
        while (token != null) {
            bitShiftExpr() ?: error("Right side of comparison expected.")
            token = maybeComparison()
        }

        return StubAst
    }

    // bit_shift_expr ::= addition_expr { (">>" | "<<") addition_expr }
    private fun bitShiftExpr(): Ast? {
        val left = additionExpr() ?: return NotMatched
        fun maybeShift() = maybeOneOf(ZcToken.BitShiftLeft, ZcToken.BitShiftRight)

        var token = maybeShift()
        while (token != null) {
            additionExpr() ?: error("Right side of bit shift expected.")
            token = maybeShift()
        }

        return StubAst
    }

    // addition_expr ::= multiplication_expr { ("+" | "-") multiplication_expr }
    private fun additionExpr(): Ast? {

        val left = multiplicationExpr() ?: return NotMatched
        val token = maybeOneOf(ZcToken.Plus, ZcToken.Minus) ?: return left
        val right = additionExpr() ?: error("Right side of addition expected.")

        return StubAst
    }

    // multiplication_expr ::= unary_expr { "*" | "/" | "%" unary_expr }
    private fun multiplicationExpr(): Ast? {
        val left = unaryExpr() ?: return NotMatched
        fun maybeMul() = maybeOneOf(ZcToken.Asterisk, ZcToken.Slash, ZcToken.Percent)

        var token = maybeMul()
        while (token != null) {
            unaryExpr()
            token = maybeMul()
        }

        return StubAst
    }

    // unary_expr ::= [( "~" | "!" | "@")] value_expr
    private fun unaryExpr(): Ast? = unaryBitNot() ?: unaryLogicalNot() ?: unaryDereferencing() ?: valueExpr()

    private fun unaryBitNot() = maybe<ZcToken.BitNot>()?.andThan { expression() ?: error("Bit-not argument expected.") }

    private fun unaryLogicalNot() = maybe<ZcToken.LogicalNot>()?.andThan { expression() ?: error("Logical-not argument expected.") }

    private fun unaryDereferencing() = maybe<ZcToken.Asterisk>()?.andThan { expression() ?: error("Dereferencing argument expected.") }

    private fun valueExpr() = constExpr() ?: parenthesisExpr() ?: assignmentExpr()

    private fun constExpr(): Ast? {
        val constant = maybe<Token.Integer>() ?: return NotMatched

        return StubAst
    }

    // parenthesis_expr ::= "(" expression ")" [chain]
    private fun parenthesisExpr(): Ast? {
        maybe<ZcToken.ParenthesisOpen>() ?: return NotMatched

        val expression = expression() ?: error("Expression expected.")

        expect<ZcToken.ParenthesisClose>()

        chain()

        return StubAst // chained somehow
    }

    // identifier ("=" expression | [chain])
    private fun assignmentExpr(): Ast? {
        val identifier = maybe<Identifier>() ?: return NotMatched

        val expression = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Right side of assignment expected.") }

        if (expression == null) {
            chain()
        }

        return StubAst // chained somehow
    }

    // chain ::= function_call | array_indexing | struct_field_dereference
    private fun chain() = functionCall() ?: arrayIndexing() ?: structFieldDereference()

    // function_call ::= "(" expressions_list ")" [chain]
    private fun functionCall(): Ast? {
        maybe<ZcToken.ParenthesisOpen>() ?: return NotMatched
        val expressions = matchList<ZcToken.Comma>(::expression) ?: emptyList()
        expect<ZcToken.ParenthesisClose>()

        chain()

        return StubAst
    }

    // array_indexing ::= "[" expression "]" ("=" expression | [chain])
    private fun arrayIndexing(): Ast? {
        maybe<ZcToken.SquareBracketOpen>() ?: return NotMatched
        val expression = expression() ?: error("Expression expected as index.")
        expect<ZcToken.SquareBracketClose>()
        val assignmentExpr = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Right side of assignment expected.") }
        if (assignmentExpr == null) {
            chain()
        }

        return StubAst
    }

    // struct_field_dereference ::= "." identifier ("=" expression | [chain])
    private fun structFieldDereference(): Ast? {
//
        return NotMatched
    }

    private fun nextToken() {
        token = lexer.nextToken().also { currentToken ->
            (currentToken as? Token.Error)?.let { error("Lexical error at sequence ${it.sequence}") }
        }
    }

    private fun error(message: String = "Syntax error at token $token"): Nothing = throw ParseException("Line ${lexer.currentLine}: $message")

    private inline fun <reified T : Token> expect() = maybe<T>()
            ?: error("Unexpected token $token. Token of type ${T::class.simpleName} expected.")

    private inline fun <reified T : Token> maybe() = (token as? T)?.also { nextToken() }

    private fun maybeOneOf(vararg tokens: Token) = tokens.firstOrNull { it == token }?.also { nextToken() }

    private inline fun <T : Token, R> T.andThan(block: (T) -> R): R = block(this)

    private inline fun <reified T : Token> matchList(element: () -> Ast?): List<Ast>? {
        val first = element() ?: return NotMatched
        return mutableListOf(first).apply {
            while (maybe<T>() != null) add(element() ?: error())
        }
    }
}

class ParseException(override val message: String) : Exception()