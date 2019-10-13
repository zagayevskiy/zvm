package testdata.sources.zc

import testdata.cases.TestSource
import testdata.sources.zc.includes.includeCrash
import testdata.sources.zc.includes.includeStack
import testdata.sources.zc.includes.includeStdMem

internal val stackTest = TestSource("Stack test", """
    ${includeStdMem()}
    ${includeStack()}
    ${includeCrash()}

    fn main(): int {
        val stack = createStack(200);
        val multiplier = 123456;
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

        return 0;
    }
""".trimIndent())