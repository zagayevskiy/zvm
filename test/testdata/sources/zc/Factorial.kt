package testdata.sources.zc

import testdata.cases.TestSource

internal object ZcFactorial {

    //run with one int argument(n) and get n! (computed by recursive way)
    val Recursive = TestSource("n! recursive", """
        fn main(n: int): int {
            if (n <= 1) return 1;
            return main(n - 1)*n;
        }
    """.trimIndent())
}