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
        struct point {
            var x: int;
            var y: int;
        }

        fn main(): int {
            val matrix: [[int]] = intMatrix(10);
            return matrix[0][0];
        }

        fn intMatrix(size: int): [[int]] {
            asm {"
                consti 123456
                pop
            "}
            var matrix: [[int]];

            matrix = allocMatrix(size);
            for (var i = 0; i < size; i = i + 1) {
                matrix[i] = arrayOfInt(size);
            }

            return matrix;
        }

        fn allocMatrix(size: int): [[int]] {
            asm{"
                aloadi 0
                consti 4
                muli
                alloc
                ret
            "}
        }


        fn arrayOfInt(size: int): [int]{
            asm{"
                aloadi 0
                consti 4
                muli
                alloc
                ret
            "}
        }

        fn createPoint(): [[point]] {
            asm{"
                consti 8
                alloc
                ret
            "}
        }

        fn createPointsArray(size: int): [point] {
            asm{"
                aloadi 0
                consti 4
                muli
                alloc
                ret
            "}
        }

        fn freePoint(p: point): int {
            asm{"
                aloadi 0
                free
                consti 0
                ret
            "}
        }

    """.trimIndent()

    val compiler = ZcCompiler()
    val loader = BytecodeLoader(compiler.compile(text))
    val vm = VirtualMachine((loader.load() as LoadingResult.Success).info, heapSize = 1024)
    println(vm.run(emptyList()))
}