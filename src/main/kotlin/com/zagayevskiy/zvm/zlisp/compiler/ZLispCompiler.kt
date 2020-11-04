package com.zagayevskiy.zvm.zlisp.compiler

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.common.preprocessing.JavaAssetsIncludesResolver
import com.zagayevskiy.zvm.zc.ParseResult
import com.zagayevskiy.zvm.zc.ZcParser
import com.zagayevskiy.zvm.zc.ZcPreprocessor
import com.zagayevskiy.zvm.zc.ZcSequenceLexer
import com.zagayevskiy.zvm.zc.ast.*
import com.zagayevskiy.zvm.zc.types.UnresolvedType
import com.zagayevskiy.zvm.zc.visitors.ByteCommandsGenerator
import com.zagayevskiy.zvm.zc.visitors.TopLevelDeclarationsResolver
import com.zagayevskiy.zvm.zc.visitors.TypesProcessor
import com.zagayevskiy.zvm.zlisp.LispParseResult
import com.zagayevskiy.zvm.zlisp.Sexpr
import com.zagayevskiy.zvm.zlisp.ZLispLexer
import com.zagayevskiy.zvm.zlisp.ZLispParser

class ZLispCompiler() {

    private val atomVals = mutableMapOf<String, AstValDecl>()
    private val numberVals = mutableMapOf<Int, AstValDecl>()
    private val strVals = mutableMapOf<String, AstValDecl>()
    private var nextId: Int = 0
        get() = field++

    fun compile(lispProgramText: String): ByteArray {
        val includesResolver = JavaAssetsIncludesResolver("/includes/zc")
        val preprocessor = ZcPreprocessor(includesResolver.resolve("lisp/main.zc")!!, includesResolver)
        val preprocessedText = preprocessor.preprocess()
        val lexer = ZcSequenceLexer(preprocessedText.asSequence())
        val parser = ZcParser(lexer)
        val program = (parser.program() as ParseResult.Success).program.let {
            val evalProgramFn = createEvalProgramFn(lispProgramText)
            AstProgram(declarations = it.declarations.toMutableList().apply { add(evalProgramFn) })
        }
        val resolver = TopLevelDeclarationsResolver(program)
        val resolved = resolver.resolve() as AstProgram
        val typesProcessor = TypesProcessor(resolved)
        val typed = typesProcessor.processTypes() as AstProgram
        val generator = ByteCommandsGenerator(typed, ::asmParser)
        val commands = generator.generate()
        val assembler = BytecodeAssembler(commands, OpcodesMapping.mapping)
        val info = assembler.generate()
        val bytecodeGenerator = BytecodeGenerator()
        return bytecodeGenerator.generate(info)
    }

    private fun asmParser(body: String): AsmParser {
        val lexer = AsmSequenceLexer(body.asSequence())
        return AsmParser(lexer, OpcodesMapping.opcodes)
    }

    private fun createEvalProgramFn(lispProgramText: String): AstFunctionDeclaration {
        val lexer = ZLispLexer(lispProgramText.asSequence())
        val parser = ZLispParser(lexer)
        val lispProgram = (parser.parse() as LispParseResult.Success).program

        val lispDecls = lispProgram.map { expr ->
            AstExpressionStatement(expr.toExpr())
        }

        val memoryVal = listOf(AstValDecl(memoryValName, null, AstStructFieldDereference(AstIdentifier(contextArgName), "mem")))

        return AstFunctionDeclaration(
                name = "evalProgram",
                returnType = null,
                body = AstBlock(
                        statements = memoryVal +
                                atomVals.values +
                                numberVals.values +
                                strVals.values +
                                lispDecls),
                args = listOf(FunctionArgumentDeclaration(contextArgName, UnresolvedType.Simple("LispRuntimeContext"))))
    }

    private val contextArgName = "context"
    private val memoryValName = "memory"
    private val memory = AstIdentifier(memoryValName)
    private val cons = AstIdentifier("cons")
    private val makeAtom = AstIdentifier("makeAtom")
    private val makeNumber = AstIdentifier("makeNumber")
    private val makeString = AstIdentifier("makeString")
    private val nil = AstIdentifier("nil")

    private fun Sexpr.toExpr(): AstExpr {
        //TODO includes
        return when (this) {
            is Sexpr.DotPair -> AstFunctionCall(cons, listOf(memory, head.toExpr(), tail.toExpr()))
            is Sexpr.Atom -> obtainAtom(name)
            is Sexpr.Number -> obtainNumber(value)
            is Sexpr.Str -> obtainString(value)
            Sexpr.Nil -> nil
            is Sexpr.RuntimeOnly -> throw IllegalStateException("Must not be here $this")
        }
    }

    private fun obtainAtom(name: String): AstIdentifier {
        return obtain("atom", name, atomVals, makeAtom, AstConst.StringLiteral(name))
    }

    private fun obtainNumber(value: Int): AstIdentifier {
        return obtain("number", value, numberVals, makeNumber, AstConst.Integer(value))
    }

    private fun obtainString(value: String): AstIdentifier {
        return obtain("str", value, strVals, makeString, AstConst.StringLiteral(value))
    }

    private fun <T> obtain(valNamePrefix: String, value: T, cache: MutableMap<T, AstValDecl>, function: AstIdentifier, valExpr: AstExpr): AstIdentifier {
        cache[value]?.let { valDecl ->
            return AstIdentifier(valDecl.valName)
        }

        val valName = "${valNamePrefix}_$nextId"
        val valDecl = AstValDecl(valName, null, AstFunctionCall(function, listOf(memory, valExpr)))
        cache[value] = valDecl
        return AstIdentifier(valName)
    }
}