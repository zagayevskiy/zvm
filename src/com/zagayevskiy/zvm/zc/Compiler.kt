package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.vm.BytecodeLoader
import com.zagayevskiy.zvm.vm.LoadingResult
import com.zagayevskiy.zvm.vm.VirtualMachine
import com.zagayevskiy.zvm.zc.ast.AstProgram
import com.zagayevskiy.zvm.zc.visitors.ByteCommandsGenerator
import com.zagayevskiy.zvm.zc.visitors.TopLevelDeclarationsResolver
import com.zagayevskiy.zvm.zc.visitors.TypesProcessor

class ZcCompiler {

    fun compile(programText: String): ByteArray {
        val lexer = ZcSequenceLexer(programText.asSequence())
        val parser = ZcParser(lexer)
        val program = (parser.program() as ParseResult.Success).program
        val resolver = TopLevelDeclarationsResolver(program)
        val typesProcessor = TypesProcessor(resolver.resolve() as AstProgram)
        val resolved = typesProcessor.processTypes() as AstProgram
        val generator = ByteCommandsGenerator(resolved, ::asmParser)
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
}


fun main(args: Array<String>) {
    val text = """
        fn initGlobals() { asm {"globals = 1"} }

        fn main(): int {
            var x = asmInsert(123);
            x = x + asmInsert(1000);


            return getGlobal0() + x;
        }

        fn getGlobal0(): int {
            asm {"
                gloadi 0
                ret
            "}
        }

        fn asmInsert(x: int): int {
            asm {"
                aloadi 0
                consti 2
                muli
                dup
                gstori 0
                ret
            "}
        }
    """.trimIndent()

    val compiler = ZcCompiler()
    val loader = BytecodeLoader(compiler.compile(text))
    val vm = VirtualMachine((loader.load() as LoadingResult.Success).info)
    println(vm.run(emptyList()))
}