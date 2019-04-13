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
            var z: byte;
            var array: [[[[int]]]];
            var y: int;
        }

        fn main(): int {
            val matrix: [[point]] = pointMatrix(10);

            var result = 0;
            for (var i = 0; i < 10; i = i + 1) {
                result = result + matrix[i][i].x + matrix[i][i].y + matrix[i][i].z;
            }

            return sizeof<(int, [int], [[point]]) -> int>;
        }

        fn map(value: int, callback: (int) -> int): int {
            return 0;
        }

        fn pointMatrix(size: int): [[point]] {
            var matrix: [[point]];

             asm{"
                aloadi 0
                consti 4
                muli
                alloc
                lstori 0
            "}

            for (var i = 0; i < size; i = i + 1) {
                matrix[i] = arrayOfPoint(size);
                for (var j = 0; j < size; j = j + 1) {
                    matrix[i][j] = createPoint();
                    matrix[i][j].x = i;
                    matrix[i][j].y = j;
                    matrix[i][j].z = 17;
                }
            }

            return matrix;
        }

        fn arrayOfPoint(size: int): [point]{
            asm{"
                aloadi 0
                consti 4
                muli
                alloc
                ret
            "}
        }

        fn createPoint(): point {
            val size = sizeof<point>;
            asm{"
                lloadi 0
                alloc
                ret
            "}
        }
    """.trimIndent()

    val compiler = ZcCompiler()
    val loader = BytecodeLoader(compiler.compile(text))
    val vm = VirtualMachine((loader.load() as LoadingResult.Success).info, heapSize = 1000000)
    println(vm.run(emptyList()))
}