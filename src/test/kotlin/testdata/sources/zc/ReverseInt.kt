package testdata.sources.zc

import testdata.cases.TestSource

internal object ZcReverseInt {
    //run with one argument(int) and reverse it bytes
     val Bytes = TestSource("Reverse int bytes", """
        const firstByteMask = 255;
        const secondByteMask = 65280;
        fn main(x: int): int {
            return ((x >> 24) & firstByteMask) | ((x >> 8) & secondByteMask) | ((x & firstByteMask) << 24) | ((x & secondByteMask) << 8);
        }
    """.trimIndent())


    //run with one argument(int) and reverse it bits
    val Bits = TestSource("Reverse int bits", """
        fn main(x: int): int {
            var result = 0;
            for (var i = 0; i < 32; i = i + 1) result = result | (((x >> i) & 1) << (31 - i));
            return result;
        }
    """.trimIndent())
}