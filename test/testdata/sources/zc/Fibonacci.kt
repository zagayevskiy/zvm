package testdata.sources.zc

import testdata.cases.TestSource
import testdata.sources.asm.AsmFibonacci
import testdata.sources.zc.includes.includeStdIo

internal object ZcFibonacci {

    //run with one int argument (n) and get n's Fibonacci number (computed by recursive way)
    val Recursive = TestSource("Fibonacci recursive", """
        ${includeStdIo()}
        fn fibonacci(prevStep: int, currentStep: int, counter: int): int {
            outInt(prevStep);
            outInt(currentStep);
            outInt(counter);
            if (counter <= 0)
                return currentStep;
            else
                return fibonacci(currentStep, prevStep + currentStep, counter - 1);
        }

        fn main(n: int): int {
            outInt(n);
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

        //run with one int argument (n) and get n's Fibonacci number
        internal val ForLoop = TestSource("Fibonacci iterative for-loop", """
            fn main(n: int): int {
                if (n <= 1) return 1;
                var prevStep = 1;
                var currentStep = 1;
                for (var step = n - 2, var temp = 0; step > 0; step = step - 1) {
                    temp = prevStep + currentStep;
                    prevStep = currentStep;
                    currentStep = temp;
                }
                return currentStep;
            }
        """.trimIndent())
    }
}