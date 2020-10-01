package testdata.sources.zc

import testdata.cases.TestSource


internal val stackTest = TestSource("Stack test", """
    fn main(): int {

        val stack : Stack = createStack(200);

        pushInt(stack, 1);
        assert(peekInt(stack) == 1, 1);
        popInt(stack);
        assert(stack.top == 0, 2);

        val b3: byte = 3;
        val b4: byte = 4;

        pushInt(stack, 2);
        pushByte(stack, b3);
        assert(peekByte(stack) == b3, 3);
        pushByte(stack, b4);
        assert(peekByte(stack) == b4, 4);
        assert(popByte(stack) == b4, 5);
        assert(popByte(stack) == b3, 6);
        assert(popInt(stack) == 2, 7);

        val multiplier = 123456;
        for(var i = 0; i < 100; i = i + 1) {
            pushInt(stack, i*multiplier);
            assert(peekInt(stack) == i*multiplier, 8);
            assert(popInt(stack) == i*multiplier, 9);

            pushByte(stack, cast<byte>(i + 1));
            assert(peekByte(stack) == cast<byte>(i + 1), 10);

            pushInt(stack, i + 10000);
            assert(peekInt(stack) == 10000 + i, 11);
            assert(popInt(stack) == i + 10000, 12);

            assert(popByte(stack) == cast<byte>(i + 1), 13);

        }

        assert(stack.top == 0, 14);

        return 0;
    }

    @include<std/mem.zc>
    @include<std/assert.zc>
    @include<std/io.zc>
    @include<container/arraystack.zc>


    fn assert(condition: bool, message: int): int {
        if (!condition) { crash(message); }
        return 0;
    }
""".trimIndent())
/*

 */