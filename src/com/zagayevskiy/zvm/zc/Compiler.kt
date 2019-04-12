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
            val ints: [int] = arrayOfInt(10);
            val points = createPointsArray(10);
            for (var i = 0; i < 10; i = i + 1) {
                ints[i] = i;
                points[i] = createPoint();
                points[i].x = ints[i];
                points[i].y = ints[i];
            }

            var result = 0;
            for (var j = 0; j < 10; j = j + 1) {
                result = result + (points[j].x + points[j].y);
            }

            return result;
        }

        fn arrayOfInt(size: int): [int] {
            asm{"
                aloadi 0
                consti 4
                muli
                alloc
                ret
            "}
        }

        fn createPoint(): point {
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

    /*

       for (val i = 0; i < 10; i = i + 1) {
                points[i].x = i;
                points[i].y = 0 - i;
            }
      var result = 0;
            for (val j = 0; j < 10; j = j + 1) {
                result = result + points[j].y * points[j].x;
            }

     */

    val compiler = ZcCompiler()
    val loader = BytecodeLoader(compiler.compile(text))
    val vm = VirtualMachine((loader.load() as LoadingResult.Success).info, heapSize = 1024)
    println(vm.run(emptyList()))
}