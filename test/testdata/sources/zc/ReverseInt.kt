package testdata.sources.zc

import testdata.cases.TestSource

internal object ZcReverseInt {
    //run with one argument(int) and reverse it bytes
     val Bytes = TestSource("Reverse int bytes", """
        fn main(x: int): int {
            return ((x >> 24) & 255) | ((x >> 8) & 65280) | ((x & 255) << 24) | ((x & 65280) << 8);
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