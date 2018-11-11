package com.zagayevskiy.zvm.zc.visitors

import com.zagayevskiy.zvm.zc.ParseResult
import com.zagayevskiy.zvm.zc.ZcParser
import com.zagayevskiy.zvm.zc.ZcSequenceLexer
import com.zagayevskiy.zvm.zc.ast.*

class GraphVizGenerator(private val ast: Ast) {

    private var id = 0

    private val visitor = object : AstVisitor<Pair<String, Int>> {
        override fun visit(ast: StubAst) = "stub" to ++id

        override fun visit(ast: AstProgram) = "program" to ++id

        override fun visit(ast: AstFunctionDeclaration) = "decl fun ${ast.name}" to ++id

        override fun visit(ast: AstStructDeclaration) = "decl struct ${ast.name}" to ++id

        override fun visit(ast: AstDefinedFunction) = "fun ${ast.name}" to ++id

        override fun visit(ast: AstBlock) = "{...}" to ++id

        override fun visit(ast: AstVarDecl) = "decl var ${ast.varName}: ${ast.typeName}" to ++id

        override fun visit(ast: AstValDecl) = "decl val ${ast.valName}: ${ast.typeName}" to ++id

        override fun visit(ast: AstLoop) = "loop-for" to ++id

        override fun visit(ast: AstWhile) = "loop-while" to ++id

        override fun visit(ast: AstIfElse) = "if-else" to ++id

        override fun visit(ast: AstFunctionReturn) = "return" to ++id

        override fun visit(ast: AstExpressionStatement) = "expr statement" to ++id

        override fun visit(ast: AstAssignment) = "=" to ++id

        override fun visit(ast: AstDisjunction) = "||" to ++id

        override fun visit(ast: AstConjunction) = "&&" to ++id

        override fun visit(ast: AstBitAnd) = "&" to ++id

        override fun visit(ast: AstBitOr) = "|" to ++id

        override fun visit(ast: AstBitXor) = "^" to ++id

        override fun visit(ast: AstBitShift.Left) = "<<" to ++id

        override fun visit(ast: AstBitShift.Right) = ">>" to ++id

        override fun visit(ast: AstEquals) = "==" to ++id

        override fun visit(ast: AstNotEquals) = "!=" to ++id

        override fun visit(ast: AstLess) = "<" to ++id

        override fun visit(ast: AstLessEq) = "<=" to ++id

        override fun visit(ast: AstGreat) = ">" to ++id

        override fun visit(ast: AstGreatEq) = ">=" to ++id

        override fun visit(ast: AstSum) = "+" to ++id

        override fun visit(ast: AstDifference) = "-" to ++id

        override fun visit(ast: AstMul) = "*" to ++id

        override fun visit(ast: AstDiv) = "/" to ++id

        override fun visit(ast: AstMod) = "%" to ++id

        override fun visit(ast: AstVariable) = "var:${ast.varName}" to ++id

        override fun visit(ast: AstArrayIndexing) = "[ ]" to ++id

        override fun visit(ast: AstFunctionCall) = "call" to ++id

        override fun visit(ast: AstConst.Integer) = "int:${ast.value}" to ++id

        override fun visit(ast: AstConst.Byte) = "byte:${ast.value}" to ++id

        override fun visit(ast: AstConst.Boolean) = "bool:${ast.value}" to ++id

        override fun visit(ast: AstConst.Undefined) = "undefined" to ++id

        override fun visit(ast: AstConst.Void) = "void" to ++id

        override fun visit(ast: AstLogicalNot) = "!" to ++id

        override fun visit(ast: AstBitNot) = "~" to ++id
    }

    fun generateDot() = StringBuilder().apply {
        append("digraph AST {\n")
        generate(ast)
        append("}\n")
    }.toString()

    private fun StringBuilder.generate(ast: Ast): Pair<String, Int> = visit(visitor, ast).also { (label, id) ->
        append("""
            node_$id[label = "$label"]

        """.trimIndent())

        ast.map { child -> generate(child) }.forEach { (_, childId) ->
            append("""
                node_$id -> node_$childId

            """.trimIndent())
        }
    }
}


fun main(args: Array<String>) {
    val text = """
            fn main(argc: int): int {
                f(1, 2, 3, 4)[5 + 6];
                val a = 1;
                var b = 2;
                var c: int;
                var d: byte = 3;
                val e: int  = 4;
                c = (a + b) - (d[e]*e[d]);
                eeq = 11 || 22 || 33 || 44 & 55 & 66 & 77 || 88 || 99;
                e000 = e + e1 +e2 + e3 +e4 -e5 -e6-e7 + e8 +e9 -e10 * a1* b2 *c3 *c4 /d5 /d6 /d7/d8/d9;
                if(true){
                    c;
                    while(1) {
                        for(;;){}
                    }
                }
                return 100000;
            }
        """.trimIndent()

    val parser = ZcParser(ZcSequenceLexer(text.asSequence()))
    val result = parser.program() as ParseResult.Success
    val dot = GraphVizGenerator(result.program).generateDot()
    println(dot)
}