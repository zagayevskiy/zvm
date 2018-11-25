package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.asm.BytecodeAssembler
import com.zagayevskiy.zvm.asm.BytecodeGenerator
import com.zagayevskiy.zvm.asm.OpcodesMapping
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
        val generator = ByteCommandsGenerator(resolved)
        val commands = generator.generate()
        val assembler = BytecodeAssembler(commands, OpcodesMapping.mapping)
        val info = assembler.generate()
        val bytecodeGenerator = BytecodeGenerator()
        return bytecodeGenerator.generate(info)
    }

}


fun main(args: Array<String>) {
    val text = """
        fn main(): int {
            return 2 + 3;
        }
    """.trimIndent()

    val compiler = ZcCompiler()
    val loader = BytecodeLoader(compiler.compile(text))
    val vm = VirtualMachine((loader.load() as LoadingResult.Success).info)
    println(vm.run(emptyList()))
}