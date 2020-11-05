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

        val evalStatements = lispProgram.asSequence().map { expr ->
            expr.toExpr()
        }.map { sexpr ->
            eval.call(context, globalEnv, sexpr)
        }.map {
            printCons.call(it)
        }.map(::AstExpressionStatement).toList()

        val customVals = listOf(
                memory assign { context.field("mem") },
                globalEnv assign { context.field("globalEnv") }
        )

        return AstFunctionDeclaration(
                name = "evalProgram",
                returnType = null,
                args = listOf(FunctionArgumentDeclaration(context.name, UnresolvedType.Simple("LispRuntimeContext"))),
                body = AstBlock(
                        statements = customVals +
                                atomVals.values +
                                numberVals.values +
                                strVals.values +
                                evalStatements +
                                listOf(AstFunctionReturn(null))
                )
        )
    }

    private val memory = AstIdentifier("memory")
    private val cons = AstIdentifier("cons")
    private val eval = AstIdentifier("eval")
    private val printCons = AstIdentifier("printCons")
    private val globalEnv = AstIdentifier("globalEnv")
    private val context = AstIdentifier("context")
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

        val valName = AstIdentifier("${valNamePrefix}_$nextId")
        val valDecl = valName assign { function.call(memory, valExpr) }
        cache[value] = valDecl
        return valName
    }

    private infix fun AstIdentifier.assign(initializer: () -> AstExpr): AstValDecl {
        return AstValDecl(name, null, initializer())
    }

    private fun AstIdentifier.call(vararg args: AstExpr): AstFunctionCall {
        return AstFunctionCall(this, args.toList())
    }

    private fun AstIdentifier.field(name: String) = AstStructFieldDereference(this, name)
}