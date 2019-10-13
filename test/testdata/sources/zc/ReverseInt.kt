package testdata.sources.zc

import testdata.cases.TestSource

internal object ZcReverseInt {
    //run with one argument(int) and reverse it bytes
     val BytesViaBitOps = TestSource("Reverse int bytes via bit ops", """
        fn main(x: int): int {
            return ((x >> 24) & 255) | ((x >> 8) & 65280) | ((x & 255) << 24) | ((x & 65280) << 8);
        }
    """.trimIndent())
}