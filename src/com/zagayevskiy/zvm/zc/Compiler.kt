package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.asm.*
import com.zagayevskiy.zvm.vm.BytecodeLoader
import com.zagayevskiy.zvm.vm.LoadingResult
import com.zagayevskiy.zvm.vm.StackEntry
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


private val stdMem = """
    fn alloc(size: int): [void] {
        asm{"
            aloadi 0
            alloc
            ret
        "}
    }

    fn free(memory: [void]): int {
        asm {"
            aloadi 0
            free
            consti 0
            ret
        "}
    }

    fn null(): [void] {
        asm {"
            consti 0
            ret
        "}
    }
""".trimIndent()

internal val linkedList ="""

    $stdMem

    struct Node {
        var payload: byte;
        var next: [void];
    }

    fn newNode(payload: byte, next: Node): Node {
        val node: Node = alloc(sizeof<Node>);
        node.payload = payload;
        node.next = next;
        return node;
    }

    fn createList(count: int): Node {
        val nil = null();
        var root: Node = nil;
        var tail: Node = nil;

        val one: byte = 1;
        var tmp: Node;
        for (var i: byte = 1; i <= count; i = i + one) {
            tmp = newNode(i, nil);
            if (root == nil) {
                root = tmp;
                tail = root;
            } else {
                tail.next = tmp;
                tail = tail.next;
            }
        }

        return root;
    }

    fn sumList(root: Node): int {
        var result = 0;
        var cursor = root;
        while(cursor != null()) {
            result = result + cursor.payload;
            cursor = cursor.next;
        }
        return result;
    }

    fn toArray(root: Node): [byte] {
        val size = size(root);
        val result: [byte] = alloc(size(root));
        var cursor = root;
        for (var i = 0; i < size; i = i + 1) {
            result[i] = cursor.payload;
            cursor = cursor.next;
        }

        return result;
    }

    fn size(root: Node): int {
        var cursor = root;
        var result = 0;
        while(cursor != null()) {
            result = result + 1;
            cursor = cursor.next;
        }


        return result;
    }

    fn reverse(root: Node): Node {
        var current = root;
        var prev: Node = null();
        var next: Node;

        while(current != null()) {
            next = current.next;
            current.next = prev;
            prev = current;
            current = next;
        }

        return prev;
    }
""".trimIndent()

fun main(args: Array<String>) {
    val text = """
        $linkedList

        fn main(count: int): [byte] {
            val list = createList(int);

            return toArray(reverse(list));
        }

    """.trimIndent()

    val compiler = ZcCompiler()
    val loader = BytecodeLoader(compiler.compile(text))
    val heap = BitTableMemory(1000000)
    val vm = VirtualMachine((loader.load() as LoadingResult.Success).info, heap = heap)
    val result = vm.run(emptyList())
    println(result)
    val buffer = ByteArray(100)
    heap.copyOut((result as StackEntry.VMInteger).intValue, buffer)
    println(buffer.joinToString())
}