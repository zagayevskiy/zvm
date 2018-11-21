@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.Lexer
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

internal data class TestData(val text: String, val expected: AstProgram)

private infix fun String.expect(expected: AstProgram) = TestData(this, expected)
private val String.type
    get() = this

private val Int.const
    get() = AstConst.Integer(this)

private val String.id
    get() = AstIdentifier(this)

private fun program(vararg declarations: TopLevelDeclaration) = AstProgram(mutableListOf(*declarations))
private fun fn(name: String, args: List<FunctionArgumentDeclaration>, returnType: String? = null, body: AstStatement) = AstFunctionDeclaration(name, args, returnType, body)
private inline class Arg(val name: String)

private infix fun Arg.withType(type: String) = FunctionArgumentDeclaration(name, UnresolvedType.Simple(type))

@RunWith(Parameterized::class)
internal class ZcParserTest(private val test: TestData) {


    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = listOf(
                "fn f(){}" expect program(fn("f", emptyList(), body = AstBlock.Empty)),
                "fn main(i: int) {}" expect program(fn("main", args = listOf(Arg("i") withType "int"), body = AstBlock.Empty)),
                "fn g(): int {}" expect program(fn("g", args = emptyList(), returnType = "int".type, body = AstBlock.Empty)),
                "fn k(){ 1 + 2; }" expect program(fn("k", args = emptyList(), body = AstBlock(listOf(AstExpressionStatement(
                        AstSum(1.const, 2.const)))))),
                "fn k(){ 3 * 4 + 0; }" expect program(fn("k", args = emptyList(), body = AstBlock(listOf(AstExpressionStatement(
                        AstSum(AstMul(4.const, 4.const), 0.const)))))),
                "fn k(){ (5 * 6) - 123; }" expect program(fn("k", args = emptyList(), body = AstBlock(listOf(AstExpressionStatement(
                        AstDifference(AstMul(5.const, 6.const), 123.const)))))),
                "fn k(){ 7 / 8 * 9 + (10 - 11); }" expect program(fn("k", args = emptyList(), body = AstBlock(listOf(AstExpressionStatement(
                        AstSum(AstDiv(7.const, AstMul(8.const, 9.const)), AstDifference(10.const, 11.const))
                ))))),
                "fn v(i: int1): int2 { val j: int3 = 100; }" expect program(
                        fn("v", args = listOf(Arg("i") withType "int1"), returnType = "int2".type, body = AstBlock(listOf(
                                AstValDecl("j", "int3".type, 100.const)
                        )))),
                "fn iff(){ if (1){} } " expect program(
                        fn("iff", args = emptyList(), body = AstBlock(listOf(
                                AstIfElse(
                                        condition = 1.const,
                                        ifBody = AstBlock.Empty,
                                        elseBody = null)
                        )))),
                "fn ifelse(c: bool){ if ((1 + 2) || c) val x = 0; else return 3; } " expect program(
                        fn("ifelse", args = listOf(Arg("c") withType "bool"), body = AstBlock(listOf(
                                AstIfElse(
                                        condition = AstConjunction(AstSum(1.const, 2.const), "c".id),
                                        ifBody = AstValDecl("x", null, initializer = 0.const),
                                        elseBody = AstFunctionReturn(3.const))
                        )))),
                "fn for_loop_infinite() { for(;;){} } " expect program(
                        fn("for_loop_infinite", args = emptyList(), body = AstBlock(listOf(
                                AstForLoop(
                                        initializer = AstBlock.Empty,
                                        condition = AstConst.Undefined,
                                        step = AstBlock.Empty,
                                        body = AstBlock.Empty
                                ))))),
                "fn for_loop(){ for(var i = 0, val k = 1; i + 1 < 10; i = i + 3, k){} }" expect program(
                        fn("for_loop", args = emptyList(), body = AstBlock(listOf(
                                AstForLoop(
                                        initializer = AstBlock(listOf(
                                                AstVarDecl("i", null, 0.const),
                                                AstValDecl("k", null, 1.const))),
                                        condition = AstLess(AstSum("i".id, 1.const), 10.const),
                                        step = AstBlock(listOf(AstExpressionStatement(AstAssignment("i".id, AstSum("i".id, 3.const))), AstExpressionStatement("k".id))),
                                        body = AstBlock.Empty
                                ))))),
                "fn while_loop(){ while(1){} }" expect program(
                        fn("while_loop", args = emptyList(), body = AstBlock(listOf(
                                AstWhileLoop(
                                        condition = 1.const,
                                        body = AstBlock.Empty
                                ))))
                )
        )
    }

    @Test
    fun test() {
        val parser = ZcParser(PrintSpyLexer(ZcSequenceLexer(test.text.asSequence())))
        val result = parser.program() as ParseResult.Success
        assertEquals(test.expected, result.program)
    }

}

class PrintSpyLexer(private val lexer: Lexer) : Lexer {
    override fun nextToken() = lexer.nextToken().also { println(it) }

    override val currentLine
        get() = lexer.currentLine
}