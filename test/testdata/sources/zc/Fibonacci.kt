package testdata.sources.zc

import testdata.cases.TestSource
import testdata.sources.asm.AsmFibonacci

internal object ZcFibonacci {

    //run with one int argument (n) and get n's Fibonacci number (computed by recursive way)
    val Recursive = TestSource("Fibonacci recursive", """
        fn fibonacci(prevStep: int, currentStep: int, counter: int): int {
            if (counter <= 0)
                return currentStep;
            else
                return fibonacci(currentStep, prevStep + currentStep, counter - 1);
        }

        fn main(n: int): int {
            return fibonacci(1, 1, n - 2);
        }
    """.trimIndent())

    object Iterative {
        val AsmInsertImpl = TestSource("Fibonacci iterative asm insert", """
            fn fibonacciAsm(n: int): int {
                asm {"
                    ${AsmFibonacci.iterativeFunctionBody()}
                "}
            }

            fn main(n: int): int {
                return fibonacciAsm(n);
            }
        """.trimIndent())
    }
}