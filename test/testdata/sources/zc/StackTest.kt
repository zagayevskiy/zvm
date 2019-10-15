package testdata.sources.zc

import testdata.cases.TestSource
import testdata.sources.zc.includes.includeCrash
import testdata.sources.zc.includes.includeStack
import testdata.sources.zc.includes.includeStdIo
import testdata.sources.zc.includes.includeStdMem

internal val stackTest = TestSource("Stack test", """
    ${includeStdMem()}
    ${includeStack()}
    ${includeCrash()}
    ${includeStdIo()}

    fn main(): int {
        val stack = createStack(200);
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


    fn assert(condition: bool, message: int): int {
        if (!condition) { crash(message); }
        return 0;
    }
""".trimIndent())

/*
for(var i = 0; i < 100; i = i + 1) {
            if (pushInt(stack, i*multiplier) != 0) return 1;
            if (peekInt(stack) != i*multiplier) return 12;

            if (pushInt(stack, i + 10000) != 0) return 11;
            if(peekInt(stack) != 10000 + i) return 112;
            if (popInt(stack) != i + 10000) return 111;

            if (pushByte(stack, cast<byte>(i + 1)) != 0) return 1111;
            if (popByte(stack) != cast<byte>(i + 1)) return 11111;

            if (pushByte(stack, cast<byte>(i)) != 0) return 2;
        }

        for(var j = 99; j >= 0; j = j - 1) {
            val b = popByte(stack);
            if (b != cast<byte>(j)) return b + 1;
            if (popInt(stack) != j*multiplier) return 4;
        }
 */