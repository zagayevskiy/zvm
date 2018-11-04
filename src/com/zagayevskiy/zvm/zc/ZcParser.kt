package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.common.Token
import com.zagayevskiy.zvm.common.Token.*

private object StubAst : Ast()

class ZcParser(private val lexer: Lexer) {

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

    fun topLevelDefinition() {
        struct() ?: function() ?: error("Top-level declaration expected.")
    }

    fun struct(): Ast? {
        if (token != ZcToken.Struct) return null
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
        maybe<ZcToken.Fun>() ?: return null
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
        val name = maybe<Identifier>() ?: return null
        expect<ZcToken.Colon>()
        val typeName = expect<Identifier>()

        return StubAst
    }

    private fun functionReturnType(): Ast? = maybe<Identifier>()?.andThan { StubAst }

    private fun functionBody() = functionBlockBody() ?: functionExpressionBody()

    private fun functionBlockBody() = block()

    private fun functionExpressionBody(): Ast? {
        maybe<ZcToken.Assign>() ?: return null
        return expression() ?: error("Expression expected in expression body.")
    }

    private fun variableDecl(): Ast? {
        return varDecl() ?: valDecl() ?: error("Variable declaration expected")
    }

    private fun varDecl(): Ast? {
        maybe<ZcToken.Var>() ?: return null

        val id = expect<Identifier>()

        val typeId = maybe<ZcToken.Colon>()?.andThan { expect<Identifier>() }

        val assignmentAst = maybe<ZcToken.Assign>()?.andThan { expect<Identifier>() }

        if (typeId == null && assignmentAst == null) error("type or assignment expected")

        return StubAst
    }


    private fun valDecl(): Ast? {
        maybe<ZcToken.Val>() ?: return null

        val id = expect<Identifier>()

        val typeId = maybe<ZcToken.Colon>()?.andThan { expect<Identifier>() }

        val assignmentAst = expect<ZcToken.Assign>().andThan { expression() } ?: error("Valid expression expected after assignment")

        return StubAst
    }

    private fun block(): Ast? {
        maybe<ZcToken.CurlyBracketOpen>() ?: return null

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

    private fun statement(): Ast? {

    }

    private fun expression(): Ast? {

    }

    private fun nextToken() {
        token = lexer.nextToken().also { currentToken ->
            if (currentToken is Token.Error) {
                error("Lexical error at sequence ${currentToken.sequence}")
            }
        }
    }

    private fun error(message: String = "Syntax error at token $token"): Nothing = throw ParseException(message)

    private inline fun <reified T : Token> expect() = (token as? T)?.also { nextToken() }
            ?: error("Unexpected token $token. Token of type ${T::class.simpleName} expected.")

    private inline fun <reified T : Token> maybe() = (token as? T)?.also { nextToken() }
    private inline fun <reified T : Token> atLeastOne() {
        expect<T>()
        skipAll<T>()
    }

    private inline fun <reified T : Token> skipAll() {
        while (maybe<T>() != null);
    }

    private fun <T : Token, R> T.andThan(block: (T) -> R): R = block(this)
}

class ParseException(override val message: String) : Exception()