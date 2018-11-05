package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.common.Token.*

private object StubAst : Ast()

sealed class ParseResult {
    class Success : ParseResult()
    class Failure : ParseResult()
}

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
                topLevelDefinition()
            }
        } catch (e: ParseException) {
            throw e
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
        while (true) {
            add(variableDecl() ?: break)
        }
        if (isEmpty()) error("At least one variable declaration expected.")
    }

    //"fn" identifier "(" function_args_list ")" [":" return_type] function_body
    private fun function(): Ast? {
        maybe<ZcToken.Fun>() ?: return NotMatched
        val name = expect<Identifier>()
        expect<ZcToken.ParenthesisOpen>()
        val args = functionArgsList()
        expect<ZcToken.ParenthesisClose>()
        val returnType = maybe<ZcToken.Colon>()?.andThan { functionReturnType() ?: error("Return type expected.") }
        functionBody() ?: error("Function body expected.")
        return StubAst
    }

    //function_args_list ::= [ function_arg_definition {"," function_arg_definition} ]
    private fun functionArgsList(): List<Ast> {
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

    private fun variableDecl() = (varDecl() ?: valDecl())?.also { expect<ZcToken.Semicolon>() }

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

        expect<ZcToken.Assign>()
        val assignmentExpr = expression() ?: error("Valid expression expected after assignment")

        return StubAst
    }

    private fun block(): Ast? {
        maybe<ZcToken.CurlyBracketOpen>() ?: return NotMatched

        val statements = mutableListOf<Ast>().apply {
            while (true) {
                add(statement() ?: break)
            }
        }

        expect<ZcToken.CurlyBracketClose>()

        return StubAst //filled
    }

    // statement ::= variable_declaration | loop | function_return_statement | expression
    private fun statement() = variableDecl() ?: loopStatement() ?: ifElseStatement() ?: functionReturnStatement() ?: expressionStatement()

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

    // "if" "(" expression ")" (block | expression) [ "else" (block | expression) ]
    private fun ifElseStatement(): Ast? {
        maybe<ZcToken.If>() ?: return NotMatched
        expect<ZcToken.ParenthesisOpen>()
        val condition = expression() ?: error("Expression expected as if-condition.")
        expect<ZcToken.ParenthesisClose>()
        val ifBody = block() ?: expression() ?: error("if-body expected.")
        val elseBody = maybe<ZcToken.Else>()?.andThan { block() ?: expression() ?: error("else-body expected") }

        return StubAst
    }

    private fun functionReturnStatement(): Ast? {
        maybe<ZcToken.Return>() ?: return NotMatched
        val expression: Ast = expression() ?: error("Expression expected.")
        expect<ZcToken.Semicolon>()
        return StubAst
    }

    private fun expressionStatement() = expression()?.also { expect<ZcToken.Semicolon>() }

    // expression ::= if_else_expr | disjunction_expr
    private fun expression() = disjunctionExpr()

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