package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.vm.*
import testsrc.zc.includes.includeStdMem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

internal data class LLT(val count: Int)

@RunWith(Parameterized::class)
internal class ZcLinkedListTest(private val test: LLT) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data() = listOf(
                LLT(1),
                LLT(2),
                LLT(10),
                LLT(100)
        )

    }

    private lateinit var compiler: ZcCompiler

    @Before
    fun setup() {
        compiler = ZcCompiler()

    }

    @Test
    fun testReverse() {
        val text = """
        $linkedList

        fn main(count: int): [byte] {
            val list = createList(count);

            return toArray(reverse(list));
        }

    """
        val bytecode = compiler.compile(text)
        val loader = BytecodeLoader(bytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val heap = BitTableMemory(1000000)
        val vm = VirtualMachine(info, heap)
        val actualResult = vm.run(listOf(test.count.toStackEntry()))

        val resultAddr = (actualResult as StackEntry.VMInteger).intValue

        val expectedValue: List<Byte> = (1..test.count).reversed().map { it.toByte() }.toList()
        val buffer = ByteArray(test.count)
        heap.copyOut(resultAddr, buffer, test.count)

        assertEquals(expectedValue, buffer.toList())

        //ensure that heap not corrupted
        for (i in (resultAddr + test.count until heap.size)) {
            assertEquals(0.toByte(), heap[i])
        }
    }

}

internal val linkedList = """

    ${includeStdMem()}

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
"""