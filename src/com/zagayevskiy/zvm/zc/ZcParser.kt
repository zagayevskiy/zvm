package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.common.Token.*

private object StubAst : Ast()

class ZcParser(private val lexer: Lexer) {

    companion object {
        private val NotMatched = null
    }

    private lateinit var token: Token
    fun program() {
        try {
            nextToken()
            topLevelDefinition()
            while (token != Eof) {
                atLeastOne<Eol>()
                topLevelDefinition()
            }
        } catch (e: ParseException) {
//            return ParseResult.Failure(lexer.currentLine, e.message, commands, e)
        }

//        return ParseResult.Success(commands)
    }

    private fun topLevelDefinition() {
        struct() ?: function() ?: error("Top-level declaration expected.")
    }

    private fun struct(): Ast? {
        maybe<ZcToken.Struct>() ?: return NotMatched
        nextToken()

        expect<ZcToken.CurlyBracketOpen>()

        structDeclarationList()

        expect<ZcToken.CurlyBracketClose>()

        return StubAst
    }

    private fun structDeclarationList(): List<Ast> = mutableListOf<Ast>().apply {
        skipAll<Eol>()
        while (true) {
            add(variableDecl() ?: break)
            atLeastOne<Eol>()
        }
        if (isEmpty()) error("At least one variable declaration expected.")
    }

    //"fn" identifier "(" function_args_list ")" [":" return_type] function_body
    private fun function(): Ast? {
        maybe<ZcToken.Fun>() ?: return NotMatched
        val name = expect<Identifier>()
        expect<ZcToken.CurlyBracketOpen>()
        val args = functionArgsList()
        expect<ZcToken.CurlyBracketClose>()
        val returnType = maybe<ZcToken.Colon>()?.andThan { functionReturnType() ?: error("Return type expected.") }
        functionBody() ?: error("Function body expected.")
        return StubAst
    }

    //function_args_list ::= [ function_arg_definition {"," function_arg_definition} ]
    private fun functionArgsList(): List<Ast> {
        skipAll<Eol>()
        val first = functionArgDefinition() ?: return emptyList()
        val list = mutableListOf(first)

        while (maybe<ZcToken.Comma>() != null) {
            list.add(functionArgDefinition() ?: error("Argument declaration expected."))
        }

        return list
    }

    //function_arg_definition ::= identifier ":" identifier
    private fun functionArgDefinition(): Ast? {
        val name = maybe<Identifier>() ?: return NotMatched
        expect<ZcToken.Colon>()
        val typeName = expect<Identifier>()

        return StubAst
    }

    private fun functionReturnType(): Ast? = maybe<Identifier>()?.andThan { StubAst }

    private fun functionBody() = functionBlockBody() ?: functionExpressionBody()

    private fun functionBlockBody() = block()

    private fun functionExpressionBody(): Ast? {
        maybe<ZcToken.Assign>() ?: return NotMatched
        return expression() ?: error("Expression expected in expression body.")
    }

    private fun variableDecl(): Ast? {
        return varDecl() ?: valDecl() ?: error("Variable declaration expected")
    }

    // var_declaration ::= "var" identifier ((":" identifier) ["=" expression] | ("=" expression))
    private fun varDecl(): Ast? {
        maybe<ZcToken.Var>() ?: return NotMatched

        val id = expect<Identifier>()

        val typeId = maybe<ZcToken.Colon>()?.andThan { expect<Identifier>() }

        val assignmentAst = maybe<ZcToken.Assign>()?.andThan { expression() ?: error("Expression expected.") }

        if (typeId == null && assignmentAst == null) error("type or assignment expected")

        return StubAst
    }


    private fun valDecl(): Ast? {
        maybe<ZcToken.Val>() ?: return NotMatched

        val id = expect<Identifier>()

        val typeId = maybe<ZcToken.Colon>()?.andThan { expect<Identifier>() }

        val assignmentAst = expect<ZcToken.Assign>().andThan { expression() } ?: error("Valid expression expected after assignment")

        return StubAst
    }

    private fun block(): Ast? {
        maybe<ZcToken.CurlyBracketOpen>() ?: return NotMatched

        skipAll<Eol>()
        val first = statement() ?: return StubAst // empty
        val statements = mutableListOf(first)
        while (true) {
            expect<Eol>()
            val next = statement() ?: break
            statements.add(next)
            skipAll<Eol>()
        }
        expect<ZcToken.CurlyBracketClose>()

        return StubAst //filled
    }

    // statement ::= variable_declaration | loop | function_return_statement | expression
    private fun statement() = variableDecl() ?: loopStatement() ?: functionReturnStatement() ?: expression()

    private fun loopStatement() = forLoop() ?: whileLoop()

    // for_loop ::= "for" "(" [for_loop_initializer] ";" [for_loop_condition] ";" [for_loop_step] ")" block
    private fun forLoop(): Ast? {
        maybe<ZcToken.For>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val initializer = forLoopInitializer()
        expect<ZcToken.Semicolon>()
        val condition = forLoopCondition()
        expect<ZcToken.Semicolon>()
        val step = forLoopStep()
        expect<ZcToken.ParenthesisClose>()
        val body = block() ?: error("For-loop body expected.")
        return StubAst
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

    private fun forLoopCondition(): Ast {
        return expression() ?: StubAst // TODO ?: true
    }

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
    private fun whileLoop(): Ast? {
        maybe<ZcToken.While>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val condition = expression() ?: error("Expression expected.")
        expect<ZcToken.ParenthesisClose>()
        val body = block() ?: error("While-loop body expected.")

        return StubAst
    }

    private fun functionReturnStatement(): Ast? {
        maybe<ZcToken.Return>() ?: return NotMatched
        val expression: Ast = expression() ?: error("Expression expected.")

        return StubAst
    }

    // expression ::= if_else_expr | disjunction_expr
    private fun expression() = ifElseExpr() ?: disjunctionExpr()

    // "if" "(" expression ")" (block | expression) [ "else" (block | expression) ]
    private fun ifElseExpr(): Ast? {
        maybe<ZcToken.If>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val condition = expression() ?: error("Expression expected as if-condition.")
        expect<ZcToken.ParenthesisClose>()
        val ifBody = block() ?: expression() ?: error("if-body expected.")
        val elseBody = maybe<ZcToken.Else>()?.andThan { block() ?: expression() ?: error("else-body expected") }

        return StubAst
    }

    // disjunction_expr ::= conjunction_expr { "||" conjunction_expr }
    private fun disjunctionExpr(): Ast? {
        var conjuctions = matchList<ZcToken.Disjunction>(::conjunctionExpr) ?: return NotMatched
        return StubAst
    }

    // conjunction_expr ::= bit_or_expr { "&&" bit_or_expr }
    private fun conjunctionExpr(): Ast? {
        val ors = matchList<ZcToken.Conjunction>(::bitOrExpr) ?: return NotMatched
        return StubAst
    }

    // bit_or_expr ::= bit_xor_expr { "|" bit_xor_expr }
    private fun bitOrExpr(): Ast? {
        val xors = matchList<ZcToken.BitOr>(::bitXorExpr) ?: return NotMatched
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
        val left = unaryExpr()
        fun maybeMul() = maybeOneOf(ZcToken.Asterisk, ZcToken.Slash, ZcToken.Percent)

        var token = maybeMul()
        while (token != null) {
            unaryExpr()
            token = maybeMul()
        }

        return StubAst
    }

    // unary_expr ::= [( "~" | "!" | "@")] value_expr
    private fun unaryExpr(): Ast? = maybe<ZcToken.BitNot>()?.andThan { valueExpr() ?: error("Bit-not argument expected.") }
            ?: maybe<ZcToken.LogicalNot>()?.andThan { valueExpr() ?: error("Logical-not argument expected.") }

    private fun valueExpr(): Ast? {

    }

    private fun nextToken() {
        token = lexer.nextToken().also { currentToken ->
            (currentToken as? Token.Error)?.let { error("Lexical error at sequence ${it.sequence}") }
        }
    }

    private fun error(message: String = "Syntax error at token $token"): Nothing = throw ParseException(message)

    private inline fun <reified T : Token> expect() = maybe<T>()
            ?: error("Unexpected token $token. Token of type ${T::class.simpleName} expected.")

    private inline fun <reified T : Token> maybe() = (token as? T)?.also { nextToken() }

    private fun maybeOneOf(vararg tokens: Token) = tokens.firstOrNull { it == token }?.also { nextToken() }

    private inline fun <reified T : Token> atLeastOne() {
        expect<T>()
        skipAll<T>()
    }

    private inline fun <reified T : Token> skipAll() {
        while (maybe<T>() != null);
    }

    private inline fun <T : Token, R> T.andThan(block: (T) -> R): R = block(this)

    private inline fun <reified T : Token> matchList(element: () -> Ast?): List<Ast>? {
        val first = element() ?: return NotMatched
        return mutableListOf(first).apply {
            while (maybe<T>() != null) add(element() ?: error())
        }
    }
}

class ParseException(override val message: String) : Exception()