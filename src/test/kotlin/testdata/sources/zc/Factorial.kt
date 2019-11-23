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

    //run with one int argument (n) and get n's Fibonacci number (computed by iterative way)
    val Iterative = TestSource("n! iterative",  """
        fn main(n: int): int {
            var accum = 1;
            for(var i = 0; i < n; i = i + 1, accum = accum * i) {}
            return accum;
        }
    """.trimIndent())
}